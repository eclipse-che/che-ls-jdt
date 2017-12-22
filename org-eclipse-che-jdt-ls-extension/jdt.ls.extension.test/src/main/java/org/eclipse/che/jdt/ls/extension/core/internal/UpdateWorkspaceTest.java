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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import org.eclipse.che.jdt.ls.extension.api.Severity;
import org.eclipse.che.jdt.ls.extension.api.dto.JobResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/** @author Anatolii Bazko */
public class UpdateWorkspaceTest extends AbstractProjectsManagerBasedTest {

  @Test
  public void shouldUpdateWorkspace() throws Exception {
    shouldAssert0Projects();
    shouldAssert1ProjectAfterAdding();
    shouldAssert2ProjectAfterAdding();
    shouldAssert1ProjectAfterRemoving();
  }

  private void shouldAssert0Projects() {
    assertProjectsNumber(0);

    UpdateWorkspaceParameters parameters = new UpdateWorkspaceParameters();
    JobResult jobResult =
        UpdateWorkspaceCommand.execute(singletonList(parameters), new NullProgressMonitor());

    assertProjectsNumber(0);
    assertJobResult(jobResult);
  }

  private void shouldAssert1ProjectAfterAdding() throws IOException {
    assertProjectsNumber(0);

    URI addedProjectUri = addProject("maven/testproject");

    UpdateWorkspaceParameters parameters = new UpdateWorkspaceParameters();
    parameters.setAddedProjectsUri(singletonList(addedProjectUri.toString()));

    JobResult jobResult =
        UpdateWorkspaceCommand.execute(singletonList(parameters), new NullProgressMonitor());

    assertProjectsNumber(1, "testproject");
    assertJobResult(jobResult);
  }

  private void shouldAssert2ProjectAfterAdding() throws IOException {
    assertProjectsNumber(1, "testproject");

    URI addedProjectUri = addProject("maven/debugproject");

    UpdateWorkspaceParameters parameters = new UpdateWorkspaceParameters();
    parameters.setAddedProjectsUri(singletonList(addedProjectUri.toString()));

    JobResult jobResult =
        UpdateWorkspaceCommand.execute(singletonList(parameters), new NullProgressMonitor());

    assertProjectsNumber(2, "testproject", "debugproject");
    assertJobResult(jobResult);
  }

  private void shouldAssert1ProjectAfterRemoving() throws Exception {
    assertProjectsNumber(2, "testproject", "debugproject");

    URI removedProjectUri = removeProject("maven/testproject");

    UpdateWorkspaceParameters parameters = new UpdateWorkspaceParameters();
    parameters.setRemovedProjectsUri(singletonList(removedProjectUri.toString()));

    JobResult jobResult =
        UpdateWorkspaceCommand.execute(singletonList(parameters), new NullProgressMonitor());

    assertProjectsNumber(1, "debugproject");
    assertJobResult(jobResult);
  }

  private void assertProjectsNumber(int expectedProjectsNumber, String... projects) {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    assertEquals(expectedProjectsNumber, root.getProjects().length);

    if (projects != null) {
      for (String project : projects) {
        assertNotNull(root.getProject(project));
      }
    }
  }

  private void assertJobResult(JobResult jobResult) {
    assertEquals(jobResult.getSeverity(), Severity.OK);
    assertEquals(jobResult.getResultCode(), 0);
  }

  private URI addProject(String path) throws IOException {
    return copyFiles(path, true).toURI();
  }

  private URI removeProject(String path) throws Exception {
    return deleteFiles(path).toURI();
  }
}
