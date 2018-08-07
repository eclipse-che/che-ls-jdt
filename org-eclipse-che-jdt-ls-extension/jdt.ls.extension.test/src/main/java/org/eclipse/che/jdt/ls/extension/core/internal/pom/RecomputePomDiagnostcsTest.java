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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.AbstractProjectsManagerBasedTest;
import org.eclipse.che.jdt.ls.extension.core.internal.WorkspaceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link RecomputePomDiagnosticsCommand}.
 *
 * @author Valeriy Svydenko
 */
public class RecomputePomDiagnostcsTest extends AbstractProjectsManagerBasedTest {
  private static final String BROKEN_POM = "pom.xml";
  private static final String GOOD_POM = "good.pom.xml";

  private IProject project;

  @Before
  public void setup() throws Exception {
    importProjects("maven/broken");
    project = WorkspaceHelper.getProject("broken");
  }

  @Test
  public void shouldFindDiagnosticsForBrokenPom() throws Exception {
    String uri = createFileUri(BROKEN_POM);
    PublishDiagnosticsParams diagnosticsParams =
        RecomputePomDiagnosticsCommand.computeDiagnostics(uri);

    assertNotNull(diagnosticsParams);

    List<Diagnostic> diagnostics = diagnosticsParams.getDiagnostics();
    assertNotNull(diagnostics);
    assertFalse(diagnostics.isEmpty());

    Comparator<Diagnostic> comparator =
        (Diagnostic d1, Diagnostic d2) -> {
          int diff = d1.getRange().getStart().getLine() - d2.getRange().getStart().getLine();
          if (diff == 0) {
            diff = d1.getMessage().compareTo(d2.getMessage());
          }
          return diff;
        };

    Collections.sort(diagnostics, comparator);
    assertEquals(diagnostics.toString(), 3, diagnostics.size());
    assertTrue(
        diagnostics
            .get(0)
            .getMessage()
            .startsWith(
                "For artifact {org.apache.commons:commons-lang3:null:jar}: The version cannot be empty. (org.apache.maven.plugins:maven-resources-plugin:2.6:resources:default-resources:process-resources)"));
    assertTrue(
        diagnostics
            .get(1)
            .getMessage()
            .startsWith(
                "For artifact {org.apache.commons:commons-lang3:null:jar}: The version cannot be empty. (org.apache.maven.plugins:maven-resources-plugin:2.6:testResources:default-testResources:process-test-resources)"));
    assertEquals(
        "Project build error: 'dependencies.dependency.version' for org.apache.commons:commons-lang3:jar is missing.",
        diagnostics.get(2).getMessage());
  }

  @Test
  public void diagnosticsShouldBeEmptyForGoodPom() throws Exception {
    String uri = createFileUri(GOOD_POM);
    PublishDiagnosticsParams diagnosticsParams =
        RecomputePomDiagnosticsCommand.computeDiagnostics(uri);

    assertNotNull(diagnosticsParams);

    List<Diagnostic> diagnostics = diagnosticsParams.getDiagnostics();
    assertNotNull(diagnostics);
    assertTrue(diagnostics.isEmpty());
  }

  private String createFileUri(String file) {
    URI uri = project.getFile(file).getRawLocationURI();
    return ResourceUtils.fixURI(uri);
  }
}
