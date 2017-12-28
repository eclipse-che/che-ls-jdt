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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateClasspathParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class UpdateProjectClasspathCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("che/plain");
    project = WorkspaceHelper.getProject("plain");
  }

  @Test
  public void classpathShouldBeUpdated() throws Exception {
    String sourcePath = "/plain/src3";
    String libPath = "/plain/a.jar";
    String projectPath = "/plain/NewProject";

    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    ClasspathEntry newSrc = new ClasspathEntry();
    newSrc.setEntryKind(IClasspathEntry.CPE_SOURCE);
    newSrc.setPath(sourcePath);

    ClasspathEntry newLib = new ClasspathEntry();
    newLib.setEntryKind(IClasspathEntry.CPE_LIBRARY);
    newLib.setPath(libPath);

    ClasspathEntry newProject = new ClasspathEntry();
    newProject.setEntryKind(IClasspathEntry.CPE_PROJECT);
    newProject.setPath(projectPath);

    List<ClasspathEntry> entries = asList(newLib, newSrc, newProject);
    UpdateClasspathParameters classpathParams = new UpdateClasspathParameters();
    classpathParams.setEntries(entries);
    classpathParams.setProjectUri(projectUri);

    List<Object> arguments = singletonList(classpathParams);

    UpdateProjectClasspathCommand.execute(arguments, new NullProgressMonitor());

    IJavaProject jProject = JavaCore.create(project);
    IClasspathEntry[] newClasspath = jProject.getRawClasspath();

    assertEquals(3, newClasspath.length);

    List<String> expectedPathes = asList(sourcePath, libPath, projectPath);
    assertThat(
        expectedPathes,
        hasItems(
            newClasspath[0].getPath().toString(),
            newClasspath[1].getPath().toString(),
            newClasspath[2].getPath().toString()));
  }
}
