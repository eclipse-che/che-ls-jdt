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

import org.eclipse.che.jdt.ls.extension.api.Notifications;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
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
    if (event.getKind() == MavenProjectChangedEvent.KIND_ADDED) {
      IProject project = event.getMavenProject().getProject();
      notifyClient(
          Notifications.MAVEN_PROJECT_CREATED, ResourceUtils.fixURI(project.getLocationURI()));
    }
  }

  @SuppressWarnings("restriction")
  private void notifyClient(String commandId, Object parameters) {
    JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
    ls.getClientConnection().sendNotification(commandId, parameters);
  }
}
