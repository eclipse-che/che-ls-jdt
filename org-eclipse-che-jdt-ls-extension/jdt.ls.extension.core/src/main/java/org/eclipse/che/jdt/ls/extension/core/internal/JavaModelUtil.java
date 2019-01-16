/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolKind;

/** Utilities for working with JDT APIs */
public class JavaModelUtil {
  public static final String JDT_LS_JAVA_PROJECT = "jdt.ls-java-project";

  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Finds java project {@link IJavaProject} by URI
   *
   * @param resourceUri URI of the project source
   * @return instance of {@link IJavaProject}, may return null if it can not associate the uri with
   *     a Java project
   */
  public static IJavaProject getJavaProject(String resourceUri) {
    IResource resource =
        JDTUtils.findResource(
            JDTUtils.toURI(resourceUri),
            ResourcesPlugin.getWorkspace().getRoot()::findContainersForLocationURI);

    return resource != null ? JavaCore.create(resource.getProject()) : null;
  }

  public static String getFolderLocation(IPath folderPath) {
    return ResourceUtils.fixURI(
        ResourcesPlugin.getWorkspace().getRoot().getFolder(folderPath).getLocationURI());
  }

  public static String getResourceLocation(IPath folderPath) {
    IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(folderPath);
    if (member != null && member.exists()) {
      return ResourceUtils.fixURI(member.getLocationURI());
    } else {
      return null;
    }
  }

  /**
   * Returns all user created java project which exist in current workspace. This method excludes
   * default {@code jdt.ls-java-project} project.
   *
   * @return all user created java projects in current workspace
   */
  public static List<IJavaProject> getWorkspaceJavaProjects() {
    return Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
        .filter(project -> !JDT_LS_JAVA_PROJECT.equals(project.getName()))
        .map(project -> JavaCore.create((project)))
        .collect(Collectors.toList());
  }

  public static <T> T convertCommandParameter(Object param, Class<T> clazz) {
    return gson.fromJson(gson.toJson(param), clazz);
  }

  static SymbolKind mapKind(IJavaElement element) {
    if (element.getElementType() == IJavaElement.METHOD) {
      // workaround for https://github.com/eclipse/eclipse.jdt.ls/issues/422
      return SymbolKind.Method;
    }
    return DocumentSymbolHandler.mapKind(element);
  }

  /**
   * Creates a location for a given java element. Element can be a {@link ICompilationUnit} or
   * {@link IClassFile}
   *
   * @param element java element
   * @return location or null
   */
  public static Location toLocation(IJavaElement element) throws JavaModelException {
    ICompilationUnit unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
    IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
    if (unit == null && cf == null) {
      return null;
    }
    if (element instanceof ISourceReference) {
      ISourceRange nameRange = getNameRange(element);
      int offset = 0;
      int length = 0;
      if (SourceRange.isAvailable(nameRange)) {
        offset = nameRange.getOffset();
        length = nameRange.getLength();
      }
      if (cf != null) {
        return JDTUtils.toLocation(cf, offset, length);
      } else {
        return JDTUtils.toLocation(unit, offset, length);
      }
    }
    return null;
  }

  private static ISourceRange getNameRange(IJavaElement element) throws JavaModelException {
    ISourceRange nameRange = null;
    if (element instanceof IMember) {
      IMember member = (IMember) element;
      nameRange = member.getNameRange();
      if ((!SourceRange.isAvailable(nameRange))) {
        nameRange = member.getSourceRange();
      }
    } else if (element instanceof ITypeParameter || element instanceof ILocalVariable) {
      nameRange = ((ISourceReference) element).getNameRange();
    } else if (element instanceof ISourceReference) {
      nameRange = ((ISourceReference) element).getSourceRange();
    }
    if (!SourceRange.isAvailable(nameRange) && element.getParent() != null) {
      nameRange = getNameRange(element.getParent());
    }
    return nameRange;
  }

  public static IJavaElement getJavaElement(Position position, String uri, IProgressMonitor pm)
      throws CoreException {
    ICompilationUnit unit = JDTUtils.resolveCompilationUnit(uri);
    IJavaElement[] elements =
        JDTUtils.findElementsAtSelection(
            unit,
            position.getLine(),
            position.getCharacter(),
            JavaLanguageServerPlugin.getPreferencesManager(),
            pm);
    if (elements == null || elements.length == 0) {
      return null;
    }
    IJavaElement element = null;
    if (elements.length != 1) {
      IPackageFragment packageFragment = (IPackageFragment) unit.getParent();
      IJavaElement found =
          Stream.of(elements).filter(e -> e.equals(packageFragment)).findFirst().orElse(null);
      if (found == null) {
        // this would be a binary package fragment
        element = elements[0];
      } else {
        element = found;
      }
    } else {
      element = elements[0];
    }
    return element;
  }
}
