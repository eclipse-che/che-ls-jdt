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
package org.eclipse.che.jdt.ls.extension.core.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.core.internal.classpath.ResolveClassPathsHandler;
import org.eclipse.che.jdt.ls.extension.core.internal.debug.FqnDiscover;
import org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestDetectionHandler;
import org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

/**
 * Implementation of {@link IDelegateCommandHandler} which handles custom commands. For each
 * supported command should be registered its id into plugin.xml.
 */
public class CheDelegateCommandHandler implements IDelegateCommandHandler {
  private static final Map<String, BiFunction<List<Object>, IProgressMonitor, ? extends Object>>
      commands;

  static {
    commands = new HashMap<String, BiFunction<List<Object>, IProgressMonitor, ? extends Object>>();
    commands.put(Commands.HELLO_WORLD_COMMAND, (params, progress) -> "Hello World");
    commands.put(Commands.FILE_STRUCTURE_COMMAND, FileStructureCommand::execute);
    commands.put(Commands.TEST_DETECT_COMMAND, TestDetectionHandler::detect);
    commands.put(Commands.FIND_TEST_BY_CURSOR_COMMAND, TestFinderHandler::getTestByCursorPosition);
    commands.put(
        Commands.FIND_TESTS_FROM_PROJECT_COMMAND, TestFinderHandler::getClassesFromProject);
    commands.put(Commands.FIND_TESTS_FROM_FOLDER_COMMAND, TestFinderHandler::getClassesFromFolder);
    commands.put(Commands.FIND_TESTS_FROM_ENTRY_COMMAND, TestFinderHandler::getClassesFromSet);
    commands.put(Commands.FIND_TESTS_IN_FILE_COMMAND, TestFinderHandler::getClassFqn);
    commands.put(Commands.RESOLVE_CLASSPATH_COMMAND, ResolveClassPathsHandler::resolveClasspaths);
    commands.put(Commands.GET_OUTPUT_DIR_COMMAND, ResolveClassPathsHandler::getOutputDirectory);
    commands.put(Commands.IDENTIFY_FQN_IN_RESOURCE, FqnDiscover::identifyFqnInResource);
    commands.put(Commands.FIND_RESOURCES_BY_FQN, FqnDiscover::findResourcesByFqn);
  }

  @Override
  public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress)
      throws Exception {
    BiFunction<List<Object>, IProgressMonitor, ? extends Object> command = commands.get(commandId);
    if (command != null) {
      return command.apply(arguments, progress);
    }
    throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
  }
}
