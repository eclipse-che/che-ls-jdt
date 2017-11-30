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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.eclipse.che.jdt.ls.extension.api.dto.GetEffectivePomParameters;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;

/** @author Mykola Morhun */
public class EffectivePomHandler {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  public static String getEffectivePom(List<Object> arguments, IProgressMonitor progressMonitor) {
    GetEffectivePomParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), GetEffectivePomParameters.class);

    String projectPath = parameters.getPathToProjectPom();
    IFile pomFile = JDTUtils.findFile("file://" + projectPath);
    if (pomFile == null) {
      throw new IllegalArgumentException("Wrong path to project pom.xml: " + projectPath);
    }

    IMavenProjectRegistry mavenProjectRegistry = MavenPlugin.getMavenProjectRegistry();
    IMavenProjectFacade mavenProjectFacade =
        mavenProjectRegistry.create(pomFile, true, progressMonitor);
    MavenProject mavenProject;
    try {
      mavenProject = mavenProjectFacade.getMavenProject(progressMonitor);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException(
          "Failed to retrieve maven project for path: '" + projectPath + '\'', e);
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
          "Failed to get effective pom for project: '" + projectPath + '\'', e);
      throw new RuntimeException(e);
    }

    return stringWriter.toString();
  }

  private static void ensureNotCancelled(IProgressMonitor progressMonitor) {
    if (progressMonitor.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
