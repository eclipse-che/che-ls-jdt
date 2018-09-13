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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

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

    IJavaProject jProject = JavaModelUtil.getJavaProject(projectUri);
    if (jProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
    }

    IResource lib = jProject.getProject().findMember(libFolder);
    if (lib == null || !lib.exists() || lib.getType() != IResource.FOLDER) {
      throw new IllegalArgumentException(format("Folder for '%s' not found", libFolder));
    }

    List<IClasspathEntry> classpath = new ArrayList<>();
    try {
      List<IClasspathEntry> jars = findJars((IFolder) lib);
      List<IClasspathEntry> iClasspathEntries = collectNonExistent(jProject, jars);
      classpath.addAll(iClasspathEntries);
      classpath.addAll(Arrays.asList(jProject.getRawClasspath()));
      jProject.setRawClasspath(
          classpath.toArray(new IClasspathEntry[classpath.size()]),
          jProject.getOutputLocation(),
          pm);
    } catch (CoreException e) {
      throw new IllegalArgumentException(format("Can't find libraries from '%s'", libFolder));
    }
    return projectUri;
  }

  private static List<IClasspathEntry> collectNonExistent(
      IJavaProject jProject, List<IClasspathEntry> jars) {
    return jars.stream()
        .filter(
            j -> {
              try {
                return jProject.getClasspathEntryFor(j.getPath()) == null;
              } catch (JavaModelException e) {
                return false;
              }
            })
        .collect(Collectors.toList());
  }

  private static List<IClasspathEntry> findJars(IFolder lib) throws CoreException {
    List<IClasspathEntry> jars = new ArrayList<>();
    lib.accept(
        iResource -> {
          if (IResource.FILE != iResource.getType()) {
            return true;
          }

          IPath path = iResource.getFullPath();
          if (!path.toString().endsWith(".jar")) {
            return false;
          }

          IClasspathEntry libEntry = newLibraryEntry(iResource.getLocation(), null, null);

          jars.add(libEntry);

          return false;
        });

    return jars;
  }
}
