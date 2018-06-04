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

/** @author Valeriy Svydenko */
public enum RefactoringSeverity {
  OK(0),
  INFO(1),
  WARNING(2),
  ERROR(3),
  FATAL(4);

  final int value;

  RefactoringSeverity(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /** Converts an integer representation of the severity into enum. */
  public static RefactoringSeverity valueOf(int value) {
    switch (value) {
      case 0:
        return OK;
      case 1:
        return INFO;
      case 2:
        return WARNING;
      case 3:
        return ERROR;
      default:
        return FATAL;
    }
  }
}
