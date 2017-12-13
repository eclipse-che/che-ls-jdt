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

import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class SimpleJavaProjectImporterTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("che/plain");
    project = WorkspaceHelper.getProject("plain");
  }

  @Test
  public void shouldReadClasspath() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    IJavaProject javaProject = getJavaProject(projectUri);

    assertEquals("/plain/bin", javaProject.readOutputLocation().toString());

    IClasspathEntry[] classpath = javaProject.getRawClasspath();
    assertEquals(3, classpath.length);
    List<String> paths = new ArrayList<>();
    for (IClasspathEntry entry : classpath) {
      paths.add(entry.getPath().toString());
    }
    assertThat(
        paths,
        CoreMatchers.hasItems(
            "org.eclipse.jdt.launching.JRE_CONTAINER", "/plain/src", "/plain/src2"));
  }
}
