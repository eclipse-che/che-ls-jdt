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

/**
 * Describes strategy for matching type names (e.g. SomeClass) in variable and method names.
 *
 * @author Valeriy Svydenko
 */
public enum MatchStrategy {
  // Find exact names only ("someClass" or "someClass()")
  EXACT(1),
  // Also find embedded names ("mySomeClassToUse" or "getSomeClass()")
  EMBEDDED(2),
  // Alos find name suffixes ("class", "myClass" or "getClassToUse()")
  SUFFIX(3);

  final int value;

  MatchStrategy(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /** Converts an integer representation of the strategy for matching into enum. */
  public static MatchStrategy valueOf(int value) {
    switch (value) {
      case 1:
        return EXACT;
      case 2:
        return EMBEDDED;
      default:
        return SUFFIX;
    }
  }
}
