/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
