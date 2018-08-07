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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibraries;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.LibraryEntryCommand;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.LibraryNavigation;
import org.eclipse.che.jdt.ls.extension.core.internal.externallibrary.ProjectExternalLibraryCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.Test;

public class LibraryEntryCommandTest extends AbstractProjectsManagerBasedTest {
  private String projectUri;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");

    IProject project = WorkspaceHelper.getProject("testproject");
    assertNotNull(project);

    projectUri = getResourceUriAsString(project.getRawLocationURI());
  }

  @Test
  public void shouldFindJUnitClassEntryDirectly() throws Exception {
    Optional<JarEntry> jarEntry = findEntryAsAChildEntry("org.junit", "Rule.class");
    assertTrue(jarEntry.isPresent());

    JarEntry target =
        LibraryEntryCommand.execute(
            singletonList(jarEntry.get().getUri()), new NullProgressMonitor());

    assertNotNull(target);
    assertEquals("org.junit.Rule", target.getPath());
    assertEquals("Rule.class", target.getName());
    assertEquals("CLASS_FILE", target.getEntryType());
    assertEquals(target.getUri(), jarEntry.get().getUri());
  }

  private Optional<JarEntry> findEntryAsAChildEntry(String... entryNames)
      throws JavaModelException {
    Optional<JarEntry> resultEntry = Optional.empty();

    Optional<Jar> jar = findJUnitJar();
    assertTrue(jar.isPresent());

    List<JarEntry> entries =
        LibraryNavigation.getPackageFragmentRootContent(
            jar.get().getId(), new NullProgressMonitor());

    for (int i = 0; i < entryNames.length; i++) {
      String entryName = entryNames[i];

      resultEntry = entries.stream().filter(entry -> entryName.equals(entry.getName())).findFirst();
      assertTrue(resultEntry.isPresent());

      if (i != entryNames.length - 1) {
        entries =
            LibraryNavigation.getChildren(
                jar.get().getId(), resultEntry.get().getPath(), new NullProgressMonitor());
      }
    }

    return resultEntry;
  }

  private Optional<Jar> findJUnitJar() {
    ExternalLibrariesParameters params = new ExternalLibrariesParameters(projectUri);

    List<Jar> jars =
        ProjectExternalLibraryCommand.execute(singletonList(params), new NullProgressMonitor());
    return jars.stream().filter(item -> "junit-4.12.jar".equals(item.getName())).findFirst();
  }
}
