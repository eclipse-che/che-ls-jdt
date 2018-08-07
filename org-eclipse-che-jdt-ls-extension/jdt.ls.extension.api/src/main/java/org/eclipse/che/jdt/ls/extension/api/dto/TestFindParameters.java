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

import java.util.List;

/** Parameter class for TestDetectionHandler. */
public class TestFindParameters {
  private String sourceUri;
  private String testMethodAnnotation;
  private String testClassAnnotation;
  private List<String> entryClasses;
  private int cursorOffset;

  public TestFindParameters() {}

  public TestFindParameters(
      String sourceUri,
      String testMethodAnnotation,
      String testClassAnnotation,
      int cursorOffset,
      List<String> classes) {
    this.sourceUri = sourceUri;
    this.testMethodAnnotation = testMethodAnnotation;
    this.testClassAnnotation = testClassAnnotation;
    this.cursorOffset = cursorOffset;
    this.entryClasses = classes;
  }

  public String getSourceUri() {
    return sourceUri;
  }

  public void setSourceUri(String sourceUri) {
    this.sourceUri = sourceUri;
  }

  public String getTestMethodAnnotation() {
    return testMethodAnnotation;
  }

  public void setTestMethodAnnotation(String testMethodAnnotation) {
    this.testMethodAnnotation = testMethodAnnotation;
  }

  public String getTestClassAnnotation() {
    return testClassAnnotation;
  }

  public void setTestClassAnnotation(String testClassAnnotation) {
    this.testClassAnnotation = testClassAnnotation;
  }

  public int getCursorOffset() {
    return cursorOffset;
  }

  public void setCursorOffset(int cursorOffset) {
    this.cursorOffset = cursorOffset;
  }

  public List<String> getEntryClasses() {
    return entryClasses;
  }

  public void setEntryClasses(List<String> entryClasses) {
    this.entryClasses = entryClasses;
  }
}
