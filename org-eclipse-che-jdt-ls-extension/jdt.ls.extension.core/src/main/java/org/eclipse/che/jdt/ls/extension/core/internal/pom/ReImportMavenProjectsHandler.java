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

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;

/**
 * Command to update maven projects.
 *
 * @author Mykola Morhun
 */
public class ReImportMavenProjectsHandler {
  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Updates given maven projects.
   *
   * @param arguments contains ReImportMavenProjectsCommandParameters in the first element
   * @param progressMonitor progress monitor
   * @return URIs of updated projects
   */
  public static List<String> reImportMavenProjects(
      List<Object> arguments, IProgressMonitor progressMonitor) {

    ReImportMavenProjectsCommandParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), ReImportMavenProjectsCommandParameters.class);

    ensureNotCancelled(progressMonitor);
    final Map<String, IProject> projects =
        validateProjects(parameters.getProjectsToUpdate(), progressMonitor);
    updateProjects(projects);

    return parameters.getProjectsToUpdate();
  }

  /**
   * Filters invalid projects (without pom) and generates list of projects to process.
   *
   * @param projectsUri list of URIs to projects which should be updated
   * @param progressMonitor progress monitor
   * @return map where the key is project's uri and the value valid project to reimport
   */
  private static Map<String, IProject> validateProjects(
      List<String> projectsUri, IProgressMonitor progressMonitor) {
    Map<String, IProject> projectsToUpdate = new HashMap<>(projectsUri.size());
    IFile pomFile;
    for (String pathToProject : projectsUri) {
      ensureNotCancelled(progressMonitor);

      pomFile = JDTUtils.findFile(pathToProject + "/pom.xml");
      if (pomFile == null) {
        continue;
      }

      projectsToUpdate.put(pathToProject, pomFile.getProject());
    }

    return projectsToUpdate;
  }

  /**
   * Updates maven projects.
   *
   * @param projects projects to be updated
   * @return list of jobs to update projects. It is needed for tests
   */
  public static List<Job> updateProjects(Map<String, IProject> projects) {
    List<Job> updatedJobs = new ArrayList<>(projects.size());
    for (String uri : projects.keySet()) {
      Job job =
          JavaLanguageServerPlugin.getProjectsManager().updateProject(projects.get(uri), true);
      job.addJobChangeListener(new JobChangedListener(uri));
      updatedJobs.add(job);
    }
    return updatedJobs;
  }

  private static class JobChangedListener extends JobChangeAdapter {
    private String projectUri;

    JobChangedListener(String projectUri) {
      this.projectUri = projectUri;
    }

    @Override
    public void done(IJobChangeEvent event) {
      if (!event.getResult().isOK()) {
        return;
      }
      try {
        JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
        ls.getClientConnection().executeClientCommand(Commands.CLIENT_UPDATE_PROJECT, projectUri);
      } catch (Exception e) {
        JavaLanguageServerPlugin.logException(
            "An exception occured while reporting project updating", e);
      }
    }
  }
}
