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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.jdt.core.JavaCore.COMPILER_PB_UNUSED_IMPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

/** @author Anatolii Bazko */
public class UpdateJavaCoreOptionsTest extends AbstractProjectsManagerBasedTest {

  @Test
  public void shouldGetSpecificOptions() throws Exception {
    List<Object> params = singletonList(COMPILER_PB_UNUSED_IMPORT);

    JavaCoreOptions javaCoreOptions =
        GetJavaCoreOptionsCommand.execute(params, new NullProgressMonitor());

    assertEquals(1, javaCoreOptions.getOptions().size());
  }

  @Test
  public void shouldGetAllOptions() throws Exception {
    JavaCoreOptions javaCoreOptions =
        GetJavaCoreOptionsCommand.execute(emptyList(), new NullProgressMonitor());

    assertTrue(javaCoreOptions.getOptions().size() > 1);
  }

  @Test
  public void shouldUpdateJavaCoreOptions() throws Exception {
    List<Object> params = singletonList(COMPILER_PB_UNUSED_IMPORT);
    JavaCoreOptions javaCoreOptions =
        GetJavaCoreOptionsCommand.execute(params, new NullProgressMonitor());
    assertNotEquals("error", javaCoreOptions.getOptions().get(COMPILER_PB_UNUSED_IMPORT));

    javaCoreOptions = new JavaCoreOptions(singletonMap(COMPILER_PB_UNUSED_IMPORT, "error"));
    UpdateJavaCoreOptionsCommand.execute(singletonList(javaCoreOptions), new NullProgressMonitor());

    params = singletonList(COMPILER_PB_UNUSED_IMPORT);
    javaCoreOptions = GetJavaCoreOptionsCommand.execute(params, new NullProgressMonitor());
    assertEquals("error", javaCoreOptions.getOptions().get(COMPILER_PB_UNUSED_IMPORT));
  }
}
