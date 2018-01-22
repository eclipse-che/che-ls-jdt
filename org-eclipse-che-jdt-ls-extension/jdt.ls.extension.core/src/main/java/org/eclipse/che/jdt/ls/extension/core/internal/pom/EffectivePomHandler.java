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

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

/**
 * Command to generate effective pom for a maven project
 *
 * @author Mykola Morhun
 */
public class EffectivePomHandler {

  /**
   * Generates effective pom for project.
   *
   * @param arguments single argument with project uri
   * @param progressMonitor progress monitor
   * @return effective pom for given maven project
   */
  public static String getEffectivePom(List<Object> arguments, IProgressMonitor progressMonitor) {
    final String projectUri = (String) arguments.get(0);
    final String projectPomUri = projectUri + "/pom.xml";

    IFile pomFile = JDTUtils.findFile(projectPomUri);
    if (pomFile == null) {
      throw new IllegalArgumentException("Wrong path to project pom.xml: " + projectPomUri);
    }

    ensureNotCancelled(progressMonitor);

    IMavenProjectRegistry mavenProjectRegistry = MavenPlugin.getMavenProjectRegistry();
    IMavenProjectFacade mavenProjectFacade =
        mavenProjectRegistry.create(pomFile, true, progressMonitor);
    MavenProject mavenProject;
    try {
      mavenProject = mavenProjectFacade.getMavenProject(progressMonitor);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException(
          "Failed to retrieve maven project for path: '" + projectUri + '\'', e);
      throw new RuntimeException(e);
    }
    Model mavenProjectModel = mavenProject.getModel();

    ensureNotCancelled(progressMonitor);

    StringWriter stringWriter = new StringWriter();
    MavenXpp3Writer mavenXpp3Writer = new MavenXpp3Writer();
    try {
      mavenXpp3Writer.write(stringWriter, mavenProjectModel);
    } catch (IOException e) {
      JavaLanguageServerPlugin.logException(
          "Failed to get effective pom for project: '" + projectUri + '\'', e);
      throw new RuntimeException(e);
    }

    return stringWriter.toString();
  }
}
