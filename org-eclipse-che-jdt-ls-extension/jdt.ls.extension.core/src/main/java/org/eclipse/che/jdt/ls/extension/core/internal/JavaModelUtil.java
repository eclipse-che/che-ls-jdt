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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
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
