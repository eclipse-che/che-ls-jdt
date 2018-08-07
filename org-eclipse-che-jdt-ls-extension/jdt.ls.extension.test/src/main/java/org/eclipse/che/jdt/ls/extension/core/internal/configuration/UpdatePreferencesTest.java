/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal.configuration;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_IMPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JdtLsPreferences;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.preferences.Preferences;
import org.junit.Test;

/** @author Anatolii Bazko */
public class UpdatePreferencesTest extends AbstractProjectsManagerBasedTest {

  @Test
  public void shouldUpdatePreferences() throws Exception {
    List<Object> params = singletonList(COMPILER_PB_UNUSED_IMPORT);
    JdtLsPreferences jdtLsPreferences =
        GetPreferencesCommand.execute(params, new NullProgressMonitor());
    assertNotEquals("false", jdtLsPreferences.getPreferences().get(Preferences.RENAME_ENABLED_KEY));

    jdtLsPreferences = new JdtLsPreferences(singletonMap(Preferences.RENAME_ENABLED_KEY, "false"));
    UpdatePreferencesCommand.execute(singletonList(jdtLsPreferences), new NullProgressMonitor());

    params = singletonList(COMPILER_PB_UNUSED_IMPORT);
    jdtLsPreferences = GetPreferencesCommand.execute(params, new NullProgressMonitor());
    assertEquals("false", jdtLsPreferences.getPreferences().get(Preferences.RENAME_ENABLED_KEY));
  }
}
