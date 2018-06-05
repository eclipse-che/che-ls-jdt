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
 * Represents kind of the java element to be refactored.
 *
 * @author Valeriy Svydenko
 */
public enum ResourceKind {
  FILE(0),
  FOLDER(1);

  final int value;

  ResourceKind(int i) {
    this.value = i;
  }

  /** Converts an integer representation of the kind into enum. */
  public static ResourceKind valueOf(int value) {
    switch (value) {
      case 0:
        return FILE;
      default:
        return FOLDER;
    }
  }
}
