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
package org.eclipse.che.jdt.ls.extension.core.internal.debug;

import static java.util.Arrays.asList;
import static org.eclipse.che.jdt.ls.extension.core.internal.debug.FqnDiscover.findResourcesByFqn;
import static org.eclipse.che.jdt.ls.extension.core.internal.debug.FqnDiscover.identifyFqnInResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ResourceLocation;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Before;
import org.junit.Test;

public class FqnDiscoverTest extends AbstractProjectsManagerBasedTest {
  private static final String FILE = "/src/main/java/org/eclipse/che/examples/HelloWorld.java";
  private static final String FQN = "org.eclipse.che.examples.HelloWorld";

  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/debugproject");
    project = WorkspaceHelper.getProject("debugproject");
  }

  @Test
  public void shouldHandleSimpleFqn() throws Exception {
    List<Object> params = asList(FQN, "15");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertTrue(location.getLeft().endsWith("maven/debugproject" + FILE));
    assertNull(location.getRight());

    params = asList(createFileUri(FILE), "15");
    String fqn = identifyFqnInResource(params, new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }

  @Test
  public void shouldHandleSimpleFqnInExternalLibrary() throws Exception {
    List<Object> params = asList("java.lang.String", "100");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertEquals(location.getRight().getFqn(), "java.lang.String");
    assertNotNull(location.getRight().getLibId());
    assertNull(location.getLeft());
  }

  @Test
  public void shouldHandleInnerClassFqn() throws Exception {
    List<Object> params = asList(FQN + "$InnerClass", "35");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertTrue(location.getLeft().endsWith("maven/debugproject" + FILE));
    assertNull(location.getRight());

    params = asList(createFileUri(FILE), "35");
    String fqn = identifyFqnInResource(params, new NullProgressMonitor());

    assertEquals(fqn, FQN + "$InnerClass");
  }

  @Test
  public void shouldHandleAnonymousClassFqn() throws Exception {
    List<Object> params = asList(FQN + "$1", "22");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertTrue(location.getLeft().endsWith("maven/debugproject" + FILE));
    assertNull(location.getRight());

    params = asList(createFileUri(FILE), "22");
    String fqn = identifyFqnInResource(params, new NullProgressMonitor());

    assertEquals(fqn, FQN + "$1");
  }

  @Test
  public void shouldHandleFqnInsideLambda() throws Exception {
    List<Object> params = asList(FQN, "29");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertTrue(location.getLeft().endsWith("maven/debugproject" + FILE));
    assertNull(location.getRight());

    params = asList(createFileUri(FILE), "29");
    String fqn = identifyFqnInResource(params, new NullProgressMonitor());

    assertEquals(fqn, FQN);
  }

  @Test
  public void shouldHandleLocalClasses() throws Exception {
    List<Object> params = asList(FQN + "$1LocalClass1", "42");
    List<Either<String, ResourceLocation>> result =
        findResourcesByFqn(params, new NullProgressMonitor());

    assertEquals(result.size(), 1);

    Either<String, ResourceLocation> location = result.get(0);
    assertTrue(location.getLeft().endsWith("maven/debugproject" + FILE));
    assertNull(location.getRight());

    params = asList(createFileUri(FILE), "48");
    String fqn = identifyFqnInResource(params, new NullProgressMonitor());

    assertEquals(fqn, FQN + "$2LocalClass2");
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return ResourceUtils.fixURI(uri);
  }
}
