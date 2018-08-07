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

import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;

/**
 * An immutable object representing an entry in the list in <code>RefactoringStatus</code>. A
 * refactoring status entry consists of a severity, a message, a problem code (represented by a
 * tuple(plug-in identifier and code number)).
 *
 * @author Valeriy Svydenko
 */
public class RefactoringStatusEntry {
  private String message;
  private RefactoringSeverity refactoringSeverity;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public RefactoringSeverity getRefactoringSeverity() {
    return refactoringSeverity;
  }

  public void setRefactoringSeverity(RefactoringSeverity refactoringSeverity) {
    this.refactoringSeverity = refactoringSeverity;
  }
}
