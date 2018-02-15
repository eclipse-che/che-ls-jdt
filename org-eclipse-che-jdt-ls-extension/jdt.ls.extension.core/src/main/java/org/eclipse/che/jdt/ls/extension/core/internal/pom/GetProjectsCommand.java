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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;

/** @author Anatolii Bazko */
public class GetProjectsCommand {

  /**
   * Returns all nested projects starting from the given root.
   *
   * @param arguments the root uri
   */
  public static List<String> execute(List<Object> arguments, IProgressMonitor progressMonitor) {
    Preconditions.checkArgument(!arguments.isEmpty(), "Project uri is expected.");
    Utils.ensureNotCancelled(progressMonitor);

    String rootUri = (String) arguments.get(0);
    if (rootUri.endsWith("/")) {
      rootUri = rootUri.substring(0, rootUri.length() - 1);
    }

    List<String> projectsUris = new LinkedList<>();

    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      URI projectUri = project.getLocationURI();
      if (project.getLocation() != null && projectUri.toString().startsWith(rootUri)) {
        projectsUris.add(ResourceUtils.fixURI(projectUri));
      }
    }

    return projectsUris;
  }
}
