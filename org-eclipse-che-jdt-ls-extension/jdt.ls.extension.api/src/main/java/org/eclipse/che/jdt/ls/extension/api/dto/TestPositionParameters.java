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

/** Parameter class for TestDetectionHandler. */
public class TestPositionParameters {
  private String fileUri;
  private String testAnnotation;
  private int cursorOffset;

  public TestPositionParameters() {}

  public TestPositionParameters(String fileUri, String testAnnotation, int cursorOffset) {
    this.fileUri = fileUri;
    this.testAnnotation = testAnnotation;
    this.cursorOffset = cursorOffset;
  }

  public String getFileUri() {
    return fileUri;
  }

  public void setFileUri(String fileUri) {
    this.fileUri = fileUri;
  }

  public String getTestAnnotation() {
    return testAnnotation;
  }

  public void setTestAnnotation(String testAnnotation) {
    this.testAnnotation = testAnnotation;
  }

  public int getCursorOffset() {
    return cursorOffset;
  }

  public void setCursorOffset(int cursorOffset) {
    this.cursorOffset = cursorOffset;
  }
}
