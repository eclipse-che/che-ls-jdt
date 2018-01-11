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

  public void setSearchedElement(String earchedElement) {
    this.searchedElement = earchedElement;
  }

  /** Returns all implementations. */
  public List<SymbolInformation> getImplementers() {
    return this.implementers;
  }

  public void setImplementers(List<SymbolInformation> implementers) {
    this.implementers = implementers;
  }
}
