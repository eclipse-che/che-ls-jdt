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

/** Describes test position in document */
public class TestPosition {
  private String testName;
  private int testNameStartOffset;
  private int testBodyLength;
  private int testNameLength;

  /** @return the test name(it can be method, suite, or class name) */
  public String getTestName() {
    return testName;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  /** @return the document offset where test name begins */
  public int getTestNameStartOffset() {
    return testNameStartOffset;
  }

  public void setTestNameStartOffset(int testNameStartOffset) {
    this.testNameStartOffset = testNameStartOffset;
  }

  /** @return the test body length */
  public int getTestBodyLength() {
    return testBodyLength;
  }

  public void setTestBodyLength(int testBodyLength) {
    this.testBodyLength = testBodyLength;
  }

  /** @return the test name length */
  public int getTestNameLength() {
    return testNameLength;
  }

  public void setTestNameLength(int testNameLength) {
    this.testNameLength = testNameLength;
  }
}
