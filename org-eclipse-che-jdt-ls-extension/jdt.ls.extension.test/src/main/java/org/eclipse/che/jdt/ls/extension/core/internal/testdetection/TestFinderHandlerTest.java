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
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler.find;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestFinderHandlerTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void setup() throws Exception {
    List<IProject> iProjects = importProjects("eclipse/testproject");
    for (IProject project : iProjects) {
      JavaLanguageServerPlugin.logInfo(
          "*******************************************Imported project location: "
              + project.getLocation());
    }
    project = WorkspaceHelper.getProject("testproject");
    IProjectDescription description = project.getDescription();
    description.setNatureIds(new String[] {"org.eclipse.jdt.core.javanature"});
    project.setDescription(description, monitor);
    JavaLanguageServerPlugin.logInfo(
        "*******************************************project exist: " + project.exists());
    JavaLanguageServerPlugin.logInfo(
        "*******************************************project location " + project.getLocation());
  }

  @Test
  @Ignore
  public void shouldReturnEmptyListIfContextTypeIsWrong() throws Exception {
    String contextType = "WRONG_CONTEXT_TYPE";
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    double cursorOffset = 700.0;

    List<Object> arguments =
        asList(
            contextType,
            projectUri,
            testMethodAnnotation,
            testClassAnnotation,
            fileURI,
            cursorOffset);

    List<String> result = find(arguments);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  @Ignore
  public void firstTestMethodShouldBeFoundByCursorPosition() throws Exception {
    String contextType = "CURSOR_POSITION";
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    double cursorOffset = 700.0;

    List<Object> arguments =
        asList(
            contextType,
            projectUri,
            testMethodAnnotation,
            testClassAnnotation,
            fileURI,
            cursorOffset);

    List<String> result = find(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest#shouldSuccessOfAppOne", result.get(0));
  }

  @Test
  @Ignore
  public void classDeclarationShouldBeFoundByCursorPositionIfItIsNotMethodBody() throws Exception {
    String contextType = "CURSOR_POSITION";
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");
    double cursorOffset = -1.0;

    List<Object> arguments =
        asList(
            contextType,
            projectUri,
            testMethodAnnotation,
            testClassAnnotation,
            fileURI,
            cursorOffset);

    List<String> result = find(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
  }

  @Test
  public void classDeclarationShouldBeFoundIfContextTypeIsFile() throws Exception {
    JavaLanguageServerPlugin.logInfo(
        "*******************************************Test started!!!!!!");
    String contextType = "FILE";
    URI rawLocationURI = project.getRawLocationURI();
    JavaLanguageServerPlugin.logInfo(
        "*******************************************Raw location URI --> " + rawLocationURI);
    String projectUri = getResourceUriAsString(rawLocationURI);
    JavaLanguageServerPlugin.logInfo(
        "*******************************************Project URI --> " + projectUri);
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples/AppOneTest.java");

    List<Object> arguments =
        asList(contextType, projectUri, testMethodAnnotation, testClassAnnotation, fileURI);

    List<String> result = find(arguments);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("org.eclipse.che.examples.AppOneTest", result.get(0));
    JavaLanguageServerPlugin.logInfo("Test finished!!!!!!");
  }

  @Test
  @Ignore
  public void classDeclarationShouldBeFoundIfContextTypeIsFileWithPackage() throws Exception {
    String contextType = "FOLDER";
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";
    String fileURI = createFileUri("src/test/java/org/eclipse/che/examples");

    List<Object> arguments =
        asList(contextType, projectUri, testMethodAnnotation, testClassAnnotation, fileURI);

    List<String> result = find(arguments);
    assertNotNull(result);
    assertEquals(2, result.size());
    List<String> espected =
        asList("org.eclipse.che.examples.AppOneTest", "org.eclipse.che.examples.AppAnotherTest");
    assertThat(espected, hasItems(result.get(0), result.get(1)));
  }

  @Test
  @Ignore
  public void testClassesShouldBeFoundInTheProject() throws Exception {
    String contextType = "PROJECT";
    String projectUri = getResourceUriAsString(project.getRawLocationURI());
    String testMethodAnnotation = "org.junit.Test";
    String testClassAnnotation = "";

    List<Object> arguments =
        asList(contextType, projectUri, testMethodAnnotation, testClassAnnotation);

    List<String> result = find(arguments);
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
    String fileUri = ResourceUtils.fixURI(uri);
    JavaLanguageServerPlugin.logInfo(
        "*******************************************Resolved File URI --> " + fileUri);
    return fileUri;
  }
}
