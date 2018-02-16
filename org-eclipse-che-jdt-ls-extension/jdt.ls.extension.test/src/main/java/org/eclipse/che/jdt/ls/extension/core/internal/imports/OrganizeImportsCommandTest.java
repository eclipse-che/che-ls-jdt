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
package org.eclipse.che.jdt.ls.extension.core.internal.imports;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportParams;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.junit.Before;
import org.junit.Test;

/** @author Anatolii Bazko */
public class OrganizeImportsCommandTest extends AbstractProjectsManagerBasedTest {

  @Before
  public void setup() throws Exception {
    importProjects("organizeimports");
  }

  @Test
  public void shouldOrganizeImports() throws Exception {
    IProject project = WorkspaceHelper.getProject("organizeimports");
    assertNotNull(project);

    IFile file = project.getFile("src/Main.java");
    assertNotNull(file);

    URI uri = file.getLocationURI();
    OrganizeImportParams organizeImports = new OrganizeImportParams(ResourceUtils.fixURI(uri));

    OrganizeImportsResult importsResult =
        OrganizeImportsCommand.execute(singletonList(organizeImports), new NullProgressMonitor());

    assertEquals(2, importsResult.getAmbiguousTypes().size());
    assertTrue(!importsResult.getWorkspaceEdit().getChanges().isEmpty());

    organizeImports = new OrganizeImportParams(ResourceUtils.fixURI(uri), asList("java.util.List"));

    importsResult =
        OrganizeImportsCommand.execute(singletonList(organizeImports), new NullProgressMonitor());

    assertEquals(1, importsResult.getAmbiguousTypes().size());
    assertTrue(!importsResult.getWorkspaceEdit().getChanges().isEmpty());

    organizeImports =
        new OrganizeImportParams(
            ResourceUtils.fixURI(uri), asList("java.util.List", "java.util.Map"));

    importsResult =
        OrganizeImportsCommand.execute(singletonList(organizeImports), new NullProgressMonitor());

    assertTrue(importsResult.getAmbiguousTypes().isEmpty());
    assertTrue(!importsResult.getWorkspaceEdit().getChanges().isEmpty());
  }
}
