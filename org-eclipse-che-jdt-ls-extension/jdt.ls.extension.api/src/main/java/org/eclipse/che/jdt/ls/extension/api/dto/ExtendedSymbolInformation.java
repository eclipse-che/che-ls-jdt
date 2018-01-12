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
 * This class represents a hierarchy of symbols.
 *
 * @author Thomas Mäder
 */
public class ExtendedSymbolInformation {
  private SymbolInformation info;
  private List<ExtendedSymbolInformation> children;

  public ExtendedSymbolInformation() {}

  public ExtendedSymbolInformation(
      SymbolInformation info, List<ExtendedSymbolInformation> children) {
    this.info = info;
    this.children = children;
  }

  public SymbolInformation getInfo() {
    return info;
  }

  public void setInfo(SymbolInformation info) {
    this.info = info;
  }

  public List<ExtendedSymbolInformation> getChildren() {
    return children;
  }

  public void setChildren(List<ExtendedSymbolInformation> children) {
    this.children = children;
  }
}
