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
  public static final String REIMPORT_MAVEN_PROJECTS_COMMAND =
      "che.jdt.ls.extension.reImportMavenProject";
  public static final String GET_CLASS_PATH_TREE_COMMAND = "che.jdt.ls.extension.classpathTree";

  // External Library commands

  public static final String GET_EXTERNAL_LIBRARIES_COMMAND =
      "che.jdt.ls.extension.externalLibraries";
  public static final String GET_EXTERNAL_LIBRARIES_CHILDREN_COMMAND =
      "che.jdt.ls.extension.externalLibrariesChildren";
  public static final String GET_LIBRARY_CHILDREN_COMMAND = "che.jdt.ls.extension.libraryChildren";
  public static final String GET_LIBRARY_ENTRY_COMMAND = "che.jdt.ls.extension.libraryEntry";
  public static final String GET_LIBRARY_NODE_CONTENT_BY_PATH_COMMAND =
      "che.jdt.ls.extension.libraryContentByPath";

  // debug

  public static final String FIND_RESOURCES_BY_FQN =
      "che.jdt.ls.extension.debug.findResourcesByFqn";
  public static final String IDENTIFY_FQN_IN_RESOURCE =
      "che.jdt.ls.extension.debug.identifyFqnInResource";

  public static final String UPDATE_WORKSPACE = "che.jdt.ls.extension.updateWorkspace";

  private Commands() {}
}
