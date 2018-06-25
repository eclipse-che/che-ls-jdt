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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static java.lang.String.format;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_CONTAINER;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_LIBRARY;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_PROJECT;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_SOURCE;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_VARIABLE;
import static org.eclipse.jdt.core.JavaCore.newContainerEntry;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.jdt.core.JavaCore.newProjectEntry;
import static org.eclipse.jdt.core.JavaCore.newSourceEntry;
import static org.eclipse.jdt.core.JavaCore.newVariableEntry;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateClasspathParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The command for updating .classpath for simple java project.
 *
 * @author Valeriy Svydenko
 */
public class UpdateProjectClasspathCommand {
  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Updates .classpath in the given simple java project.
   *
   * @param arguments contains one argument: information about classpath {@link
   *     UpdateClasspathParameters}}
   * @param pm a progress monitor
   * @return uri of updated project
   */
  public static Object execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(arguments.size() >= 1, "Information about .classpath is expected");

    UpdateClasspathParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), UpdateClasspathParameters.class);

    final String projectUri = parameters.getProjectUri();
    final List<ClasspathEntry> entries = parameters.getEntries();

    IJavaProject jProject = JavaModelUtil.getJavaProject(projectUri);

    if (jProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
    }

    try {
      jProject.setRawClasspath(createModifiedEntry(entries), jProject.getOutputLocation(), pm);
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

    return projectUri;
  }

  private static IClasspathEntry[] createModifiedEntry(List<ClasspathEntry> entries) {
    List<IClasspathEntry> coreClasspathEntries = new ArrayList<>(entries.size());
    for (ClasspathEntry entry : entries) {
      String uri = entry.getPath();
      try {
        switch (entry.getEntryKind()) {
          case CPE_LIBRARY:
            {
              String absolutePath = new File(new URI(uri)).getAbsolutePath();
              coreClasspathEntries.add(
                  newLibraryEntry(Path.fromOSString(absolutePath), null, null));
              break;
            }
          case CPE_SOURCE:
            {
              coreClasspathEntries.add(newSourceEntry(getWSPathForContainer(uri)));
              break;
            }
          case CPE_VARIABLE:
            coreClasspathEntries.add(newVariableEntry(Path.fromOSString(uri), null, null));
            break;
          case CPE_CONTAINER:
            coreClasspathEntries.add(newContainerEntry(Path.fromOSString(uri)));
            break;
          case CPE_PROJECT:
            coreClasspathEntries.add(newProjectEntry(getWSPathForContainer(uri)));
            break;
        }
      } catch (URISyntaxException e1) {
        throw new IllegalArgumentException("could not parse URI " + uri);
      }
    }
    return coreClasspathEntries.toArray(new IClasspathEntry[coreClasspathEntries.size()]);
  }

  private static IPath getWSPathForContainer(String uri) throws URISyntaxException {
    IContainer[] folders =
        ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(new URI(uri));
    if (folders.length == 0) {
      throw new IllegalArgumentException("no folders found for " + uri);
    }
    return folders[0].getFullPath();
  }
}
