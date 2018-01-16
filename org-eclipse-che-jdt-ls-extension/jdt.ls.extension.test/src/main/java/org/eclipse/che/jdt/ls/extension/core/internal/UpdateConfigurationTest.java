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
import static org.junit.Assert.assertFalse;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsConfiguration;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/** @author Anatolii Bazko */
public class UpdateConfigurationTest extends AbstractProjectsManagerBasedTest {

  @Test
  public void shouldUpdateConfiguration() throws Exception {
    JdtLsConfiguration configuration =
        GetConfigurationCommand.execute(emptyList(), new NullProgressMonitor());

    assertFalse(configuration.getPreferences().isEmpty());
    assertFalse(configuration.getJavaCoreOptions().isEmpty());

    List<Object> params = singletonList(configuration);
    SetConfigurationCommand.execute(params, new NullProgressMonitor());
  }
}
