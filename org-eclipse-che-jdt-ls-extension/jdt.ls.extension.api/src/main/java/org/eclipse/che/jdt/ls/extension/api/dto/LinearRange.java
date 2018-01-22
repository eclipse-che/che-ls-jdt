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

public class LinearRange {
  private int offset;
  private int length;

  public LinearRange() {}

  public LinearRange(int offset, int length) {
    this.offset = offset;
    this.length = length;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LinearRange that = (LinearRange) obj;
    return this.offset == that.offset && this.length == that.length;
  }

  @Override
  public int hashCode() {
    return this.offset & Integer.MAX_VALUE - this.length;
  }

  @Override
  public String toString() {
    return "LinearRange[" + offset + ", " + length + "]";
  }
}
