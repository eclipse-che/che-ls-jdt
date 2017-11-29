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

/** @author Anatolii Bazko */
public class LocationParameters {
  private String filePath;
  private int lineNumber;
  private String fqn;
  private int libId;

  public LocationParameters() {}

  public LocationParameters(String filePath, int lineNumber) {
    this.filePath = filePath;
    this.lineNumber = lineNumber;
  }

  public LocationParameters(String fqn, int libId, int lineNumber) {
    this.fqn = fqn;
    this.libId = libId;
    this.lineNumber = lineNumber;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public int getLibId() {
    return libId;
  }

  public void setLibId(int libId) {
    this.libId = libId;
  }

  public String getFqn() {
    return fqn;
  }

  public void setFqn(String fqn) {
    this.fqn = fqn;
  }
}
