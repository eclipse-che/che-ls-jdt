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

/**
 * Represents kind of the java element to be refactored.
 *
 * @author Valeriy Svydenko
 */
public enum RenameKind {
  PACKAGE(0),
  COMPILATION_UNIT(1),
  TYPE(2),
  FIELD(3),
  ENUM_CONSTANT(4),
  TYPE_PARAMETER(5),
  METHOD(6),
  LOCAL_VARIABLE(7),
  JAVA_ELEMENT(8),
  UNKNOWN(-1);

  final int value;

  RenameKind(int i) {
    this.value = i;
  }

  /** Converts an integer representation of the kind into enum. */
  public static RenameKind valueOf(int value) {
    switch (value) {
      case 0:
        return PACKAGE;
      case 1:
        return COMPILATION_UNIT;
      case 2:
        return TYPE;
      case 3:
        return FIELD;
      case 4:
        return ENUM_CONSTANT;
      case 5:
        return TYPE_PARAMETER;
      case 6:
        return METHOD;
      case 7:
        return LOCAL_VARIABLE;
      case 8:
        return JAVA_ELEMENT;
      default:
        return UNKNOWN;
    }
  }
}
