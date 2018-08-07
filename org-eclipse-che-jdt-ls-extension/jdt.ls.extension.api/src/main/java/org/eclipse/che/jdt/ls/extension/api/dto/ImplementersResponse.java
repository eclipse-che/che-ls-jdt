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
package org.eclipse.che.jdt.ls.extension.api.dto;

import java.util.List;
import org.eclipse.lsp4j.SymbolInformation;

/**
 * The class represents implementers of a searched element
 *
 * @author Dmitrii Bocharov
 */
public class ImplementersResponse {

  private String searchedElement;
  private List<SymbolInformation> implementers;

  public ImplementersResponse() {}

  public String getSearchedElement() {
    return this.searchedElement;
  }

  public void setSearchedElement(String searchedElement) {
    this.searchedElement = searchedElement;
  }

  /** Returns all implementations. */
  public List<SymbolInformation> getImplementers() {
    return this.implementers;
  }

  public void setImplementers(List<SymbolInformation> implementers) {
    this.implementers = implementers;
  }
}
