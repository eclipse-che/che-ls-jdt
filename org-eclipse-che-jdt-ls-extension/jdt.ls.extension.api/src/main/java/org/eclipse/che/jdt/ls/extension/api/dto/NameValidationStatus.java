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
import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;

/**
 * Represents the outcome of a new name checking operation.
 *
 * @author Valeriy Svydenko
 */
public class NameValidationStatus {
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
