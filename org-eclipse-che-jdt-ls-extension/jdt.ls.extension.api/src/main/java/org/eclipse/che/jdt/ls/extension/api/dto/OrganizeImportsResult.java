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
import org.eclipse.lsp4j.WorkspaceEdit;

/** @author Anatolii Bazko */
public class OrganizeImportsResult {
  private List<List<String>> ambiguousTypes;
  private WorkspaceEdit workspaceEdit;

  public OrganizeImportsResult() {}

  public OrganizeImportsResult(WorkspaceEdit workspaceEdit) {
    this.workspaceEdit = workspaceEdit;
  }

  public OrganizeImportsResult(WorkspaceEdit workspaceEdit, List<List<String>> ambiguousTypes) {
    this.ambiguousTypes = ambiguousTypes;
    this.workspaceEdit = workspaceEdit;
  }

  /** Returns ambiguousTypes that can't be imported automatically and require user's interaction. */
  public List<List<String>> getAmbiguousTypes() {
    return ambiguousTypes;
  }

  public void setAmbiguousTypes(List<List<String>> ambiguousTypes) {
    this.ambiguousTypes = ambiguousTypes;
  }

  /** Returns workspace changes to apply. */
  public WorkspaceEdit getWorkspaceEdit() {
    return workspaceEdit;
  }

  public void setWorkspaceEdit(WorkspaceEdit workspaceEdit) {
    this.workspaceEdit = workspaceEdit;
  }
}
