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
import static org.eclipse.che.jdt.ls.extension.core.internal.debug.FqnConverter.fqnToLocation;
import static org.eclipse.che.jdt.ls.extension.core.internal.debug.FqnConverter.locationToFqn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.LocationParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.junit.Before;
import org.junit.Test;

public class FqnConverterTest extends AbstractProjectsManagerBasedTest {
  private static final String FILE = "/src/main/java/org/eclipse/che/examples/HelloWorld.java";
  private static final String FQN = "org.eclipse.che.examples.HelloWorld";

  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/debugproject");
    project = WorkspaceHelper.getProject("debugproject");
  }

  @Test
  public void shouldConvertSimpleLocation() throws Exception {
    List<LocationParameters> result = fqnToLocation(asList(FQN, "15"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), 15);
    assertTrue(location.getFilePath().endsWith("maven/debugproject" + FILE));
    assertEquals(location.getLibId(), 0);
    assertNull(location.getFqn());

    String fqn = locationToFqn(asList(createFileUri(FILE), "15"), new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }

  @Test
  public void shouldConvertExternalLibLocation() throws Exception {
    List<LocationParameters> result =
        fqnToLocation(asList("java.lang.String", "100"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);
    assertEquals(location.getLineNumber(), 100);
    assertNull(location.getFilePath());
    assertEquals(location.getFqn(), "java.lang.String");
    assertTrue(location.getLibId() != 0);

    String fqn = locationToFqn(asList(location.getFqn(), "15"), new NullProgressMonitor());

    assertEquals(fqn, "java.lang.String");
  }

  @Test
  public void shouldConvertInnerClassLocation() throws Exception {
    List<LocationParameters> result =
        fqnToLocation(asList(FQN + "$InnerClass", "35"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 35);
    assertTrue(location.getFilePath().endsWith("maven/debugproject" + FILE));
    assertEquals(location.getLibId(), 0);
    assertNull(location.getFqn());

    String fqn = locationToFqn(asList(createFileUri(FILE), "35"), new NullProgressMonitor());

    assertEquals(fqn, FQN + "$InnerClass");
  }

  @Test
  public void shouldConvertAnonymousClassLocation() throws Exception {
    List<LocationParameters> result =
        fqnToLocation(asList(FQN + "$1", "22"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 22);
    assertTrue(location.getFilePath().endsWith("maven/debugproject" + FILE));
    assertEquals(location.getLibId(), 0);
    assertNull(location.getFqn());

    String fqn = locationToFqn(asList(createFileUri(FILE), "22"), new NullProgressMonitor());

    assertEquals(fqn, FQN + "$1");
  }

  @Test
  public void shouldConvertLocationInsideLambdaToFqn() throws Exception {
    List<LocationParameters> result = fqnToLocation(asList(FQN, "29"), new NullProgressMonitor());

    assertEquals(result.size(), 1);

    LocationParameters location = result.get(0);

    assertEquals(location.getLineNumber(), 29);
    assertTrue(location.getFilePath().endsWith("maven/debugproject" + FILE));
    assertEquals(location.getLibId(), 0);
    assertNull(location.getFqn());

    String fqn = locationToFqn(asList(createFileUri(FILE), "29"), new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return ResourceUtils.fixURI(uri);
  }
}
