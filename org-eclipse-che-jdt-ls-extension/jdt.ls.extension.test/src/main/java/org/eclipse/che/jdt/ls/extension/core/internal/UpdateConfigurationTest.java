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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_IMPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsConfiguration;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/** @author Anatolii Bazko */
public class UpdateConfigurationTest extends AbstractProjectsManagerBasedTest {

  @Test
  public void shouldGetSpecificOptions() throws Exception {
    List<Object> params = singletonList(COMPILER_PB_UNUSED_IMPORT);

    JdtLsConfiguration configuration =
        GetConfigurationCommand.execute(params, new NullProgressMonitor());

    assertFalse(configuration.getJdtLsPreferences().isEmpty());
    assertEquals(1, configuration.getJavaCoreOptions().size());
  }

  @Test
  public void shouldGetAllOptions() throws Exception {
    JdtLsConfiguration configuration =
        GetConfigurationCommand.execute(emptyList(), new NullProgressMonitor());

    assertFalse(configuration.getJdtLsPreferences().isEmpty());
    assertTrue(configuration.getJavaCoreOptions().size() > 1);
  }

  @Test
  public void shouldUpdateConfiguration() throws Exception {
    JdtLsConfiguration configuration = new JdtLsConfiguration();
    configuration.setJavaCoreOptions(singletonMap(COMPILER_PB_UNUSED_IMPORT, "error"));

    UpdateConfigurationCommand.execute(singletonList(configuration), new NullProgressMonitor());

    List<Object> params = singletonList(COMPILER_PB_UNUSED_IMPORT);
    configuration = GetConfigurationCommand.execute(params, new NullProgressMonitor());
    assertEquals("error", configuration.getJavaCoreOptions().get(COMPILER_PB_UNUSED_IMPORT));
  }
}
