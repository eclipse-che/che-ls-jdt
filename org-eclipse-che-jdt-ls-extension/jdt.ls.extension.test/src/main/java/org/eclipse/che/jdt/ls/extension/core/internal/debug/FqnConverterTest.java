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
package org.eclipse.che.jdt.ls.extension.core.internal.debug;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.LocationParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;
import org.junit.Before;
import org.junit.Test;

public class FqnConverterTest extends AbstractProjectsManagerBasedTest {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  @Before
  public void setup() throws Exception {
    importProjects("maven/debugproject");
  }

  @Test
  public void shouldConvertFqnToLocation() throws Exception {
    final String fqn = "org.eclipse.che.examples.HelloWorld";
    final int lineNumber = 15;
    final List<Object> params = Arrays.asList(fqn, lineNumber);

    List<LocationParameters> result = FqnConverter.fqnToLocation(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), lineNumber);
    assertEquals(
        location.getTarget(),
        "/debugproject/src/main/java/org/eclipse/che/examples/HelloWorld.java");
    assertEquals(location.getProjectPath(), "/debugproject");
    assertEquals(location.getLibId(), -1);
  }

  @Test
  public void shouldConvertExternalLibFqnToLocationAndBack() throws Exception {
    final String target = "java.lang.String";
    final int lineNumber = 100;
    final List<Object> params = Arrays.asList(target, lineNumber);

    List<LocationParameters> result = FqnConverter.fqnToLocation(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), lineNumber);
    assertEquals(location.getTarget(), "java.lang.String");
    assertNotNull(location.getProjectPath());
    assertTrue(location.getLibId() != -1);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, target);
  }

  @Test
  public void shouldConvertLocationToFqn() throws Exception {
    LocationParameters location =
        new LocationParameters(
            "/debugproject/src/main/java/org/eclipse/che/examples/HelloWorld.java",
            15,
            "/debugproject");

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, "org.eclipse.che.examples.HelloWorld");
  }
}
