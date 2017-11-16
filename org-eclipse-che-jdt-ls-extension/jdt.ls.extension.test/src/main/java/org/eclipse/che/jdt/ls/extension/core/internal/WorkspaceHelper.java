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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

public final class WorkspaceHelper {

  private WorkspaceHelper() {
    // No instances allowed
  }

  public static void initWorkspace() throws CoreException {
    JavaLanguageServerPlugin.getProjectsManager()
        .initializeProjects(Collections.emptyList(), new NullProgressMonitor());
    assertEquals(1, getAllProjects().size());
  }

  public static IProject getProject(String name) {
    IProject project = getWorkspaceRoot().getProject(name);
    return project.exists() ? project : null;
  }

  public static void deleteAllProjects() {
    getAllProjects().forEach(WorkspaceHelper::delete);
  }

  public static List<IProject> getAllProjects() {
    return asList(getWorkspaceRoot().getProjects());
  }

  public static IWorkspaceRoot getWorkspaceRoot() {
    return ResourcesPlugin.getWorkspace().getRoot();
  }

  public static void delete(IProject project) {
    try {
      project.delete(true, new NullProgressMonitor());
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
}
