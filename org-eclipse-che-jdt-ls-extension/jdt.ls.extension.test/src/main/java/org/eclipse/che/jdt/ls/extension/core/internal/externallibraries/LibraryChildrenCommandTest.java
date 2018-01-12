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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibraries;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.LibraryChildrenCommand;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ProjectExternalLibraryCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

/** @author Valeriy Svydenko */
public class LibraryChildrenCommandTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldFindChildrenOfJUnitLibrary() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    ExternalLibrariesParameters params = new ExternalLibrariesParameters(projectUri);
    List<Jar> jars =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());

    Jar junitJar = findJarByName("junit-4.12.jar", jars);
    assertNotNull(junitJar);

    params.setNodeId(junitJar.getId());
    List<JarEntry> entries =
        LibraryChildrenCommand.execute(singletonList(params), new NullProgressMonitor());

    assertFalse(entries.isEmpty());
    assertEquals(4, entries.size());

    assertContainsJarEntry("junit", "PACKAGE", entries);
    assertContainsJarEntry("org.junit", "PACKAGE", entries);
    assertContainsJarEntry("LICENSE-junit.txt", "FILE", entries);
    assertContainsJarEntry("META-INF", "FOLDER", entries);
  }

  private void assertContainsJarEntry(String name, String entryType, List<JarEntry> entries) {
    assertFalse(entries.isEmpty());
    JarEntry foundEntry = null;
    for (JarEntry entry : entries) {
      if (name.equals(entry.getName())) {
        foundEntry = entry;
        assertEquals(entryType, entry.getEntryType());
        return;
      }
    }
    assertNotNull(foundEntry);
  }

  private Jar findJarByName(String name, List<Jar> jars) {
    for (Jar jar : jars) {
      if (name.equals(jar.getName())) {
        return jar;
      }
    }
    return null;
  }
}
