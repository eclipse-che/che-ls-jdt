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

  private static final String TEST_DETECT = "che.jdt.ls.extension.testdetect";

  private static final String TEST_FIND = "che.jdt.ls.extension.testfind";

  private static final String RESOLVE_CLASSPATH = "che.jdt.ls.extension.resolveclasspath";

  private static final String GET_OUTPUT_DIR = "che.jdt.ls.extension.outputdir";

  @Override
  public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor progress)
      throws Exception {
    switch (commandId) {
      case TEST_DETECT:
        return TestDetectionHandler.detect(arguments);
      case TEST_FIND:
        return TestFinderHandler.find(arguments);
      case RESOLVE_CLASSPATH:
        return ResolveClassPathsHandler.resolveClasspaths(arguments);
      case GET_OUTPUT_DIR:
        return ResolveClassPathsHandler.getOutputDirectory(arguments);
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported command '%s'!", commandId));
    }
  }
}
