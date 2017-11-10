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

import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.classpath.ResolveClassPathsHandler;
import org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestDetectionHandler;
import org.eclipse.che.jdt.ls.extension.core.internal.testdetection.TestFinderHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

/**
 * Implementation of {@link IDelegateCommandHandler} which handles custom commands. For each
 * supported command should be registered its id into plugin.xml.
 */
public class CheDelegateCommandHandler implements IDelegateCommandHandler {

  public static final String TEST_DETECT_COMMAND_ID = "che.jdt.ls.extension.testdetect";

  public static final String TEST_FIND_COMMAND_ID = "che.jdt.ls.extension.testfind";

  public static final String RESOLVE_CLASSPATH_COMMAND_ID = "che.jdt.ls.extension.resolveclasspath";

  public static final String GET_OUTPUT_DIR_ID = "che.jdt.ls.extension.outputdir";

  @Override
  public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress)
      throws Exception {
    if (TEST_DETECT_COMMAND_ID.equals(commandId)) {
      TestDetectionHandler testDetectionHandler = new TestDetectionHandler();
      return testDetectionHandler.detectTests(arguments);
    } else if (TEST_FIND_COMMAND_ID.equals(commandId)) {
      TestFinderHandler testFinderHandler = new TestFinderHandler();
      return testFinderHandler.findTests(arguments);
    } else if (RESOLVE_CLASSPATH_COMMAND_ID.equals(commandId)) {
      ResolveClassPathsHandler resolveClasspathsHandler = new ResolveClassPathsHandler();
      return resolveClasspathsHandler.resolveClasspaths(arguments);
    } else if (GET_OUTPUT_DIR_ID.equals(commandId)) {
      ResolveClassPathsHandler resolveClasspathsHandler = new ResolveClassPathsHandler();
      return resolveClasspathsHandler.getOutputDirectory(arguments);
    }
    throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
  }
}
