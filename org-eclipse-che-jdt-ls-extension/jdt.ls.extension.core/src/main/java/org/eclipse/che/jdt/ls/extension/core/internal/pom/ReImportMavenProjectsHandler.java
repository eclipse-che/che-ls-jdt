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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/**
 * Command to update maven projects.
 *
 * @author Mykola Morhun
 */
public class ReImportMavenProjectsHandler {
  public static long REIMPORT_TIMEOUT = 60L;

  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  private static final PreferenceManager preferenceManager = new PreferenceManager();
  private static final ProjectsManager projectsManager = new ProjectsManager(preferenceManager);
  private static final IJobManager jobManager = Job.getJobManager();
  private static final JobChangedListener jobChangedListener = new JobChangedListener();
  private static final Lock lock = new ReentrantLock();

  private static CountDownLatch jobsToBeFinished;
  // name -> path
  private static Map<String, String> projectsToBeUpdated = new ConcurrentHashMap<>();
  // set of paths to updated projects; path -> ""
  private static Map<String, String> updatedProjectsPaths = new ConcurrentHashMap<>();

  /**
   * Updates given maven projects.
   *
   * @param arguments contains ReImportMavenProjectsCommandParameters in the first element
   * @param progressMonitor progress monitor
   * @return URIs of updated projects
   */
  public static List<String> reImportMavenProjects(
      List<Object> arguments, IProgressMonitor progressMonitor) {

    if (lock.tryLock()) {
      try {
        updatedProjectsPaths.clear();

        ReImportMavenProjectsCommandParameters parameters =
            gson.fromJson(
                gson.toJson(arguments.get(0)), ReImportMavenProjectsCommandParameters.class);

        ensureNotCancelled(progressMonitor);
        return new ArrayList<>(
            doReImportMavenProjects(parameters.getProjectsToUpdate(), progressMonitor));
      } catch (InterruptedException e) {
        throw new OperationCanceledException(e.getMessage());
      } finally {
        lock.unlock();
      }
    }

    throw new RuntimeException("Reimport is already in progress.");
  }

  // TODO this is temporary solution.
  // It shouldn't block response but send back projects to which a job was submitted.
  // Then progress should be send separately.
  // For details see https://github.com/eclipse/eclipse.jdt.ls/issues/404
  private static Set<String> doReImportMavenProjects(
      List<String> projectsUri, IProgressMonitor progressMonitor) throws InterruptedException {
    final List<IProject> projects = validateProjects(projectsUri, progressMonitor);
    jobManager.addJobChangeListener(jobChangedListener);
    try {
      jobsToBeFinished = new CountDownLatch(projects.size());
      submitUpdateJobs(projects);
      jobsToBeFinished.await(REIMPORT_TIMEOUT, TimeUnit.SECONDS);
    } finally {
      jobManager.removeJobChangeListener(jobChangedListener);
    }
    return updatedProjectsPaths.keySet();
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
   * Submits jobs to update given maven projects.
   *
   * @param projects list of projects to be updated
   */
  private static void submitUpdateJobs(List<IProject> projects) {
    for (IProject project : projects) {
      projectsManager.updateProject(project);
    }
  }

  private static void ensureNotCancelled(IProgressMonitor progressMonitor) {
    if (progressMonitor.isCanceled()) {
      throw new OperationCanceledException();
    }
  }

  // TODO temporary solution.
  // Should be reworked when https://github.com/eclipse/eclipse.jdt.ls/issues/506 will be fixed.
  private static class JobChangedListener extends JobChangeAdapter {
    private static final String UPDATE_PROJECT_JOB_NAME_PREFIX = "Update project ";

    /** Listens to jobs changes events to get information about updated projects. */
    @Override
    public void done(IJobChangeEvent event) {
      String jobName = event.getJob().getName();
      if (jobName.startsWith(UPDATE_PROJECT_JOB_NAME_PREFIX)) {
        String projectName = jobName.substring(UPDATE_PROJECT_JOB_NAME_PREFIX.length());
        if (projectsToBeUpdated.containsKey(projectName)) {
          if (event.getResult().isOK()) {
            updatedProjectsPaths.put(projectsToBeUpdated.remove(projectName), "");
          } else {
            projectsToBeUpdated.remove(projectName);
          }

          jobsToBeFinished.countDown();
        }
      }
    }
  }
}
