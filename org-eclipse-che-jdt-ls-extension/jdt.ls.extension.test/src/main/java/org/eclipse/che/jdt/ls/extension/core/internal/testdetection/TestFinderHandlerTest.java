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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler.getClassFqn;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler.getClassesFromFolder;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler.getTestByCursorPosition;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.TestFindParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

public class TestFinderHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;
  public static final String TEST_METHOD_ANNOTATION = "org.junit.Test";
  public static final String TEST_CLASS_ANNOTATION = "org.junit.runner.RunWith";

  @Before
  public void setup() throws Exception {
    importProjects("maven/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void firstTestMethodShouldBeFoundByCursorPosition() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    int cursorOffset = 700;

    TestFindParameters params =
        new TestFindParameters(
            fileURI, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION, cursorOffset, emptyList());

    List<String> result = getTestByCursorPosition(singletonList(params), new NullProgressMonitor());
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest#shouldSuccessOfAppOne", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundByCursorPositionIfItIsNotMethodBody() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    int cursorOffset = -1;

    TestFindParameters params =
        new TestFindParameters(
            fileURI, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION, cursorOffset, emptyList());

    List<String> result = getTestByCursorPosition(singletonList(params), new NullProgressMonitor());
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundIfContextTypeIsFile() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");

    TestFindParameters params =
        new TestFindParameters(
            fileURI, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION, 0, emptyList());

    List<String> result = getClassFqn(singletonList(params), new NullProgressMonitor());
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundIfContextTypeIsFileWithPackage() throws Exception {
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples");

    TestFindParameters params =
        new TestFindParameters(
            fileURI, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION, 0, emptyList());

    List<String> result = getClassesFromFolder(singletonList(params), new NullProgressMonitor());
    assertNotNull(result);
    assertEquals(3, result.size());
    List<String> espected =
        asList(
            "org.eclipse.che.examples.AppOneTest",
            "org.eclipse.che.examples.Junit4TestSuite",
            "org.eclipse.che.examples.AppAnotherTest");
    assertThat(espected, hasItems(result.get(0), result.get(1), result.get(2)));
  }

  @Test
  public void testClassesShouldBeFoundInTheProject() throws Exception {
    String projectUri = getResourceUriAsString(project.getRawLocationURI());

    TestFindParameters params =
        new TestFindParameters(
            projectUri, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION, 0, emptyList());

    List<String> result =
        TestFinderHandler.getClassesFromProject(singletonList(params), new NullProgressMonitor());
    assertNotNull(result);
    assertEquals(3, result.size());

    List<String> espected =
        asList(
            "org.eclipse.che.examples.AppOneTest",
            "org.eclipse.che.examples.Junit4TestSuite",
            "org.eclipse.che.examples.AppAnotherTest");
    assertThat(espected, hasItems(result.get(0), result.get(1), result.get(2)));
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return getResourceUriAsString(uri);
  }
}
