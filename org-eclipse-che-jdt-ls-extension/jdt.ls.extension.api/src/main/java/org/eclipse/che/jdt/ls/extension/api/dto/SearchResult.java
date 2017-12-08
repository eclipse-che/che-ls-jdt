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
package org.eclipse.che.jdt.ls.extension.api.dto;

import java.util.List;
import org.eclipse.lsp4j.SymbolKind;

public class SearchResult {
  private SymbolKind kind;
  private String name;
  private String uri;
  private List<SearchResult> children;
  private List<LinearRange> matches;

  public SymbolKind getKind() {
    return kind;
  }

  public void setKind(SymbolKind kind) {
    this.kind = kind;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public List<SearchResult> getChildren() {
    return children;
  }

  public void setChildren(List<SearchResult> children) {
    this.children = children;
  }

  public List<LinearRange> getMatches() {
    return matches;
  }

  public void setMatches(List<LinearRange> matches) {
    this.matches = matches;
  }
}
