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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UsagesTest extends AbstractProjectsManagerBasedTest {
  private IProject project;

  @Before
  public void importProjects() throws Exception {
    importProjects("maven/usages");
    project = WorkspaceHelper.getProject("usages");
  }

  @Test
  public void testFindImplementation() {
    TextDocumentPositionParams parameter =
        createParameter("src/main/java/pkg1/AInterface.java", 2, 22);
    UsagesResponse usages =
        UsagesCommand.execute(Collections.singletonList(parameter), new NullProgressMonitor())
            .get(0);
    assertEquals(SymbolKind.Interface, usages.getElementKind());
    assertEquals("AInterface", usages.getSearchedElement());
    List<LinearRange> matches = collectMatches(usages.getSearchResults());
    assertEquals(1, matches.size());
    assertEquals(new LinearRange(46, 10), matches.get(0));
  }

  @Test
  public void testFindConstructorReference() {
    TextDocumentPositionParams parameter = createParameter("src/main/java/pkg1/AClass.java", 3, 12);
    UsagesResponse usages =
        UsagesCommand.execute(Collections.singletonList(parameter), new NullProgressMonitor())
            .get(0);
    assertEquals(SymbolKind.Method, usages.getElementKind());
    assertEquals("AClass()", usages.getSearchedElement());
    List<LinearRange> matches = collectMatches(usages.getSearchResults());
    assertEquals(1, matches.size());
    assertEquals(new LinearRange(83, 12), matches.get(0));
  }

  @Test
  public void testFindTestAnnotation() {
    TextDocumentPositionParams parameter = createParameter("src/test/java/pkg1/ATest.java", 6, 5);
    UsagesResponse usages =
        UsagesCommand.execute(Collections.singletonList(parameter), new NullProgressMonitor())
            .get(0);
    assertEquals(SymbolKind.Interface, usages.getElementKind());
    assertEquals("Test", usages.getSearchedElement());
    findResult(
        usages.getSearchResults(),
        "src/test/java/pkg1/ATest.java",
        "ATest.java",
        47,
        14,
        Assert::assertTrue);
    findResult(
        usages.getSearchResults(), "TestMethod.class", "TestMethod", 250, 14, Assert::assertTrue);
  }

  private void findResult(
      List<SearchResult> searchResults,
      String path,
      String name,
      int offset,
      int length,
      BiConsumer<String, Boolean> ifFound) {
    LinearRange range = new LinearRange(offset, length);
    for (SearchResult res : searchResults) {
      if (!iterate(
          res,
          SearchResult::getChildren,
          r -> {
            if (r.getUri().contains(path) && r.getName().equals(name)) {
              ifFound.accept(
                  "no match " + range + " found in " + path, r.getMatches().contains(range));
              return false;
            }
            return true;
          })) {
        return;
      }
      ;
    }
    ifFound.accept("No matches in " + path, false);
  }

  private TextDocumentPositionParams createParameter(
      String workspacePath, int line, int character) {
    URI locationURI = project.findMember(workspacePath).getLocationURI();
    return new TextDocumentPositionParams(
        new TextDocumentIdentifier(locationURI.toString()), new Position(line, character));
  }

  private List<LinearRange> collectMatches(List<SearchResult> results) {
    List<LinearRange> matches = new ArrayList<LinearRange>();
    for (SearchResult result : results) {
      iterate(
          result,
          SearchResult::getChildren,
          res -> {
            matches.addAll(res.getMatches());
            return true;
          });
    }
    return matches;
  }

  private <T> boolean iterate(
      T root, Function<T, List<T>> childrenAccessor, Function<T, Boolean> elementHandler) {
    if (!elementHandler.apply(root)) {
      return false;
    }
    for (T child : childrenAccessor.apply(root)) {
      if (!iterate(child, childrenAccessor, elementHandler)) {
        return false;
      }
      ;
    }
    return true;
  }
}
