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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.WorkspaceEdit;

/** @author Anatolii Bazko */
public class OrganizeImportsResult {
  private List<ImportConflicts> importConflicts;
  private WorkspaceEdit workspaceEdit;

  public OrganizeImportsResult() {}

  public OrganizeImportsResult(WorkspaceEdit workspaceEdit) {
    this.workspaceEdit = workspaceEdit;
    this.importConflicts = new ArrayList<>();
  }

  public OrganizeImportsResult(WorkspaceEdit workspaceEdit, List<ImportConflicts> importConflicts) {
    this.importConflicts = importConflicts;
    this.workspaceEdit = workspaceEdit;
  }

  /** Returns conflicted imports that can't be apply automatically but require user interaction. */
  public List<ImportConflicts> getImportConflicts() {
    return importConflicts;
  }

  public void setImportConflicts(List<ImportConflicts> importConflicts) {
    this.importConflicts = importConflicts;
  }

  /** Returns workspace changes to apply. */
  public WorkspaceEdit getWorkspaceEdit() {
    return workspaceEdit;
  }

  public void setWorkspaceEdit(WorkspaceEdit workspaceEdit) {
    this.workspaceEdit = workspaceEdit;
  }
}
