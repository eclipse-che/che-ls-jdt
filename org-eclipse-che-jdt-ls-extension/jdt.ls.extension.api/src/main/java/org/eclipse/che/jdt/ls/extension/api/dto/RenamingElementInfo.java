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

import org.eclipse.che.jdt.ls.extension.api.RenameKind;

/**
 * The information about the element which should be renamed.
 *
 * @author Valeriy Svydenko
 */
public class RenamingElementInfo {

  private String elementName;
  private RenameKind renameKind;

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  /** Returns name of the element. */
  public String getElementName() {
    return this.elementName;
  }

  /** Returns type of the renaming. */
  public RenameKind getRenameKind() {
    return renameKind;
  }

  public void setRenameKind(RenameKind renameKind) {
    this.renameKind = renameKind;
  }
}
