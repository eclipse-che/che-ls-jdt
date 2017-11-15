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
package org.eclipse.che.jdt.ls.extension.core.internal.classpath;

import static java.util.Arrays.asList;
import static org.eclipse.che.jdt.ls.extension.core.internal.classpath.ResolveClassPathsHandler.getOutputDirectory;
import static org.eclipse.che.jdt.ls.extension.core.internal.classpath.ResolveClassPathsHandler.resolveClasspaths;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.junit.Before;
import org.junit.Test;

public class ResolveClassPathsHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldReturnResolvedClasspath() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    List<Object> arguments = asList(projectUri);
    List<String> result = resolveClasspaths(arguments);

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void shouldReturnOutputDirectory() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    List<Object> arguments = asList(projectUri);
    String result = getOutputDirectory(arguments);

    assertTrue(result.endsWith("testproject/target/classes"));
  }

  @SuppressWarnings("restriction")
  private String getResourceUriAsString(URI uri) {
    return ResourceUtils.fixURI(uri);
  }
}
