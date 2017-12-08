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
}
