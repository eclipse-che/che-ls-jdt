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
package org.eclipse.che.jdt.ls.extension.api.dto.navigation;

import java.util.List;
import org.eclipse.lsp4j.SymbolInformation;

public class ImplementationsDescriptor {

  private String memberName;
  private List<SymbolInformation> implementations;

  public ImplementationsDescriptor() {}

  public String getMemberName() {
    return this.memberName;
  }

  public void setMemberName(String memberName) {
    this.memberName = memberName;
  }

  /* public ImplementationsDescriptor withImplementations(List<SymbolInformation> implementations) {
    this.implementations = implementations;
    return this;
  }*/

  /** Returns all implementations. */
  public List<SymbolInformation> getImplementations() {
    return this.implementations;
  }

  public void setImplementations(List<SymbolInformation> implementations) {
    this.implementations = implementations;
  }
}
