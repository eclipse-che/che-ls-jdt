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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ProjectExternalLibraryCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class ProjectExternalLibraryCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldReturnListOfDependencies() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    ExternalLibrariesParameters params = new ExternalLibrariesParameters(projectUri);
    List<Jar> jars =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());

    assertFalse(jars.isEmpty());

    List<String> names = jars.stream().map(Jar::getName).collect(Collectors.toList());
    assertThat(names, hasItems("junit-4.12.jar", "rt.jar", "hamcrest-core-1.3.jar"));
  }
}
