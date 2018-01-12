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
package org.eclipse.che.jdt.ls.extension.api;

/** @author Anatolii Bazko */
public enum Severity {
  OK(0),
  INFO(1),
  WARNING(2),
  ERROR(4),
  CANCEL(8),
  UNKNOWN(-1);

  final int value;

  Severity(int value) {
    this.value = value;
  }

  /** Converts an integer representation of the severity into enum. */
  public static Severity valueOf(int value) {
    switch (value) {
      case 0:
        return OK;
      case 1:
        return INFO;
      case 2:
        return WARNING;
      case 4:
        return ERROR;
      case 8:
        return CANCEL;
      default:
        return UNKNOWN;
    }
  }
}
