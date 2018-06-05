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

import java.util.LinkedList;
import java.util.List;
import org.eclipse.lsp4j.WorkspaceEdit;

/**
 * A workspace edit represents resource changes.
 *
 * @author Valeriy Svydenko
 */
public class CheWorkspaceEdit extends WorkspaceEdit {
  /** List of resource changes */
  private List<CheResourceChange> cheResourceChanges;

  public CheWorkspaceEdit() {
    this.cheResourceChanges = new LinkedList<>();
  }

  public List<CheResourceChange> getCheResourceChanges() {
    return this.cheResourceChanges;
  }

  public void setCheResourceChanges(final List<CheResourceChange> cheResourceChanges) {
    this.cheResourceChanges = cheResourceChanges;
  }
}
