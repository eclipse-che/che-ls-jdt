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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import static java.util.Collections.singletonList;
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;
import static org.eclipse.che.jdt.ls.extension.core.internal.pom.ReImportMavenProjectsHandler.reImportMavenProjects;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.junit.Before;
import org.junit.Test;

/** @author Mykola Morhun */
public class ReImportMavenProjectsHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldRespondAfterUpdateWithListOfUpdatedProjects() throws Exception {
    final String uriToProject = "file://" + project.getLocation().toString();
    final List<Object> arguments = getReImportArguments(uriToProject);

    final List<String> updatedProjects =
        reImportMavenProjects(arguments, new NullProgressMonitor());

    assertTrue(updatedProjects.size() == 1);
    assertEquals(uriToProject, updatedProjects.get(0));
  }

  @Test
  public void shouldUpdateMavenDependenciesDuringProjectUpdate() throws Exception {
    final String uriToProject = "file://" + project.getLocation().toString();
    final List<Object> arguments = getReImportArguments(uriToProject);
    final IJavaProject javaProject =
        getJavaProject(getResourceUriAsString(project.getRawLocationURI()));

    final IFile pom =
        MavenPluginActivator.getDefault().getMavenProjectManager().getProject(project).getPom();
    final String originalPomContent = IOUtils.toString(pom.getContents(), "UTF-8");

    final List<String> jarsBeforeReimport =
        getExternalJars(javaProject.getResolvedClasspath(false));
    addDependencyIntoPom(pom);
    reImportMavenProjects(arguments, new NullProgressMonitor());
    final List<String> jarsAfterReimport = getExternalJars(javaProject.getResolvedClasspath(false));
    FileUtils.writeStringToFile(pom.getLocation().toFile(), originalPomContent);
    reImportMavenProjects(arguments, new NullProgressMonitor());
    final List<String> jarsOriginal = getExternalJars(javaProject.getResolvedClasspath(false));

    assertTrue(jarsAfterReimport.containsAll(jarsBeforeReimport));

    List<String> newJars = new LinkedList<>(jarsAfterReimport);
    assertTrue(newJars.removeAll(jarsBeforeReimport));
    assertTrue(newJars.size() > 0);
    assertTrue(newJars.contains("eclipse-boot-2.1.0.jar"));

    assertEquals(jarsBeforeReimport, jarsOriginal);
  }

  /** Returns names of all jars from external libraries */
  private List<String> getExternalJars(IClasspathEntry[] classpathEntries) {
    final List<String> jars = new LinkedList<>();
    for (IClasspathEntry classpathEntry : classpathEntries) {
      if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
        jars.add(classpathEntry.getPath().lastSegment());
      }
    }
    return jars;
  }

  /** Replaces test project pom to add eclipse-boot 2.1.0 dependency */
  private void addDependencyIntoPom(IFile pom) throws IOException {
    InputStream alteredPomInputStream =
        getClass().getResourceAsStream("reimport-testproject-pom-with-additional-dependency.txt");
    String alteredPomContent = IOUtils.toString(alteredPomInputStream, "UTF-8");
    FileUtils.writeStringToFile(pom.getLocation().toFile(), alteredPomContent);
  }

  /** Returns arguments to reimport given project */
  private List<Object> getReImportArguments(String uriToProject) {
    final List<String> uris = new ArrayList<>();
    uris.add(uriToProject);
    final ReImportMavenProjectsCommandParameters parameters =
        new ReImportMavenProjectsCommandParameters().withProjectsToUpdate(uris);
    return singletonList(parameters);
  }
}
