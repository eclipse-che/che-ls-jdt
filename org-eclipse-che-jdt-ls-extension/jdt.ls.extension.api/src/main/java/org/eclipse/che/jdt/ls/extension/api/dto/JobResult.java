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

import org.eclipse.che.jdt.ls.extension.api.Severity;

/** @author Anatolii Bazko */
public class JobResult {
  private Severity severity;
  private int resultCode;
  private String message;

  public JobResult() {}

  public JobResult(Severity severity, int resultCode, String message) {
    this.resultCode = resultCode;
    this.message = message;
    this.severity = severity;
  }

  /** Returns the result of a finished job. Zero means job has finished successfully. */
  public int getResultCode() {
    return resultCode;
  }

  public void setResultCode(int resultCode) {
    this.resultCode = resultCode;
  }

  /** Returns the message explaining the {@link #resultCode} */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  /** Returns the severity of a finished job. */
  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }
}
