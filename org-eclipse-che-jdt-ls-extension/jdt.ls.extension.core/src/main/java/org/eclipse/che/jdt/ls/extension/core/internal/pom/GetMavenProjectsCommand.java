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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import com.google.common.base.Preconditions;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

/** @author Anatolii Bazko */
public class GetMavenProjectsCommand {

  /**
   * Returns all nested projects starting from the given root.
   *
   * @param arguments the root uri
   */
  public static List<String> execute(List<Object> arguments, IProgressMonitor progressMonitor) {
    Preconditions.checkArgument(!arguments.isEmpty(), "Project uri is expected.");
    Utils.ensureNotCancelled(progressMonitor);

    final String rootUri = (String) arguments.get(0);
    final IPath rootPath = ResourceUtils.filePathFromURI(rootUri);

    List<String> projectsUris = new LinkedList<>();

    for (IMavenProjectFacade mavenProject : MavenPlugin.getMavenProjectRegistry().getProjects()) {
      IProject project = mavenProject.getProject();
      if (project == null || project.getLocation() == null) {
        continue;
      }

      if (rootPath.isPrefixOf(project.getLocation())) {
        projectsUris.add(ResourceUtils.fixURI(project.getLocationURI()));
      }
    }

    return projectsUris;
  }
}
