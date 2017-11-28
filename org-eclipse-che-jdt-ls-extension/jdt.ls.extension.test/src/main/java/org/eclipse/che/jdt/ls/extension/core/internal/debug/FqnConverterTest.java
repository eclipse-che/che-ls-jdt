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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.LocationParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

public class FqnConverterTest extends AbstractProjectsManagerBasedTest {

  private static final String PROJECT = "/debugproject";
  private static final String TEST_CLASS =
      PROJECT + "/src/main/java/org/eclipse/che/examples/HelloWorld.java";
  private static final String FQN = "org.eclipse.che.examples.HelloWorld";

  @Before
  public void setup() throws Exception {
    importProjects("maven/debugproject");
  }

  @Test
  public void shouldConvertSimpleLocation() throws Exception {
    List<LocationParameters> result =
        FqnConverter.fqnToLocation(asList(FQN, "15"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), 15);
    assertEquals(location.getTarget(), TEST_CLASS);
    assertEquals(location.getProjectPath(), PROJECT);
    assertEquals(location.getLibId(), 0);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }

  @Test
  public void shouldConvertExternalLibLocation() throws Exception {
    List<LocationParameters> result =
        FqnConverter.fqnToLocation(asList("java.lang.String", "100"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), 100);
    assertEquals(location.getTarget(), "java.lang.String");
    assertNotNull(location.getProjectPath());
    assertTrue(location.getLibId() != 0);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, "java.lang.String");
  }

  @Test
  public void shouldConvertInnerClassLocation() throws Exception {
    List<LocationParameters> result =
        FqnConverter.fqnToLocation(asList(FQN + "$InnerClass", "35"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 35);
    assertEquals(location.getTarget(), TEST_CLASS);
    assertEquals(location.getProjectPath(), PROJECT);
    assertEquals(location.getLibId(), 0);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, FQN + "$InnerClass");
  }

  @Test
  public void shouldConvertAnonymousClassLocation() throws Exception {
    List<LocationParameters> result =
        FqnConverter.fqnToLocation(asList(FQN + "$1", "22"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 22);
    assertEquals(location.getTarget(), TEST_CLASS);
    assertEquals(location.getProjectPath(), PROJECT);
    assertEquals(location.getLibId(), 0);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, FQN + "$1");
  }

  @Test
  public void shouldConvertLocationInsideLambdaToFqn() throws Exception {
    List<LocationParameters> result =
        FqnConverter.fqnToLocation(asList(FQN, "29"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 29);
    assertEquals(location.getTarget(), TEST_CLASS);
    assertEquals(location.getProjectPath(), PROJECT);
    assertEquals(location.getLibId(), 0);

    String fqn = FqnConverter.locationToFqn(singletonList(location), new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }
}
