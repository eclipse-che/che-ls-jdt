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
import org.eclipse.che.jdt.ls.extension.api.Visibility;
import org.eclipse.lsp4j.SymbolInformation;

/**
 * This class represents a hierarchy of symbols.
 *
 * @author Thomas Mäder
 */
public class ExtendedSymbolInformation {
  private SymbolInformation info;
  private List<ExtendedSymbolInformation> children;
  private Visibility visiblity;

  public ExtendedSymbolInformation() {}

  public ExtendedSymbolInformation(
      SymbolInformation info, Visibility visibility, List<ExtendedSymbolInformation> children) {
    this.info = info;
    this.visiblity = visibility;
    this.children = children;
  }

  public SymbolInformation getInfo() {
    return info;
  }

  public void setInfo(SymbolInformation info) {
    this.info = info;
  }

  public Visibility getVisiblity() {
    return visiblity;
  }

  public void setVisiblity(Visibility visiblity) {
    this.visiblity = visiblity;
  }

  public List<ExtendedSymbolInformation> getChildren() {
    return children;
  }

  public void setChildren(List<ExtendedSymbolInformation> children) {
    this.children = children;
  }
}
