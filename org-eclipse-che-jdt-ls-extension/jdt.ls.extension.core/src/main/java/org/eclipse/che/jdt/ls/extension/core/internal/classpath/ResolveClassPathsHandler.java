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
package org.eclipse.che.jdt.ls.extension.core.internal.classpath;

import static java.util.Collections.emptyList;
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;
import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Class for resolving class path and getting location of the output directory for a java project.
 */
public class ResolveClassPathsHandler {

  /**
   * Resolves class path for a java project.
   *
   * @param arguments a list contains project URI
   * @param pm a progress monitor
   * @return the class paths entries
   */
  public static List<String> resolveClasspaths(List<Object> arguments, IProgressMonitor pm) {
    String projectUri = (String) arguments.get(0);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return emptyList();
    }

    return getProjectClassPath(javaProject, pm);
  }

  /**
   * Gets output location for a java project.
   *
   * @param arguments a list contains project URI
   * @param pm a progress monitor
   * @return output location, might returns empty string if something happens
   */
  public static String getOutputDirectory(List<Object> arguments, IProgressMonitor pm) {
    String projectUri = (String) arguments.get(0);

    ensureNotCancelled(pm);

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
   * Returns classpath tree.
   *
   * @param params first parameter must be project URI
   * @param pm a progress monitor
   * @return list of classpath entries
   */
  public static List<ClasspathEntry> getClasspathModelTree(
      List<Object> params, IProgressMonitor pm) {
    String projectUri = (String) params.get(0);

    ensureNotCancelled(pm);

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return emptyList();
    }

    try {
      IClasspathEntry[] entries = javaProject.getRawClasspath();

      if (entries.length == 0) {
        return emptyList();
      }

      return convertClasspathEntriesToDTO(javaProject, entries);
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
  }

  private static List<ClasspathEntry> convertClasspathEntriesToDTO(
      IJavaProject javaProject, IClasspathEntry[] entries) throws JavaModelException {
    List<ClasspathEntry> entriesDTO = new ArrayList<>(entries.length);
    for (IClasspathEntry entry : entries) {
      ClasspathEntry entryDTO = new ClasspathEntry();
      entryDTO.setEntryKind(entry.getEntryKind());
      entryDTO.setPath(entry.getPath().toOSString());
      if (IClasspathEntry.CPE_CONTAINER == entry.getEntryKind()) {

        IClasspathEntry[] subEntries =
            JavaCore.getClasspathContainer(entry.getPath(), javaProject).getClasspathEntries();

        entryDTO.setChildren(convertClasspathEntriesToDTO(javaProject, subEntries));
      }
      entriesDTO.add(entryDTO);
    }

    return entriesDTO;
  }

  /**
   * Builds classpath for the java project.
   *
   * @param javaProject java project
   * @return set of resources which are included to the classpath
   */
  private static List<String> getProjectClassPath(IJavaProject javaProject, IProgressMonitor pm) {
    try {
      IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
      List<String> result = new LinkedList<>();
      for (IClasspathEntry classpathEntry : resolvedClasspath) {
        ensureNotCancelled(pm);

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
            result.addAll(getProjectClassPath(project, pm));
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
