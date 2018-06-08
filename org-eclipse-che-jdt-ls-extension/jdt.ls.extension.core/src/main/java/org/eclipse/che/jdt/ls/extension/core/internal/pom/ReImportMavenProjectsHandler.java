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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  // name -> path
  private static Map<String, String> projectsToBeUpdated = new ConcurrentHashMap<>();

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
    doReImportMavenProjects(parameters.getProjectsToUpdate(), progressMonitor);

    return parameters.getProjectsToUpdate();
  }

  private static void doReImportMavenProjects(
      List<String> projectsUri, IProgressMonitor progressMonitor) {
    final List<IProject> projects = validateProjects(projectsUri, progressMonitor);
    updateProjects(projects);
  }

  /**
   * Filters invalid projects (without pom) and generates list of projects to process.
   *
   * @param projectsUri list of URIs to projects which should be updated
   * @param progressMonitor progress monitor
   * @return list of valid projects to reimport
   */
  private static List<IProject> validateProjects(
      List<String> projectsUri, IProgressMonitor progressMonitor) {
    projectsToBeUpdated.clear();

    List<IProject> projectsToUpdate = new ArrayList<>(projectsUri.size());
    IFile pomFile;
    for (String pathToProject : projectsUri) {
      ensureNotCancelled(progressMonitor);

      pomFile = JDTUtils.findFile(pathToProject + "/pom.xml");
      if (pomFile == null) {
        continue;
      }

      projectsToUpdate.add(pomFile.getProject());
      projectsToBeUpdated.put(pomFile.getProject().getName(), pathToProject);
    }

    return projectsToUpdate;
  }

  /**
   * Updates maven projects.
   *
   * @param projects list of projects to be updated
   * @return list of jobs to update projects. It is needed for tests
   */
  public static List<Job> updateProjects(List<IProject> projects) {
    List<Job> updatedJobs = new ArrayList<>();
    for (IProject project : projects) {
      Job job = JavaLanguageServerPlugin.getProjectsManager().updateProject(project, true);
      job.addJobChangeListener(new JobChangedListener());
      updatedJobs.add(job);
    }
    return updatedJobs;
  }

  private static class JobChangedListener extends JobChangeAdapter {
    private static final String UPDATE_PROJECT_JOB_NAME_PREFIX = "Update project ";

    /** Listens to jobs changes events to get information about updated projects. */
    @Override
    public void done(IJobChangeEvent event) {
      String jobName = event.getJob().getName();
      if (!jobName.startsWith(UPDATE_PROJECT_JOB_NAME_PREFIX)) {
        return;
      }
      String projectName = jobName.substring(UPDATE_PROJECT_JOB_NAME_PREFIX.length());
      if (!event.getResult().isOK()) {
        return;
      }
      String projectPath = projectsToBeUpdated.get(projectName);
      try {
        JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
        ls.getClientConnection().executeClientCommand(Commands.CLIENT_UPDATE_PROJECT, projectPath);
      } catch (Exception e) {
        JavaLanguageServerPlugin.logException(
            "An exception occured while reporting project updating", e);
      }
    }
  }
}
