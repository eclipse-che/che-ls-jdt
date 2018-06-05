/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
  public static final String GET_MAVEN_PROJECTS_COMMAND = "che.jdt.ls.extension.mavenProjects";
  public static final String RECOMPUTE_POM_DIAGNOSTICS = "che.jdt.ls.extension.pom.diagnostics";
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

  // debug

  public static final String FIND_RESOURCES_BY_FQN =
      "che.jdt.ls.extension.debug.findResourcesByFqn";
  public static final String IDENTIFY_FQN_IN_RESOURCE =
      "che.jdt.ls.extension.debug.identifyFqnInResource";
  public static final String USAGES_COMMAND = "che.jdt.ls.extension.usages";

  public static final String UPDATE_WORKSPACE = "che.jdt.ls.extension.updateWorkspace";

  // simple java project

  public static final String CREATE_SIMPLE_PROJECT = "che.jdt.ls.extension.plain.createProject";
  public static final String UPDATE_PROJECT_CLASSPATH =
      "che.jdt.ls.extension.plain.updateClasspath";
  public static final String GET_SOURCE_FOLDERS = "che.jdt.ls.extension.plain.sourceFolders";

  // navigation commands

  public static final String FIND_IMPLEMENTERS_COMMAND = "che.jdt.ls.extension.findImplementers";

  // configuration

  public static final String GET_JAVA_CORE_OPTIONS_СOMMAND =
      "che.jdt.ls.extension.configuration.getJavaCoreOptions";
  public static final String UPDATE_JAVA_CORE_OPTIONS_СOMMAND =
      "che.jdt.ls.extension.configuration.updateJavaCoreOptions";
  public static final String GET_PREFERENCES_СOMMAND =
      "che.jdt.ls.extension.configuration.getPreferences";
  public static final String UPDATE_PREFERENCES_СOMMAND =
      "che.jdt.ls.extension.configuration.updatePreferences";

  // import

  public static final String ORGANIZE_IMPORTS = "che.jdt.ls.extension.import.organizeImports";

  // Refactoring

  public static final String RENAME_COMMAND = "che.jdt.ls.extension.refactoring.rename";
  public static final String GET_RENAME_TYPE_COMMAND =
      "che.jdt.ls.extension.refactoring.get.rename.type";
  public static final String VALIDATE_RENAMED_NAME_COMMAND =
      "che.jdt.ls.extension.refactoring.validate.renamed.name";
  public static final String GET_LINKED_ELEMENTS_COMMAND =
      "che.jdt.ls.extension.refactoring.get.linked.elements";

  private Commands() {}
}
