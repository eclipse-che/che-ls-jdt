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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateMavenModulesInfo;
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
      mavenProjectChanged(event);
    }
  }

  private void mavenProjectChanged(MavenProjectChangedEvent event) {
    IMavenProjectFacade mavenProject = event.getMavenProject();
    IMavenProjectFacade oldMavenProject = event.getOldMavenProject();

    if (mavenProject == null || oldMavenProject == null) {
      return;
    }
    if (isConfigurationUpdated(mavenProject, oldMavenProject)) {
      String projectUri = getNormalizedProjectPath(mavenProject);
      notifyClient(Commands.CLIENT_UPDATE_PROJECT_CONFIG, projectUri);
    } else if (!mavenProject
        .getMavenProjectModules()
        .equals(oldMavenProject.getMavenProjectModules())) {
      updateModules(mavenProject, oldMavenProject);
    }
  }

  private boolean isConfigurationUpdated(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    return !mavenProject
        .getArtifactKey()
        .getArtifactId()
        .equals(oldMavenProject.getArtifactKey().getArtifactId());
  }

  private void updateModules(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    UpdateMavenModulesInfo updateInfo = new UpdateMavenModulesInfo();
    updateInfo.setProjectUri(getNormalizedProjectPath(mavenProject));
    updateInfo.setAdded(findAddedModules(mavenProject, oldMavenProject));
    updateInfo.setRemoved(findRemovedModules(mavenProject, oldMavenProject));

    notifyClient(Commands.CLIENT_UPDATE_MAVEN_MODULE, updateInfo);
  }

  private List<String> findRemovedModules(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    mavenProject.getMavenProjectModules();
    List<String> newModules = mavenProject.getMavenProjectModules();
    List<String> oldModules = new ArrayList<>(oldMavenProject.getMavenProjectModules());

    oldModules.removeAll(newModules);

    return oldModules;
  }

  private List<String> findAddedModules(
      IMavenProjectFacade mavenProject, IMavenProjectFacade oldMavenProject) {
    List<String> newModules = new ArrayList<>(mavenProject.getMavenProjectModules());
    List<String> oldModules = oldMavenProject.getMavenProjectModules();

    newModules.removeAll(oldModules);

    return newModules;
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
