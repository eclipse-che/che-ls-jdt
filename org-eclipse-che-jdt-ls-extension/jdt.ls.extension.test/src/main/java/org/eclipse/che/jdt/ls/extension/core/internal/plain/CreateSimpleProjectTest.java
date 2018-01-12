/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class CreateSimpleProjectTest extends AbstractProjectsManagerBasedTest {
  private static final String PROJECT_NAME = "plain";
  private static final String SOURCE_FOLDER = "src";

  private List<Object> arguments;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    IProject project = WorkspaceHelper.getProject("testproject");
    String uri = project.getRawLocationURI().toString();

    String plainUri = uri.replaceFirst("maven/testproject", PROJECT_NAME);

    arguments = Arrays.asList(plainUri, SOURCE_FOLDER);
  }

  @Test
  public void simpleProjectShouldBeCreated() throws Exception {
    CreateSimpleProjectCommand.execute(arguments, new NullProgressMonitor());
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("plain");
    assertTrue(project.exists());
  }

  @Test
  public void classpathShouldBeSet() throws Exception {
    CreateSimpleProjectCommand.execute(arguments, new NullProgressMonitor());
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("plain");

    IJavaProject jProject = JavaCore.create(project);

    IClasspathEntry[] classpath = jProject.getRawClasspath();
    assertNotNull(classpath);
    assertEquals(2, classpath.length);

    // JRE container
    IClasspathEntry container = classpath[0];
    assertEquals("org.eclipse.jdt.launching.JRE_CONTAINER", container.getPath().toString());
    assertEquals(IClasspathEntry.CPE_CONTAINER, container.getEntryKind());

    // src folder
    IClasspathEntry sourceFolder = classpath[1];
    assertEquals("/plain/src", sourceFolder.getPath().toString());
    assertEquals(IClasspathEntry.CPE_SOURCE, sourceFolder.getEntryKind());

    // output dir
    IPath outputDir = jProject.getOutputLocation();
    assertNotNull(outputDir);
    assertEquals("/plain/bin", outputDir.toString());
  }
}
