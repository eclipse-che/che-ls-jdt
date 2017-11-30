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
package org.eclipse.che.jdt.ls.extension.api;

/**
 * Defines commmand ids for che specific custom commands.
 *
 * @author Thomas Mäder
 */
public class Commands {
  public static final String HELLO_WORLD_COMMAND = "org.eclipse.che.jdt.ls.extension.samplecommand";
  public static final String FILE_STRUCTURE_COMMAND =
      "org.eclipse.che.jdt.ls.extension.filestructure";
  public static final String TEST_DETECT_COMMAND = "che.jdt.ls.extension.detectTest";
  public static final String FIND_TEST_BY_CURSOR_COMMAND = "che.jdt.ls.extension.findTestByCursor";
  public static final String FIND_TESTS_FROM_PROJECT_COMMAND =
      "che.jdt.ls.extension.findTestFromProject";
  public static final String FIND_TESTS_FROM_FOLDER_COMMAND =
      "che.jdt.ls.extension.findTestFromFolder";
  public static final String FIND_TESTS_FROM_ENTRY_COMMAND =
      "che.jdt.ls.extension.findTestFromEntry";
  public static final String FIND_TESTS_IN_FILE_COMMAND = "che.jdt.ls.extension.findTestInFile";
  public static final String RESOLVE_CLASSPATH_COMMAND = "che.jdt.ls.extension.resolveClasspath";
  public static final String GET_OUTPUT_DIR_COMMAND = "che.jdt.ls.extension.outputDir";
  public static final String GET_EFFECTIVE_POM_COMMAND = "che.jdt.ls.extension.effectivePom";

  private Commands() {}
}