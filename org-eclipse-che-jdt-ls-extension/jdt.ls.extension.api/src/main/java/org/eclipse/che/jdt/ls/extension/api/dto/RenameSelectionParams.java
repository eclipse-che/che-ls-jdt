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
import org.eclipse.lsp4j.Position;

/**
 * The rename request is sent from the client to the server to recognize which element was selected.
 */
public class RenameSelectionParams {
  private String newName;
  private Position position;
  private String resourceUri;
  private RenameKind renameKind;

  /** New name of the element */
  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  /** The position at which this request was send. */
  public Position getPosition() {
    return this.position;
  }

  /** The position at which this request was send. */
  public void setPosition(final Position position) {
    this.position = position;
  }

  public String getResourceUri() {
    return this.resourceUri;
  }

  public void setResourceUri(String resourceUri) {
    this.resourceUri = resourceUri;
  }

  /** Type of the selected element */
  public RenameKind getRenameKind() {
    return renameKind;
  }

  public void setRenameKind(RenameKind renameKind) {
    this.renameKind = renameKind;
  }
}
