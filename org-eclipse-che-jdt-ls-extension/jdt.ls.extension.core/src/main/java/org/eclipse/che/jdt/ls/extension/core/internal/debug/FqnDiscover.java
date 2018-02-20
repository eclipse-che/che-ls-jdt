/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getWorkspaceJavaProjects;
import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ResourceLocation;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/** @author Anatolii Bazko */
public class FqnDiscover {

  /**
   * Identifies FQN in the given resource.
   *
   * @param params contains two arguments: the resource uri and the line number
   */
  public static String identifyFqnInResource(List<Object> params, IProgressMonitor pm) {
    Preconditions.checkArgument(params.size() >= 2, "Resource uri and line number are expected");

    ensureNotCancelled(pm);

    final String fileUri = (String) params.get(0);
    final Integer lineNumber = Integer.valueOf(params.get(1).toString());

    IJavaProject javaProject = getJavaProject(fileUri);

    if (javaProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", fileUri));
    }

    URI fileRelativeUri =
        javaProject.getProject().getLocationURI().relativize(JDTUtils.toURI(fileUri));
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
    } catch (JavaModelException | BadLocationException e) {
      throw new IllegalArgumentException(e);
    }

    if (iMember instanceof IType) {
      return ((IType) iMember).getFullyQualifiedName();
    }

    if (iMember != null) {
      fqn = ((IMember) iMember).getDeclaringType().getFullyQualifiedName();

      // figure out if the fqn represents a local inner class
      while (iMember != null && !(iMember instanceof CompilationUnit)) {
        if (iMember instanceof SourceType
            && !((SourceType) iMember).isAnonymous()
            && iMember.getParent() instanceof SourceMethod) {

          int index = 1;
          try {
            ArrayList<SourceType> sources =
                ((SourceMethod) iMember.getParent()).getChildrenOfType(iMember.getElementType());

            for (int i = 0; i < sources.size(); i++, index++) {
              if (sources.get(i).getElementName().equals(iMember.getElementName())) {
                break;
              }
            }

          } catch (JavaModelException e) {
            throw new IllegalArgumentException(e);
          }

          // In case of local inner class the fqn should be altered
          // a$b -> a$1b, it will be the name of loaded class into VM
          fqn = fqn.replace("$" + iMember.getElementName(), "$" + index + iMember.getElementName());
        }

        iMember = iMember.getParent();
      }

      return fqn;
    }

    throw new IllegalArgumentException(
        format("Unable to find source code for '%s' in the project '%s'", fileUri, javaProject));
  }

  /**
   * Finds resources by a fqn.
   *
   * @param params contains fqn
   * @return all resources are identified by the given fqn
   */
  public static List<Either<String, ResourceLocation>> findResourcesByFqn(
      List<Object> params, IProgressMonitor progressMonitor) {
    Preconditions.checkArgument(params.size() >= 1, "FQN is expected.");

    ensureNotCancelled(progressMonitor);

    String fqn = (String) params.get(0);
    List<IJavaProject> projects = getWorkspaceJavaProjects();

    List<IType> types = new LinkedList<>();
    IType aType;
    for (IJavaProject javaProject : projects) {
      ensureNotCancelled(progressMonitor);

      try {
        aType = javaProject.findType(fqn);
        if (aType != null) {
          types.add(aType);
        }
      } catch (JavaModelException e) {
        // skip it and try with next project
      }
    }

    ensureNotCancelled(progressMonitor);

    List<Either<String, ResourceLocation>> result = new ArrayList<>(types.size());
    for (IType type : types) {
      if (type.isBinary()) {
        IClassFile classFile = type.getClassFile();
        String libId =
            classFile.getAncestor(IPackageFragmentRoot.PACKAGE_FRAGMENT_ROOT).getHandleIdentifier();
        result.add(Either.forRight(new ResourceLocation(fqn, libId)));
      } else {
        result.add(Either.forLeft(JDTUtils.toURI(type.getCompilationUnit())));
      }
    }

    return result;
  }

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
