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
package org.eclipse.che.jdt.ls.extension.api;

public enum Visibility {
  PRIVATE(1),
  PACKAGE(2),
  PROTECTED(3),
  PUBLIC(4);

  @SuppressWarnings("unused")
  private final int value;

  Visibility(int value) {
    this.value = value;
  }

  /** Converts an integer representation of the severity into enum. */
  public static Visibility valueOf(int value) {
    switch (value) {
      case 1:
        return PRIVATE;
      case 2:
        return Visibility.PACKAGE;
      case 4:
        return PROTECTED;
      case 8:
        return PUBLIC;
      default:
        throw new IllegalArgumentException("Unknown Visibility:" + value);
    }
  }
}
