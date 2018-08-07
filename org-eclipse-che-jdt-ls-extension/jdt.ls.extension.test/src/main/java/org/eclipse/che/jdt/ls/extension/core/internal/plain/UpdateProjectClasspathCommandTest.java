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
    String projectUri = getResourceUriAsString(project.getLocationURI());
    String sourcePath = projectUri + "/plain/src3";
    String libPath = projectUri + "/plain/a.jar";

    ClasspathEntry newSrc = new ClasspathEntry();
    newSrc.setEntryKind(IClasspathEntry.CPE_SOURCE);
    newSrc.setPath(sourcePath);

    ClasspathEntry newLib = new ClasspathEntry();
    newLib.setEntryKind(IClasspathEntry.CPE_LIBRARY);
    newLib.setPath(libPath);

    List<ClasspathEntry> entries = asList(newLib, newSrc);
    UpdateClasspathParameters classpathParams = new UpdateClasspathParameters();
    classpathParams.setEntries(entries);
    classpathParams.setProjectUri(projectUri);

    List<Object> arguments = singletonList(classpathParams);

    UpdateProjectClasspathCommand.execute(arguments, new NullProgressMonitor());

    IJavaProject jProject = JavaCore.create(project);
    IClasspathEntry[] newClasspath = jProject.getRawClasspath();

    assertEquals(2, newClasspath.length);

    List<String> newPaths =
        asList(newClasspath[0].getPath().toString(), newClasspath[1].getPath().toString());
    assertThat(newPaths, hasItems("/plain/plain/src3", libPath.substring(7)));
  }
}
