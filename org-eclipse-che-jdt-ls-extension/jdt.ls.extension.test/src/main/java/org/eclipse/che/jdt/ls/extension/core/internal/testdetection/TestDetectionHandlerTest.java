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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class TestDetectionHandlerTest extends AbstractProjectsManagerBasedTest {

  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("eclipse/testproject");
    project = WorkspaceHelper.getProject("testproject");
  }

  @Test
  public void shouldReturnEmptyListIfCompilationUnitCanNotBeResolved() throws Exception {
    String testAnnotation = "org.junit.Test";
    double cursorOffset = -1.0;
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/Wrong.java");

    List<Object> arguments = asList(fileURI, testAnnotation, cursorOffset);

    List<TestPosition> result = TestDetectionHandler.detect(arguments);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldDetect3TestMethods() throws Exception {
    String testAnnotation = "org.junit.Test";
    double cursorOffset = -1.0;
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");

    List<Object> arguments = asList(fileURI, testAnnotation, cursorOffset);

    List<TestPosition> result = TestDetectionHandler.detect(arguments);
    assertNotNull(result);
    assertEquals(3, result.size());

    // check methods' names
    List<String> testNames =
        asList("shouldSuccessOfAppOne", "shouldFailOfAppOne", "shouldBeIgnoredOfAppOne");
    assertThat(
        testNames,
        CoreMatchers.hasItems(
            result.get(0).getTestName(), result.get(1).getTestName(), result.get(2).getTestName()));
  }

  @Test
  public void shouldDetectOnlyFirstTestMethods() throws Exception {
    String testAnnotation = "org.junit.Test";
    double cursorOffset = 700.0;
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");

    List<Object> arguments = asList(fileURI, testAnnotation, cursorOffset);

    List<TestPosition> result = TestDetectionHandler.detect(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());

    TestPosition testPosition = result.get(0);

    assertEquals("shouldSuccessOfAppOne", testPosition.getTestName());
    assertEquals(697, testPosition.getTestNameStartOffset());
    assertEquals(21, testPosition.getTestNameLength());
    assertEquals(104, testPosition.getTestBodyLength());
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
