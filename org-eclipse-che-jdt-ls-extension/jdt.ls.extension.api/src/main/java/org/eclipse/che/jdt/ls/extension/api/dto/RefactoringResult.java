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

/**
 * Describes the information about preparing changes for refactoring operation.
 *
 * @author Valeriy Svydenko
 */
public class RefactoringResult {
  private RefactoringStatus refactoringStatus;
  private CheWorkspaceEdit cheWorkspaceEdit;

  /** Returns the status of the refactoring operation see {@link RefactoringStatus} */
  public RefactoringStatus getRefactoringStatus() {
    return refactoringStatus;
  }

  public void setRefactoringStatus(RefactoringStatus refactoringStatus) {
    this.refactoringStatus = refactoringStatus;
  }

  /** Returns instance of {@link CheWorkspaceEdit} with all changes. */
  public CheWorkspaceEdit getCheWorkspaceEdit() {
    return cheWorkspaceEdit;
  }

  public void setCheWorkspaceEdit(CheWorkspaceEdit cheWorkspaceEdit) {
    this.cheWorkspaceEdit = cheWorkspaceEdit;
  }
}
