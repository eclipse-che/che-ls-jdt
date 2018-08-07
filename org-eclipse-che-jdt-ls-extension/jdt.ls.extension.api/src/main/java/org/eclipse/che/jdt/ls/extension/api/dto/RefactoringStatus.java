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
import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;

/**
 * Represents the outcome of a new name checking operation.
 *
 * @author Valeriy Svydenko
 */
public class RefactoringStatus {
  private List<RefactoringStatusEntry> refactoringStatusEntries;
  private RefactoringSeverity refactoringSeverity;

  public RefactoringSeverity getRefactoringSeverity() {
    return refactoringSeverity;
  }

  public void setRefactoringSeverity(RefactoringSeverity refactoringSeverity) {
    this.refactoringSeverity = refactoringSeverity;
  }

  public List<RefactoringStatusEntry> getRefactoringStatusEntries() {
    return refactoringStatusEntries;
  }

  public void setRefactoringStatusEntries(List<RefactoringStatusEntry> refactoringStatusEntries) {
    this.refactoringStatusEntries = refactoringStatusEntries;
  }
}
