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

import static java.util.Collections.emptyList;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.resolveCompilationUnit;

import java.util.List;

/** Class for finding test methods in the different areas. */
public class JavaTestFinderHandler {
  private static final String FILE_CONTEXT_TYPE = "FILE";
  private static final String FOLDER_CONTEXT_TYPE = "FOLDER";
  private static final String SET_CONTEXT_TYPE = "SET";
  private static final String PROJECT_CONTEXT_TYPE = "PROJECT";
  private static final String CURSOR_POSITION_CONTEXT_TYPE = "CURSOR_POSITION";

  private String fileUri;
  private String projectUri;
  private String testMethodAnnotation;
  private String testClassAnnotation;
  private String contextType;
  private List<String> classes;
  private int cursorOffset;

  /**
   * Finds tests in some area. The area is defined by first parameter of arguments - it is context
   * type.
   *
   * <p>The context type can be:
   *
   * <ul>
   *   <li>FILE - find tests in the file
   *   <li>FOLDER - find tests in the folder/package
   *   <li>SET - find tests in custom set of classes
   *   <li>PROJECT - find tests in the project
   *   <li>CURSOR_POSITION - find class in cursor position
   * </ul>
   *
   * The values of arguments are:
   *
   * <p>0: context type (String) 1: project URI (String) 2: fqn of test method annotation (String)
   * 3: fqn of test class annotation (String). If context type is CURSOR_POSITION 4: file URI
   * (String) 5: cursor offset (double) If context type is FILE or FOLDER 4: file URI (String) If
   * contex type is SET 4: list of classes (List<String>)
   *
   * @param arguments list of arguments
   * @return test methods' declaration
   */
  public Object findTests(List<Object> arguments) {
    readArguments(arguments);
    JavaTestFinder finder = new JavaTestFinder();

    switch (contextType) {
      case FILE_CONTEXT_TYPE:
        return finder.findTestClassDeclaration(resolveCompilationUnit(fileUri));
      case FOLDER_CONTEXT_TYPE:
        return finder.findTestClassesInPackage(fileUri, testMethodAnnotation, testClassAnnotation);
      case SET_CONTEXT_TYPE:
        return finder.getFqnsOfClasses(classes);
      case PROJECT_CONTEXT_TYPE:
        return finder.findTestClassesInProject(
            projectUri, testMethodAnnotation, testClassAnnotation);
      case CURSOR_POSITION_CONTEXT_TYPE:
        return finder.findTestMethodDeclaration(resolveCompilationUnit(fileUri), cursorOffset);
    }

    return emptyList();
  }

  private void readArguments(List<Object> arguments) {
    contextType = (String) arguments.get(0);
    projectUri = (String) arguments.get(1);
    testMethodAnnotation = (String) arguments.get(2);
    testClassAnnotation = (String) arguments.get(3);
    switch (contextType) {
      case CURSOR_POSITION_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        cursorOffset = ((Double) arguments.get(5)).intValue();
        break;
      case FILE_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        break;
      case FOLDER_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        break;
      case SET_CONTEXT_TYPE:
        classes = (List<String>) arguments.get(4);
        break;
    }
  }
}
