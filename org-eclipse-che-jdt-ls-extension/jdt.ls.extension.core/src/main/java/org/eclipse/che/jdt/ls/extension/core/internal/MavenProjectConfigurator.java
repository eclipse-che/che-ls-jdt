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
package org.eclipse.che.jdt.ls.extension.core.internal;

import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;

/**
 * Listens to project configuration changes and notify client if it happened.
 *
 * @author tolusha
 */
public class MavenProjectConfigurator implements IMavenProjectChangedListener {

  public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor) {
    for (int i = 0; i < events.length; i++) {
      mavenProjectChanged(events[i]);
    }
  }

  private void mavenProjectChanged(MavenProjectChangedEvent event) {
    if (isConfigurationUpdated(event.getMavenProject(), event.getOldMavenProject())) {
      String projectUri = getNormalizedProjectPath(event.getMavenProject());
      notifyClient(projectUri);
    }
  }

  private boolean isConfigurationUpdated(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    return mavenProject != null
        && oldMavenProject != null
        && !mavenProject
            .getArtifactKey()
            .getArtifactId()
            .equals(oldMavenProject.getArtifactKey().getArtifactId());
  }

  private void notifyClient(String projectUri) {
    try {
      JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
      ls.getClientConnection()
          .executeClientCommand(Commands.CLIENT_UPDATE_PROJECT_CONFIG, projectUri);
    } catch (Exception e) {
      JavaLanguageServerPlugin.logException(
          "An exception occurred while reporting project updating", e);
    }
  }

  private String getNormalizedProjectPath(IMavenProjectFacade project) {
    String projectUri = ResourceUtils.fixURI(project.getMavenProject().getBasedir().toURI());
    if (projectUri.endsWith("/")) {
      return projectUri.substring(0, projectUri.length() - 1);
    }

    return projectUri;
  }
}
