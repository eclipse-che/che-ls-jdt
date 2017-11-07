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

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;

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
  public Set<String> resolveClasspaths(List<Object> arguments) {
    String projectUri = (String) arguments.get(0);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return emptySet();
    }

    return getProjectClassPath(javaProject);
  }

  /**
   * Gets output location for a java project.
   *
   * @param arguments a list contains project URI
   * @return output location, might returns empty string if something happens
   */
  public String getOutputDirectory(List<Object> arguments) {
    String projectUri = (String) arguments.get(0);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return "";
    }

    try {
      IPath outputLocation = javaProject.getOutputLocation();
      IPath projectPath = javaProject.getProject().getFullPath();
      IPath outputLocationWithoutProjectName =
          outputLocation.removeFirstSegments(projectPath.segmentCount());
      IFolder outputFolder = javaProject.getProject().getFolder(outputLocationWithoutProjectName);
      if (outputFolder.exists()) {
        return outputFolder.getLocation().toOSString();
      }
      return "";
    } catch (JavaModelException e) {
      return "";
    }
  }

  private IJavaProject getJavaProject(String projectUri) {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IContainer[] containers = root.findContainersForLocationURI(JDTUtils.toURI(projectUri));

    if (containers.length == 0) {
      return null;
    }

    IContainer container = containers[0];
    IProject project = container.getProject();
    if (!project.exists()) {
      return null;
    }

    return JavaCore.create(project);
  }

  /**
   * Builds classpath for the java project.
   *
   * @param javaProject java project
   * @return set of resources which are included to the classpath
   */
  private Set<String> getProjectClassPath(IJavaProject javaProject) {
    try {
      IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
      Set<String> result = new HashSet<>();
      for (IClasspathEntry classpathEntry : resolvedClasspath) {
        switch (classpathEntry.getEntryKind()) {
          case IClasspathEntry.CPE_LIBRARY:
            IPath path = classpathEntry.getPath();
            result.add(path.toOSString());
            break;

          case IClasspathEntry.CPE_SOURCE:
            IPath outputLocation = classpathEntry.getOutputLocation();
            if (outputLocation != null) {
              IPath projectPath = javaProject.getProject().getFullPath();
              IPath outputLocationWithoutProjectName =
                  outputLocation.removeFirstSegments(projectPath.segmentCount());
              IFolder outputFolder =
                  javaProject.getProject().getFolder(outputLocationWithoutProjectName);
              if (outputFolder.exists()) {
                result.add(outputFolder.getLocation().toOSString());
              }
            }
            break;

          case IClasspathEntry.CPE_PROJECT:
            IPath projectPath = classpathEntry.getPath();
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IContainer[] containers =
                root.findContainersForLocationURI(JDTUtils.toURI(projectPath.toOSString()));
            IContainer container = containers[0];
            if (!container.exists()) {
              break;
            }
            IJavaProject project = JavaCore.create(container.getProject());
            result.addAll(getProjectClassPath(project));
            break;
        }
      }
      return result;
    } catch (JavaModelException e) {
      return emptySet();
    }
  }
}
