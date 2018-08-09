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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateMavenModulesInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.ArtifactKey;
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
    IMavenProjectFacade mavenProject = event.getMavenProject();
    IMavenProjectFacade oldMavenProject = event.getOldMavenProject();

    if (mavenProject == null || oldMavenProject == null) {
      return;
    }
    try {
      if (isConfigurationUpdated(mavenProject, oldMavenProject)) {
        String projectUri = getNormalizedProjectPath(mavenProject);
        notifyClient(Commands.CLIENT_UPDATE_PROJECT_CONFIG, projectUri);
      } else if (hasModules(mavenProject, oldMavenProject)) {
        updateModules(mavenProject, oldMavenProject, monitor);
      }
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("An exception occurred while getting Maven project", e);
    }
  }

  private boolean hasModules(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    List<String> newModules = mavenProject.getMavenProjectModules();
    List<String> oldModules = oldMavenProject.getMavenProjectModules();

    return !newModules.isEmpty() && !oldModules.isEmpty();
  }

  private boolean isConfigurationUpdated(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    return !mavenProject
        .getArtifactKey()
        .getArtifactId()
        .equals(oldMavenProject.getArtifactKey().getArtifactId());
  }

  private void updateModules(
      IMavenProjectFacade mavenProject,
      IMavenProjectFacade oldMavenProject,
      IProgressMonitor monitor)
      throws CoreException {
    UpdateMavenModulesInfo updateInfo = new UpdateMavenModulesInfo();

    List<String> newModules = mavenProject.getMavenProjectModules();
    List<String> oldModules = oldMavenProject.getMavenProjectModules();

    for (String artifactId : newModules) {
      if (!oldModules.contains(artifactId)) {
        IMavenProjectFacade updatedModule =
            getUpdatedModule(artifactId, mavenProject.getArtifactKey(), monitor);
        if (updatedModule != null) {
          updateInfo.getCreated().add(getNormalizedProjectPath(updatedModule));
        }
      }
    }

    for (String artifactId : oldModules) {
      if (!newModules.contains(artifactId)) {
        IMavenProjectFacade updatedModule =
            getUpdatedModule(artifactId, mavenProject.getArtifactKey(), monitor);
        if (updatedModule != null) {
          updateInfo.getRemoved().add(getNormalizedProjectPath(updatedModule));
        }
      }
    }

    notifyClient(Commands.CLIENT_UPDATE_MAVEN_MODULE, updateInfo);
  }

  private IMavenProjectFacade getUpdatedModule(
      String artifactId, ArtifactKey parentArtifact, IProgressMonitor monitor)
      throws CoreException {
    IMavenProjectFacade[] projects = MavenPlugin.getMavenProjectRegistry().getProjects();
    for (IMavenProjectFacade project : projects) {
      if (!artifactId.equals(project.getArtifactKey().getArtifactId())) {
        continue;
      }
      MavenProject mavenProject = project.getMavenProject(monitor);
      if (mavenProject == null || !mavenProject.hasParent()) {
        continue;
      }

      Artifact artifact = mavenProject.getParent().getArtifact();
      if (parentArtifact.getArtifactId().equals(artifact.getArtifactId())
          && parentArtifact.getGroupId().equals(artifact.getGroupId())
          && parentArtifact.getVersion().equals(artifact.getVersion())) {
        return project;
      }
    }
    return null;
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
