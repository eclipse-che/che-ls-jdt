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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Sample integration test. In Eclipse, right-click > Run As > JUnit-Plugin. <br>
 * In Maven CLI, run "mvn integration-test".
 */
public class CheDelegateCommandHandlerTest {

  private CheDelegateCommandHandler commandHandler;

  @Before
  public void setUp() {
    commandHandler = new CheDelegateCommandHandler();
  }

  @Test
  public void veryStupidTest() throws Exception {
    assertEquals(
        "Hello World",
        commandHandler.executeCommand(CheDelegateCommandHandler.COMMAND_ID, null, null));
  }
}
