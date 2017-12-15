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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsResult;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.registry.MavenProjectManager;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;

/** @author Mykola Morhun */
public class ReImportMavenProjectsHandler {

  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  private static final Lock lock = new ReentrantLock();
  private static final MavenProjectChangedListener mavenProjectChangedListener =
      new MavenProjectChangedListener();

  private static Set<String> updatedProjects = new HashSet<>();
  private static Set<String> addedProjects = new HashSet<>();
  private static Set<String> removedProjects = new HashSet<>();

  /**
   * Updates given maven projects.
   *
   * @param arguments contains ReImportMavenProjectsCommandParameters in the first element
   * @param progressMonitor progress monitor
   * @return paths of updated, added and removed projects
   */
  public static ReImportMavenProjectsResult reImportMavenProjects(
      List<Object> arguments, IProgressMonitor progressMonitor) {

    if (lock.tryLock()) {
      try {
        updatedProjects.clear();
        addedProjects.clear();
        removedProjects.clear();

        ReImportMavenProjectsCommandParameters parameters =
            gson.fromJson(
                gson.toJson(arguments.get(0)), ReImportMavenProjectsCommandParameters.class);

        ensureNotCancelled(progressMonitor);
        doReImportMavenProjects(parameters.getProjectsToUpdate(), progressMonitor);

        return new ReImportMavenProjectsResult()
            .withUpdatedProjects(new ArrayList<>(updatedProjects))
            .withAddedProjects(new ArrayList<>(addedProjects))
            .withRemovedProjects(new ArrayList<>(removedProjects));
      } finally {
        lock.unlock();
      }
    }

    throw new RuntimeException("Reimport is already in progress.");
  }

  /**
   * Updates given maven projects.
   *
   * @param projectsUri list of URIs to projects which should be updated
   */
  private static void doReImportMavenProjects(
      List<String> projectsUri, IProgressMonitor progressMonitor) {
    final MavenProjectManager mavenProjectManager =
        MavenPluginActivator.getDefault().getMavenProjectManager();
    mavenProjectManager.addMavenProjectChangedListener(mavenProjectChangedListener);

    final PreferenceManager preferenceManager = new PreferenceManager();
    final ProjectsManager projectsManager = new ProjectsManager(preferenceManager);

    IFile pomFile;
    for (String pathToProject : projectsUri) {
      ensureNotCancelled(progressMonitor);

      pomFile = JDTUtils.findFile(pathToProject + "/pom.xml");
      if (pomFile == null) {
        continue;
      }

      projectsManager.updateProject(pomFile.getProject());
    }

    mavenProjectManager.removeMavenProjectChangedListener(mavenProjectChangedListener);
  }

  private static void ensureNotCancelled(IProgressMonitor progressMonitor) {
    if (progressMonitor.isCanceled()) {
      throw new OperationCanceledException();
    }
  }

  private static class MavenProjectChangedListener implements IMavenProjectChangedListener {
    /** Listens to maven project change events to get information about submodules. */
    @Override
    public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor) {
      for (MavenProjectChangedEvent event : events) {
        String projectUri = event.getMavenProject().getProject().getFullPath().toString();
        switch (event.getKind()) {
          case MavenProjectChangedEvent.KIND_CHANGED:
            updatedProjects.add(projectUri);
            break;
          case MavenProjectChangedEvent.KIND_ADDED:
            addedProjects.add(projectUri);
            break;
          case MavenProjectChangedEvent.KIND_REMOVED:
            removedProjects.add(projectUri);
            break;
        }
      }
    }
  }
}
