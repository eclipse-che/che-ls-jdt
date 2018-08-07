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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class GetSourceFoldersCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("che/plain");
    project = WorkspaceHelper.getProject("plain");
  }

  @Test
  public void sourceFoldersShouldBeReturned() throws Exception {
    String projectUri = getResourceUriAsString(project.getLocationURI());

    List<Object> arguments = singletonList(projectUri);

    List<String> result = GetSourceFoldersCommand.execute(arguments, new NullProgressMonitor());

    assertEquals(2, result.size());
    assertThat(result, hasItems(projectUri + "/src", projectUri + "/src2"));
  }
}
