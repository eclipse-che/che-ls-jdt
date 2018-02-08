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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

/** @author Anatolii Bazko */
public class GetMavenProjectsCommand {

  /**
   * Returns all nested maven projects starting from the given root.
   *
   * @param arguments the root uri
   */
  public static List<String> execute(List<Object> arguments, IProgressMonitor progressMonitor) {
    Preconditions.checkArgument(!arguments.isEmpty(), "Project uri is expected.");
    Utils.ensureNotCancelled(progressMonitor);

    final String rootUri = (String) arguments.get(0);
    final IPath rootPath = ResourceUtils.filePathFromURI(rootUri);
    if (rootPath == null) {
      return Collections.emptyList();
    }

    IMavenProjectRegistry mavenProjectRegistry = MavenPlugin.getMavenProjectRegistry();
    IMavenProjectFacade[] mavenProjects = mavenProjectRegistry.getProjects();

    List<String> projectsUri = new ArrayList<>(mavenProjects.length);
    for (int i = 0; i < mavenProjects.length; i++) {
      IProject project = mavenProjects[i].getProject();
      if (project != null && rootPath.isPrefixOf(project.getLocation())) {
        projectsUri.add(ResourceUtils.fixURI(project.getLocationURI()));
      }
    }

    return projectsUri;
  }
}
