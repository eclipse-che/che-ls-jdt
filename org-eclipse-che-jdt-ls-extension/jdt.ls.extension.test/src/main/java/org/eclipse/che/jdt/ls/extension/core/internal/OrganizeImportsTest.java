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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.junit.Before;
import org.junit.Test;

public class OrganizeImportsTest extends AbstractProjectsManagerBasedTest {
  private static final String FILE1 = "/src/main/java/org/eclipse/che/examples/HelloWorld1.java";
  private static final String FILE2 = "/src/main/java/org/eclipse/che/examples/HelloWorld2.java";
  private static final String DIR = "/src/main/java";

  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/organizeimports");
    project = WorkspaceHelper.getProject("organizeimports");
  }

  @Test
  public void shouldOrganizeImportsInFile() throws Exception {
    String fileUri = createFileUri(FILE1);
    List<Object> params = Collections.singletonList(fileUri);

    WorkspaceEdit workspaceEdit = OrganizeImports.execute(params, new NullProgressMonitor());

    assertEquals(workspaceEdit.getChanges().size(), 1);
    assertFalse(workspaceEdit.getChanges().get(fileUri).isEmpty());
  }

  @Test
  public void shouldReturnEmptyListIfNoOrganizeImportRequired() throws Exception {
    String fileUri = createFileUri(FILE2);
    List<Object> params = Collections.singletonList(fileUri);

    WorkspaceEdit workspaceEdit = OrganizeImports.execute(params, new NullProgressMonitor());

    assertEquals(workspaceEdit.getChanges().size(), 1);
    assertTrue(workspaceEdit.getChanges().get(fileUri).isEmpty());
  }

  @Test
  public void shouldOrganizeImportsInDirectory() throws Exception {
    String fileUri1 = createFileUri(FILE1);
    String fileUri2 = createFileUri(FILE2);
    List<Object> params = Collections.singletonList(createFileUri(DIR));

    WorkspaceEdit workspaceEdit = OrganizeImports.execute(params, new NullProgressMonitor());

    assertEquals(workspaceEdit.getChanges().size(), 2);
    assertFalse(workspaceEdit.getChanges().get(fileUri1).isEmpty());
    assertTrue(workspaceEdit.getChanges().get(fileUri2).isEmpty());
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return ResourceUtils.fixURI(uri);
  }
}
