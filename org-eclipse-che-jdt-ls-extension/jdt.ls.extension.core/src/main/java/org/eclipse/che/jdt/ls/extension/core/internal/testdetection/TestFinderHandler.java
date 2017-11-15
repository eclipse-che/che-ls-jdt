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

import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.JavaTestFinder.findTestClassDeclaration;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.JavaTestFinder.findTestClassesInPackage;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.JavaTestFinder.findTestClassesInProject;
import static org.eclipse.che.jdt.ls.extension.core.internal.testdetection.JavaTestFinder.findTestMethodDeclaration;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.resolveCompilationUnit;

import java.util.List;
import org.eclipse.jdt.core.ICompilationUnit;

/** Class for finding test methods in the different areas. */
public class TestFinderHandler {
  /**
   * Returns test class declaration by file uri.
   *
   * @param arguments contain value of file URI
   * @return fqn of test class
   */
  public static List<String> getClass(List<Object> arguments) {
    String uriString = (String) arguments.get(0);
    ICompilationUnit unit = resolveCompilationUnit(uriString);
    return findTestClassDeclaration(unit);
  }

  /**
   * Returns test classes from the folder.
   *
   * @param arguments contain folder URI, fqn of test method annotation and fqn of test class
   *     annotation
   * @return fqns of test classes
   */
  public static List<String> getClassesFromFolder(List<Object> arguments) {
    String folderUri = (String) arguments.get(0);
    String testMethodAnnotation = (String) arguments.get(1);
    String testClassAnnotation = (String) arguments.get(2);
    return findTestClassesInPackage(folderUri, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Returns test classes from the project.
   *
   * @param arguments contain project URI, fqn of test method annotation and fqn of test class
   *     annotation
   * @return fqns of test classes
   */
  public static List<String> getClassesFromProject(List<Object> arguments) {
    String projectUri = (String) arguments.get(0);
    String testMethodAnnotation = (String) arguments.get(1);
    String testClassAnnotation = (String) arguments.get(2);
    return findTestClassesInProject(projectUri, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Returns test method declaration by cursor position.
   *
   * @param arguments contain file URI, cursor offset annotation
   * @return returns method declaration if it is a test otherwise returns empty list
   */
  public static List<String> getTestByCursorPosition(List<Object> arguments) {
    String fileUri = (String) arguments.get(0);
    int cursorOffset = ((Double) arguments.get(1)).intValue();
    ICompilationUnit unit = resolveCompilationUnit(fileUri);
    return findTestMethodDeclaration(unit, cursorOffset);
  }

  /**
   * Returns classes's fqns.
   *
   * @param arguments contain list of classes
   * @return returns fqns
   */
  public static List<String> getClassesFromSet(List<Object> arguments) {
    List<String> classes = (List<String>) arguments.get(1);
    return JavaTestFinder.getFqns(classes);
  }
}
