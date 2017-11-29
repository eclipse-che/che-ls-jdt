/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal.debug;

import static java.lang.String.format;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.che.jdt.ls.extension.api.dto.LocationParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;

/** @author Anatolii Bazko */
public class FqnConverter {

  /** Converts {@link LocationParameters} into fqn. The first parameter contains */
  public static String locationToFqn(List<Object> params, IProgressMonitor pm) {
    Preconditions.checkArgument(params.size() >= 2, "File uri and line number are expected");

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    final String fileUri = (String) params.get(0);
    final Integer lineNumber = Integer.valueOf(params.get(1).toString());

    // Not a URI then let's assume it is a FQN.
    if (!fileUri.startsWith("file:")) {
      return fileUri;
    }

    IJavaProject javaProject = JavaModelUtil.getJavaProject(fileUri);

    if (javaProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", fileUri));
    }

    URI fileRelativeUri = javaProject.getProject().getLocationURI().relativize(URI.create(fileUri));
    IPath fileRelativePath = javaProject.getPath().append(fileRelativeUri.toString());

    String fqn = null;
    for (int i = fileRelativePath.segmentCount(); i > 0; i--) {
      try {
        IClasspathEntry classpathEntry =
            ((JavaProject) javaProject)
                .getClasspathEntryFor(fileRelativePath.removeLastSegments(i));

        if (classpathEntry != null) {
          fqn =
              fileRelativePath
                  .removeFirstSegments(fileRelativePath.segmentCount() - i)
                  .removeFileExtension()
                  .toString()
                  .replace("/", ".");
          break;
        }
      } catch (JavaModelException e) {
        return fileUri;
      }
    }

    if (fqn == null) {
      return fileUri;
    }

    IType outerClass;
    IJavaElement iMember;
    try {
      outerClass = javaProject.findType(fqn);

      if (outerClass == null) {
        return fileUri;
      }

      String source;
      if (outerClass.isBinary()) {
        IClassFile classFile = outerClass.getClassFile();
        source = classFile.getSource();
      } else {
        ICompilationUnit unit = outerClass.getCompilationUnit();
        source = unit.getSource();
      }

      Document document = new Document(source);
      IRegion region = document.getLineInformation(lineNumber);
      int start = region.getOffset();
      int end = start + region.getLength();

      iMember = binSearch(outerClass, start, end);
    } catch (JavaModelException e) {
      throw new IllegalArgumentException(
          format(
              "Unable to find source for class with fqn '%s' in the project '%s'",
              fileUri, javaProject),
          e);
    } catch (BadLocationException e) {
      throw new IllegalArgumentException("Unable to calculate breakpoint location", e);
    }

    if (iMember instanceof IType) {
      return ((IType) iMember).getFullyQualifiedName();
    }

    if (iMember != null) {
      fqn = ((IMember) iMember).getDeclaringType().getFullyQualifiedName();
      while (iMember != null && !(iMember instanceof CompilationUnit)) {
        if (iMember instanceof SourceType
            && !((SourceType) iMember).isAnonymous()
            && iMember.getParent() instanceof SourceMethod) {

          fqn = fqn.replace("$" + iMember.getElementName(), "$1" + iMember.getElementName());
        }

        iMember = iMember.getParent();
      }

      return fqn;
    }

    throw new IllegalArgumentException("Unable to calculate breakpoint location");
  }

  /** Converts fqn into {@link LocationParameters}. */
  public static List<LocationParameters> fqnToLocation(
      List<Object> parameters, IProgressMonitor pm) {
    Preconditions.checkArgument(parameters.size() >= 2, "Fqn and line number are expected.");

    String fqn = (String) parameters.get(0);
    Integer lineNumber = Integer.parseInt(parameters.get(1).toString());

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    Pair<char[][], char[][]> fqnPair = prepareFqnToSearch(fqn);
    List<IType> types;
    try {
      types =
          findTypeByFqn(fqnPair.getKey(), fqnPair.getValue(), SearchEngine.createWorkspaceScope());
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

    if (types.isEmpty()) {
      throw new RuntimeException("Type with fully qualified name: " + fqn + " was not found");
    }

    IType type = types.get(0); // TODO we need handle few result! It's temporary solution.
    if (type.isBinary()) {
      IClassFile classFile = type.getClassFile();
      int libId = classFile.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).hashCode();
      return Collections.singletonList(new LocationParameters(fqn, libId, lineNumber));
    } else {
      ICompilationUnit compilationUnit = type.getCompilationUnit();
      URI resourseUri =
          type.getJavaProject().getProject().getFile(compilationUnit.getPath()).getRawLocationURI();
      return Collections.singletonList(new LocationParameters(resourseUri.toString(), lineNumber));
    }
  }

  private static List<IType> findTypeByFqn(
      char[][] packages, char[][] names, IJavaSearchScope scope) throws JavaModelException {
    List<IType> result = new ArrayList<>();

    SearchEngine searchEngine = new SearchEngine();
    searchEngine.searchAllTypeNames(
        packages,
        names,
        scope,
        new TypeNameMatchRequestor() {
          @Override
          public void acceptTypeNameMatch(TypeNameMatch typeNameMatch) {
            result.add(typeNameMatch.getType());
          }
        },
        IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
        new NullProgressMonitor());
    return result;
  }

  private static Pair<char[][], char[][]> prepareFqnToSearch(String fqn) {
    String outerClassFqn = extractOuterClassFqn(fqn);
    int lastDotIndex = outerClassFqn.trim().lastIndexOf('.');

    char[][] packages;
    char[][] names;
    if (lastDotIndex == -1) {
      packages = new char[0][];
      names = new char[][] {outerClassFqn.toCharArray()};
    } else {
      String packageLine = fqn.substring(0, lastDotIndex);
      packages = new char[][] {packageLine.toCharArray()};

      String nameLine = fqn.substring(lastDotIndex + 1, outerClassFqn.length());
      names = new char[][] {nameLine.toCharArray()};
    }
    return Pair.of(packages, names);
  }

  private static String extractOuterClassFqn(String fqn) {
    // handle fqn in case nested classes
    if (fqn.contains("$")) {
      return fqn.substring(0, fqn.indexOf("$"));
    }

    // handle fqn in case lambda expressions
    return fqn.contains("$$") ? fqn.substring(0, fqn.indexOf("$$")) : fqn;
  }

  /**
   * Searches the given source range of the container for a member that is not the same as the given
   * type.
   *
   * @param type the {@link IType}
   * @param start the starting position
   * @param end the ending position
   * @return the {@link IMember} from the given start-end range
   * @throws JavaModelException if there is a problem with the backing Java model
   */
  private static IMember binSearch(IType type, int start, int end) throws JavaModelException {
    IJavaElement je = getElementAt(type, start);
    if (je != null && !je.equals(type)) {
      return asMember(je);
    }
    if (end > start) {
      je = getElementAt(type, end);
      if (je != null && !je.equals(type)) {
        return asMember(je);
      }
      int mid = ((end - start) / 2) + start;
      if (mid > start) {
        je = binSearch(type, start + 1, mid);
        if (je == null) {
          je = binSearch(type, mid + 1, end - 1);
        }
        return asMember(je);
      }
    }
    return null;
  }

  /**
   * Returns the given Java element if it is an <code>IMember</code>, otherwise <code>null</code>.
   *
   * @param element Java element
   * @return the given element if it is a type member, otherwise <code>null</code>
   */
  private static IMember asMember(IJavaElement element) {
    if (element instanceof IMember) {
      return (IMember) element;
    }
    return null;
  }

  /**
   * Returns the element at the given position in the given type
   *
   * @param type the {@link IType}
   * @param pos the position
   * @return the {@link IJavaElement} at the given position
   * @throws JavaModelException if there is a problem with the backing Java model
   */
  private static IJavaElement getElementAt(IType type, int pos) throws JavaModelException {
    if (type.isBinary()) {
      return type.getClassFile().getElementAt(pos);
    }
    return type.getCompilationUnit().getElementAt(pos);
  }
}
