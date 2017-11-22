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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.jdt.ls.extension.api.dto.debug.LocationParameters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

import java.util.List;

/** @author Anatolii Bazko */
public class FqnConverter {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  public static List<String> locationToFqn(List<Object> parameters, IProgressMonitor pm) {
    LocationParameters location =
        gson.fromJson(gson.toJson(parameters.get(0)), LocationParameters.class);

    IPath path = Path.fromOSString(location.getTarget());
    IJavaProject project = getJavaProject(path);
    if (project == null) {
      if (location.getResourceProjectPath() != null) {
        project = MODEL.getJavaProject(location.getResourceProjectPath());
      } else {
        return location.getTarget();
      }
    }

    String fqn = null;
    for (int i = path.segmentCount(); i > 0; i--) {
      try {
        IClasspathEntry classpathEntry =
            ((JavaProject) project).getClasspathEntryFor(path.removeLastSegments(i));

        if (classpathEntry != null) {
          fqn =
              path.removeFirstSegments(path.segmentCount() - i)
                  .removeFileExtension()
                  .toString()
                  .replace("/", ".");
          break;
        }
      } catch (JavaModelException e) {
        return location.getTarget();
      }
    }

    if (fqn == null) {
      return location.getTarget();
    }

    IType outerClass;
    IJavaElement iMember;
    try {
      outerClass = project.findType(fqn);

      if (outerClass == null) {
        return location.getTarget();
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
      IRegion region = document.getLineInformation(location.getLineNumber());
      int start = region.getOffset();
      int end = start + region.getLength();

      iMember = binSearch(outerClass, start, end);
    } catch (JavaModelException e) {
      throw new DebuggerException(
          format(
              "Unable to find source for class with fqn '%s' in the project '%s'",
              location.getTarget(), project),
          e);
    } catch (BadLocationException e) {
      throw new DebuggerException("Unable to calculate breakpoint location", e);
    }

    if (iMember instanceof IType) {
      return ((IType) iMember).getFullyQualifiedName();
    }

    if (iMember != null) {
      fqn = ((IMember) iMember).getDeclaringType().getFullyQualifiedName();
      while (!(iMember instanceof CompilationUnit)) {
        if (iMember instanceof SourceType
            && !((SourceType) iMember).isAnonymous()
            && iMember.getParent() instanceof SourceMethod) {

          fqn = fqn.replace("$" + iMember.getElementName(), "$1" + iMember.getElementName());
        }

        iMember = iMember.getParent();
      }

      return fqn;
    }

    throw new DebuggerException("Unable to calculate breakpoint location");
  }

  public static List<String> fqnToLocation(List<Object> parameters, IProgressMonitor pm) {
    return null;
  }

  private static String extractOuterClassFqn(String fqn) {
    // handle fqn in case nested classes
    if (fqn.contains("$")) {
      return fqn.substring(0, fqn.indexOf("$"));
    }

    // handle fqn in case lambda expressions
    return fqn.contains("$$") ? fqn.substring(0, fqn.indexOf("$$")) : fqn;
  }

  private static IJavaProject getJavaProject(IPath path, JavaModel javaModel)
      throws JavaModelException {
    IJavaProject project = null;
    outer:
    for (int i = 1; i < path.segmentCount(); i++) {
      IPath projectPath = path.removeLastSegments(i);
      for (IJavaProject p : javaModel.getJavaProjects()) {
        if (p.getPath().equals(projectPath)) {
          project = p;
          break outer;
        }
      }
    }
    return project;
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
