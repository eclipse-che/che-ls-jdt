/*
 * Copyright (c) 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;
import org.eclipse.che.jdt.ls.extension.api.dto.SearchResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.jdt.core.ITypeRoot;
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
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.hover.JavaElementLabels;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentPositionParams;

public class UsagesCommand {
  private static final Set<Integer> INTERESTING_ELEMENT_TYPES;

  static {
    INTERESTING_ELEMENT_TYPES = new HashSet<>();
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.METHOD);
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.INITIALIZER);
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.TYPE);
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.CLASS_FILE);
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.COMPILATION_UNIT);
    INTERESTING_ELEMENT_TYPES.add(IJavaElement.PACKAGE_FRAGMENT);
  }

  public static List<UsagesResponse> execute(List<Object> parameters, IProgressMonitor pm) {
    TextDocumentPositionParams param =
        JavaModelUtil.convertCommandParameter(parameters.get(0), TextDocumentPositionParams.class);
    SearchEngine engine = new SearchEngine();

    Map<IJavaElement, SearchResult> results = new HashMap<IJavaElement, SearchResult>();
    try {
      ITypeRoot typeRoot = JDTUtils.resolveTypeRoot(param.getTextDocument().getUri());
      IJavaElement elementToSearch =
          JDTUtils.findElementAtSelection(
              typeRoot,
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
      if (isInsideJRE(typeRoot)) {
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
                SearchResult result = ensureCreated(element);
                result.getMatches().add(new LinearRange(match.getOffset(), match.getLength()));
              }
            }

            private SearchResult ensureCreated(IJavaElement element) throws JavaModelException {
              try {
                while (element != null && !isInteresting(element)) {
                  // transparent elements don't show up in result
                  element = element.getParent();
                }
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
                    IJavaElement parent = element.getParent();
                    IResource resource = parent.getCorrespondingResource();
                    if (resource != null) {
                      r.setUri(ResourceUtils.fixURI(resource.getLocationURI()));
                    } else {
                      r.setUri(
                          new URI(
                                  "jdt",
                                  "/"
                                      + parent.getJavaProject().getElementName()
                                      + "/"
                                      + parent.getElementName(),
                                  element.getElementName())
                              .toString());
                    }
                  }
                  results.put(element, r);
                }
                return r;
              } catch (URISyntaxException e) {
                JavaLanguageServerPlugin.logException("Uri syntax should not happen", e);
                return null;
              }
            }

            private boolean isInteresting(IJavaElement element) {
              return INTERESTING_ELEMENT_TYPES.contains(element.getElementType());
            }
          },
          pm);

      String searchTerm =
          JavaElementLabels.getElementLabel(elementToSearch, JavaElementLabels.ALL_DEFAULT);
      SymbolKind elementKind = JavaModelUtil.mapKind(elementToSearch);
      return Collections.singletonList(
          new UsagesResponse(
              searchTerm,
              elementKind,
              results
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getKey() instanceof IPackageFragment)
                  .map(e -> e.getValue())
                  .collect(Collectors.toList())));
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
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
