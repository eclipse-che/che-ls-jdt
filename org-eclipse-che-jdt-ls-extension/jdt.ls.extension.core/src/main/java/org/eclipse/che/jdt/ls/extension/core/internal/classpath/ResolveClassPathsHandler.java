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
package org.eclipse.che.jdt.ls.extension.core.internal.classpath;

import static java.util.Collections.emptyList;
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Class for resolving class path and getting location of the output directory for a java project.
 */
public class ResolveClassPathsHandler {

  /**
   * Resolves class path for a java project.
   *
   * @param arguments a list contains project URI
   * @return the class paths entries
   */
  public static List<String> resolveClasspaths(List<Object> arguments) {
    String projectUri = (String) arguments.get(0);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return emptyList();
    }

    return getProjectClassPath(javaProject);
  }

  /**
   * Gets output location for a java project.
   *
   * @param arguments a list contains project URI
   * @return output location, might returns empty string if something happens
   */
  public static String getOutputDirectory(List<Object> arguments) {
    String projectUri = (String) arguments.get(0);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return "";
    }

    try {
      IPath outputLocation = javaProject.getOutputLocation();
      IPath outputFolderLocation = getOutputFolderLocation(outputLocation);
      return outputFolderLocation == null ? "" : outputFolderLocation.toOSString();
    } catch (JavaModelException e) {
      return "";
    }
  }

  /**
   * Builds classpath for the java project.
   *
   * @param javaProject java project
   * @return set of resources which are included to the classpath
   */
  private static List<String> getProjectClassPath(IJavaProject javaProject) {
    try {
      IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
      List<String> result = new LinkedList<>();
      for (IClasspathEntry classpathEntry : resolvedClasspath) {
        switch (classpathEntry.getEntryKind()) {
          case IClasspathEntry.CPE_LIBRARY:
            IPath path = classpathEntry.getPath();
            result.add(path.toOSString());
            break;

          case IClasspathEntry.CPE_SOURCE:
            IPath outputLocation = classpathEntry.getOutputLocation();
            IPath outputFolderLocation = getOutputFolderLocation(outputLocation);

            if (outputFolderLocation != null) {
              result.add(outputFolderLocation.toOSString());
            }

            break;
          case IClasspathEntry.CPE_PROJECT:
            IPath projectPath = classpathEntry.getPath();
            IJavaProject project = getJavaProject(projectPath.toOSString());
            if (project == null) {
              break;
            }
            result.addAll(getProjectClassPath(project));
            break;
        }
      }
      return result;
    } catch (JavaModelException e) {
      return emptyList();
    }
  }

  private static IPath getOutputFolderLocation(IPath path) {
    if (path == null) {
      return null;
    }
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IFolder folder = root.getFolder(path);
    if (folder == null || !folder.exists()) {
      return null;
    }
    return folder.getLocation();
  }
}
