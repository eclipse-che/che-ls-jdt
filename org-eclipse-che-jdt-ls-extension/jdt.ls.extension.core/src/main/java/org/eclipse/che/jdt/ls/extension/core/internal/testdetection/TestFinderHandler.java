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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.TestFindParameters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/** Class for finding test methods in the different areas. */
public class TestFinderHandler {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  /**
   * Returns test class declaration by file uri.
   *
   * @param arguments contain value of file URI
   * @param pm a progress monitor
   * @return fqn of test class
   */
  public static List<String> getClassFqn(List<Object> arguments, IProgressMonitor pm) {
    TestFindParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), TestFindParameters.class);

    String uriString = parameters.getSourceUri();
    String methodAnnotation = parameters.getTestMethodAnnotation();
    String classAnnotation = parameters.getTestClassAnnotation();

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    ICompilationUnit unit = resolveCompilationUnit(uriString);
    return findTestClassDeclaration(unit, methodAnnotation, classAnnotation);
  }

  /**
   * Returns test classes from the folder.
   *
   * @param arguments contain folder URI, fqn of test method annotation and fqn of test class
   *     annotation
   * @param pm a progress monitor
   * @return fqns of test classes
   */
  public static List<String> getClassesFromFolder(List<Object> arguments, IProgressMonitor pm) {
    TestFindParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), TestFindParameters.class);

    String folderUri = parameters.getSourceUri();
    String testMethodAnnotation = parameters.getTestMethodAnnotation();
    String testClassAnnotation = parameters.getTestClassAnnotation();

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    return findTestClassesInPackage(folderUri, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Returns test classes from the project.
   *
   * @param arguments contain project URI, fqn of test method annotation and fqn of test class
   *     annotation
   * @param pm a progress monitor
   * @return fqns of test classes
   */
  public static List<String> getClassesFromProject(List<Object> arguments, IProgressMonitor pm) {
    TestFindParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), TestFindParameters.class);

    String projectUri = parameters.getSourceUri();
    String testMethodAnnotation = parameters.getTestMethodAnnotation();
    String testClassAnnotation = parameters.getTestClassAnnotation();

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    return findTestClassesInProject(projectUri, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Returns test method declaration by cursor position.
   *
   * @param arguments contain file URI, cursor offset annotation
   * @param pm a progress monitor
   * @return returns method declaration if it is a test otherwise returns empty list
   */
  public static List<String> getTestByCursorPosition(List<Object> arguments, IProgressMonitor pm) {
    TestFindParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), TestFindParameters.class);

    String fileUri = parameters.getSourceUri();
    int cursorOffset = parameters.getCursorOffset();

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    ICompilationUnit unit = resolveCompilationUnit(fileUri);
    return findTestMethodDeclaration(unit, cursorOffset);
  }

  /**
   * Returns classes's fqns.
   *
   * @param arguments contain list of classes
   * @param pm a progress monitor
   * @return returns fqns
   */
  public static List<String> getClassesFromSet(List<Object> arguments, IProgressMonitor pm) {
    TestFindParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), TestFindParameters.class);

    List<String> classes = parameters.getEntryClasses();

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    return JavaTestFinder.getFqns(classes);
  }
}
