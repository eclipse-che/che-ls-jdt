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

  private Commands() {}
}
