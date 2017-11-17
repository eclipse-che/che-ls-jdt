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
package org.eclipse.che.jdt.ls.extension.core.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.hover.JavaElementLabels;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;

public class ReferencesCommand {
  public static List<SearchResult> execute(List<Object> parameters, IProgressMonitor pm) {

    TextDocumentPositionParams param =
        JavaModelUtil.convertCommandParameter(parameters.get(0), TextDocumentPositionParams.class);
    SearchEngine engine = new SearchEngine();

    Map<IJavaElement, SearchResult> results = new HashMap<IJavaElement, SearchResult>();
    try {
      IJavaElement elementToSearch =
          JDTUtils.findElementAtSelection(
              JDTUtils.resolveTypeRoot(param.getTextDocument().getUri()),
              param.getPosition().getLine(),
              param.getPosition().getCharacter(),
              JavaLanguageServerPlugin.getPreferencesManager(),
              pm);

      if (elementToSearch == null) {
        return Collections.emptyList();
      }
      IJavaProject[] projects =
          JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      int flags = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES;
      if (isInsideJRE(elementToSearch)) {
        flags |= IJavaSearchScope.SYSTEM_LIBRARIES;
      }
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(projects, flags);

      SearchPattern pattern =
          SearchPattern.createPattern(elementToSearch, IJavaSearchConstants.REFERENCES);
      engine.search(
          pattern,
          new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
          scope,
          new SearchRequestor() {

            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              Object o = match.getElement();
              if (o instanceof IJavaElement) {
                IJavaElement element = (IJavaElement) o;
                Location location = JDTUtils.toLocation(element);
                SearchResult result = ensureCreated(element);
                result.getMatches().add(location.getRange());
              }
            }

            private SearchResult ensureCreated(IJavaElement element) throws JavaModelException {
              try {
                SearchResult r = results.get(element);
                if (r == null) {
                  r = new SearchResult();
                  r.setChildren(new ArrayList<>());
                  r.setMatches(new ArrayList<>());
                  r.setKind(JavaModelUtil.mapKind(element));
                  r.setName(
                      JavaElementLabels.getElementLabel(element, JavaElementLabels.ALL_DEFAULT));
                  if (element instanceof ISourceReference) {
                    Location l = JDTUtils.toLocation(element);
                    r.setUri(l.getUri());
                    SearchResult parent = ensureCreated(element.getParent());
                    parent.getChildren().add(r);
                  } else if (element instanceof IPackageFragment) {
                    r.setUri(
                        new URI(
                                "jdt",
                                "/"
                                    + element.getJavaProject().getElementName()
                                    + "/"
                                    + element.getParent().getElementName(),
                                element.getElementName())
                            .toString());
                  }
                }
                return r;
              } catch (URISyntaxException e) {
                JavaLanguageServerPlugin.logException("Uri syntax should not happen", e);
                return null;
              }
            }
          },
          pm);

    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
    return results
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey() instanceof IPackageFragment)
        .map(e -> e.getValue())
        .collect(Collectors.toList());
  }

  private static boolean isInsideJRE(IJavaElement element) {
    IPackageFragmentRoot root =
        (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (root != null) {
      try {
        IClasspathEntry entry = root.getRawClasspathEntry();
        if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
          IClasspathContainer container =
              JavaCore.getClasspathContainer(entry.getPath(), root.getJavaProject());
          return container != null && container.getKind() == IClasspathContainer.K_DEFAULT_SYSTEM;
        }
        return false;
      } catch (JavaModelException e) {
        JavaLanguageServerPlugin.log(e);
      }
    }
    return true; // include JRE in doubt
  }
}
