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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static java.lang.String.format;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.PATH_SEPARATOR;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Adds all jars from library folder into project's classpath.
 *
 * @author Valeriy Svydenko
 */
public class AddJarsCommand {
  /**
   * Adds jars into classpath.
   *
   * @param arguments contains two arguments: project uri and library folder's name
   * @param pm a progress monitor
   * @return uri of the project
   */
  public static Object execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(
        arguments.size() >= 2, "Project uri and library folder are expected");

    final String projectUri = (String) arguments.get(0);
    final String libFolder = (String) arguments.get(1);

    String projectName = projectUri.substring(projectUri.lastIndexOf(PATH_SEPARATOR) + 1);

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    if (project == null || !project.exists()) {
      throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
    }

    IJavaProject jProject = JavaModelUtil.getJavaProject(projectUri);
    if (jProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
    }

    IFolder lib = project.getFolder(libFolder);
    if (!lib.exists()) {
      throw new IllegalArgumentException(format("Folder for '%s' not found", libFolder));
    }

    List<IClasspathEntry> resolvedClasspath = new ArrayList<>();
    try {
      addJars(jProject, lib, resolvedClasspath);
      IClasspathEntry[] rawClasspath = jProject.getRawClasspath();
      resolvedClasspath.addAll(Arrays.asList(rawClasspath));
      jProject.setRawClasspath(
          resolvedClasspath.toArray(new IClasspathEntry[resolvedClasspath.size()]),
          jProject.getOutputLocation(),
          pm);
    } catch (CoreException e) {
      throw new IllegalArgumentException(
          format("Can't read folder structure for: '%s'", libFolder));
    }
    return projectUri;
  }

  private static void addJars(
      IJavaProject jProject, IFolder lib, List<IClasspathEntry> resolvedClasspath)
      throws CoreException {
    lib.accept(
        proxy -> {
          if (IResource.FILE != proxy.getType()) {
            return true;
          }

          IPath path = proxy.requestFullPath();
          if (!path.toString().endsWith(".jar")) {
            return false;
          }

          IClasspathEntry libEntry =
              newLibraryEntry(proxy.requestResource().getLocation(), null, null);

          if (jProject.getClasspathEntryFor(libEntry.getPath()) == null) {
            resolvedClasspath.add(libEntry);
          }

          return false;
        },
        IContainer.INCLUDE_PHANTOMS);
  }
}
