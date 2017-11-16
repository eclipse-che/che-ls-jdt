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
package org.eclipse.che.jdt.ls.extension.core.internal.testdetection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.junit.Before;
import org.junit.Test;

public class TestFinderHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void firstTestMethodShouldBeFoundByCursorPosition() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    double cursorOffset = 700.0;

    List<Object> arguments = asList(fileURI, cursorOffset);

    List<String> result = TestFinderHandler.getTestByCursorPosition(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest#shouldSuccessOfAppOne", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundByCursorPositionIfItIsNotMethodBody() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    double cursorOffset = -1.0;

    List<Object> arguments = asList(fileURI, cursorOffset);

    List<String> result = TestFinderHandler.getTestByCursorPosition(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundIfContextTypeIsFile() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");

    List<Object> arguments = singletonList(fileURI);

    List<String> result = TestFinderHandler.getClass(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundIfContextTypeIsFileWithPackage() throws Exception {
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples");

    List<Object> arguments = asList(fileURI, testMethodAnnotation, testClassAnnotation);

    List<String> result = TestFinderHandler.getClassesFromFolder(arguments);
    assertNotNull(result);
    assertEquals(2, result.size());
    List<String> espected =
        asList("org.eclipse.che.examples.AppOneTest", "org.eclipse.che.examples.AppAnotherTest");
    assertThat(espected, hasItems(result.get(0), result.get(1)));
  }

  @Test
  public void testClassesShouldBeFoundInTheProject() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";

    List<Object> arguments = asList(projectUri, testMethodAnnotation, testClassAnnotation);

    List<String> result = TestFinderHandler.getClassesFromProject(arguments);
    assertNotNull(result);
    assertEquals(2, result.size());

    List<String> espected =
        asList("org.eclipse.che.examples.AppOneTest", "org.eclipse.che.examples.AppAnotherTest");
    assertThat(espected, hasItems(result.get(0), result.get(1)));
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return getResourceUriAsString(uri);
  }

  @SuppressWarnings("restriction")
  private String getResourceUriAsString(URI uri) {
    return ResourceUtils.fixURI(uri);
  }
}
