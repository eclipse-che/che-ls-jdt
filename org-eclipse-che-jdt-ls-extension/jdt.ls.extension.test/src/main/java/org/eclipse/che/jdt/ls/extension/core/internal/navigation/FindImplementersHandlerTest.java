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
package org.eclipse.che.jdt.ls.extension.core.internal.navigation;

import static java.util.Collections.singletonList;
import static org.eclipse.che.jdt.ls.extension.core.internal.navigation.FindImplementersHandler.getImplementers;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.junit.Before;
import org.junit.Test;

/** @author Dmitrii Bocharov */
public class FindImplementersHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void testFindInterfaceMethodImplementers() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/TestInterface.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(15, 10));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(1, impls.getImplementers().size());
    assertEquals("interfaceMethod()", impls.getImplementers().get(0).getName());
    assertEquals(
        createFileUri("src/main/java/org/eclipse/che/examples/ChildClass.java"),
        impls.getImplementers().get(0).getLocation().getUri());
  }

  @Test
  public void testFindInterfaceImplementers() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/TestInterface.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(13, 22));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(2, impls.getImplementers().size());
    assertEquals("ParentClass", impls.getImplementers().get(0).getName());
    assertEquals("ChildClass", impls.getImplementers().get(1).getName());
  }

  @Test
  public void testFindClassMethodImplementers() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/ParentClass.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(15, 24));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(1, impls.getImplementers().size());
    assertEquals("simpleMethod(int)", impls.getImplementers().get(0).getName());
  }

  @Test
  public void testFindClassImplementers() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/ParentClass.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(13, 29));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(1, impls.getImplementers().size());
    assertEquals("ChildClass", impls.getImplementers().get(0).getName());
  }

  @Test
  public void testFindImplementersWhenNoValidElementSelected() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/ParentClass.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(0, 0));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(0, impls.getImplementers().size());
    assertNull(impls.getSearchedElement());
  }

  @Test
  public void testNoImplementersExist() {
    String fileURI = createFileUri("src/main/java/org/eclipse/che/examples/ChildClass.java");
    TextDocumentPositionParams params =
        new TextDocumentPositionParams(new TextDocumentIdentifier(fileURI), new Position(13, 20));
    ImplementersResponse impls = getImplementers(singletonList(params), new NullProgressMonitor());
    assertEquals(0, impls.getImplementers().size());
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return getResourceUriAsString(uri);
  }
}
