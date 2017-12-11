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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ContentByPathCommand;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ProjectExternalLibraryCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

public class ContentByPathCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldFindContentOfClassByPath() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    ExternalLibrariesParameters params = new ExternalLibrariesParameters(projectUri);
    List<Jar> jars =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());

    Jar junitJar = findJarByName("junit-4.12.jar", jars);
    assertNotNull(junitJar);

    params.setNodeId(junitJar.getId());
    params.setNodePath("junit.extensions.TestSetup");
    String content = ContentByPathCommand.execute(singletonList(params), new NullProgressMonitor());

    String expectedContent = getExpectedContent();

    assertNotNull(content);
    assertEquals(expectedContent, content);
  }

  private Jar findJarByName(String name, List<Jar> jars) {
    for (Jar jar : jars) {
      if (name.equals(jar.getName())) {
        return jar;
      }
    }
    return null;
  }

  private String getExpectedContent() throws IOException {
    InputStream expectedClassContent = getClass().getResourceAsStream("TestSetup");
    return IOUtils.toString(expectedClassContent, "UTF-8");
  }
}
