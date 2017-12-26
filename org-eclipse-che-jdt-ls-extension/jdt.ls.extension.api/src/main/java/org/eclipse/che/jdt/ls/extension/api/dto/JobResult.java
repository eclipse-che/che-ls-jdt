/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
