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

/**
 * Parameters for ExternalLibrary commands.
 *
 * @author Valeriy Svydenko
 */
public class ExternalLibrariesParameters {
  private String projectUri;
  private String nodePath;
  private String nodeId;

  public ExternalLibrariesParameters() {}

  public ExternalLibrariesParameters(String projectUri) {
    this.projectUri = projectUri;
  }

  public ExternalLibrariesParameters(String projectUri, String nodeId) {
    this.projectUri = projectUri;
    this.nodeId = nodeId;
  }

  public ExternalLibrariesParameters(String projectUri, String nodePath, String nodeId) {
    this.projectUri = projectUri;
    this.nodePath = nodePath;
    this.nodeId = nodeId;
  }

  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }

  public String getNodePath() {
    return nodePath;
  }

  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }
}
