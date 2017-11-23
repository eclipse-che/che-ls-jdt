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
package org.eclipse.che.jdt.ls.extension.api.dto.debug;

/** @author Anatolii Bazko */
public class LocationParameters {
  private String projectPath;
  private String target;
  private int lineNumber;
  private int libId;

  public LocationParameters() {}

  public LocationParameters(String target, int lineNumber, String projectPath) {
    this.target = target;
    this.lineNumber = lineNumber;
    this.projectPath = projectPath;
  }

  public LocationParameters(String target, int lineNumber, int libId, String projectPath) {
    this.target = target;
    this.lineNumber = lineNumber;
    this.projectPath = projectPath;
    this.libId = libId;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  private int getLibId() {
    return libId;
  }

  private void setLibId(int libId) {
    this.libId = libId;
  }
}
