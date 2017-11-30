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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibraries;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.jdt.ls.extension.api.dto.ClassContent;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ContentByFqnCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

public class ContentByFqnCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldFindContentOfClassByFqn() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    ExternalLibrariesParameters params = new ExternalLibrariesParameters();
    params.setProjectUri(projectUri);
    params.setNodePath("junit.extensions.TestSetup");
    ClassContent content =
        ContentByFqnCommand.execute(singletonList(params), new NullProgressMonitor());

    String expectedContent = getExpectedContent();

    assertNotNull(content);
    assertFalse(content.isGenerated());
    assertEquals(expectedContent, content.getContent());
  }

  private String getExpectedContent() throws IOException {
    InputStream expectedClassContent = getClass().getResourceAsStream("TestSetup");
    return IOUtils.toString(expectedClassContent, "UTF-8");
  }
}
