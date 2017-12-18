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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin.getProjectsManager;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.JobResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.managers.ProjectsManager;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/** @author Anatolii Bazko */
public class UpdateWorkspaceCommand {

  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  /**
   * Updates eclipse workspace after adding/removing projects.
   *
   * @param params {@link UpdateWorkspaceParameters} expected
   */
  public static JobResult execute(List<Object> params, IProgressMonitor pm) {
    Preconditions.checkArgument(
        !params.isEmpty(), UpdateWorkspaceParameters.class.getName() + " expected.");
    ensureNotCancelled(pm);

    UpdateWorkspaceParameters updateWorkspaceParameters =
        gson.fromJson(gson.toJson(params.get(0)), UpdateWorkspaceParameters.class);

    ProjectsManager projectsManager = getProjectsManager();

    Job job =
        projectsManager.updateWorkspaceFolders(
            updateWorkspaceParameters
                .getAddedProjectsUri()
                .stream()
                .map(ResourceUtils::filePathFromURI)
                .collect(Collectors.toList()),
            updateWorkspaceParameters
                .getRemovedProjectsUri()
                .stream()
                .map(ResourceUtils::filePathFromURI)
                .collect(Collectors.toList()));

    try {
      job.join(0L, pm);
    } catch (InterruptedException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
      return new JobResult(IStatus.CANCEL, 1, e.getMessage());
    }

    IStatus result = job.getResult();
    return new JobResult(result.getSeverity(), result.getCode(), result.getMessage());
  }

  private static void ensureNotCancelled(IProgressMonitor pm) {
    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
