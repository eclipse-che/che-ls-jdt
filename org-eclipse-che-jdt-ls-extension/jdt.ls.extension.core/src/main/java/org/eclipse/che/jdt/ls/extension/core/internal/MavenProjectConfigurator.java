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

import java.util.List;
import org.apache.maven.project.MavenProject;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.dto.MavenProjectUpdateInfo;
import org.eclipse.core.runtime.CoreException;
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
    for (MavenProjectChangedEvent event : events) {
      mavenProjectChanged(event, monitor);
    }
  }

  private void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) {
    if (isConfigurationUpdated(event.getMavenProject(), event.getOldMavenProject())) {
      String projectUri = getNormalizedProjectPath(event.getMavenProject());
      notifyClient(Commands.CLIENT_UPDATE_PROJECT_CONFIG, projectUri);
    }
    try {
      updateModules(event, monitor);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("An exception occurred while getting Maven project", e);
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

  private void updateModules(MavenProjectChangedEvent event, IProgressMonitor monitor)
      throws CoreException {
    IMavenProjectFacade updatedProject = event.getMavenProject();
    if (updatedProject == null) {
      return;
    }
    MavenProject mavenProject = updatedProject.getMavenProject(monitor);
    if (mavenProject == null) {
      return;
    }

    MavenProject parent = mavenProject.getParent();
    if (parent == null) {
      return;
    }

    String projectUri = getNormalizedProjectPath(updatedProject);

    List<String> modules = parent.getModules();
    MavenProjectUpdateInfo updateInfo = new MavenProjectUpdateInfo();
    updateInfo.setProjectUri(projectUri);
    updateInfo.setCreated(modules.contains(updatedProject.getArtifactKey().getArtifactId()));

    notifyClient(Commands.CLIENT_UPDATE_MAVEN_MODULE, updateInfo);
  }

  private void notifyClient(String commandId, Object parameters) {
    try {
      JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
      ls.getClientConnection().executeClientCommand(commandId, parameters);
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
