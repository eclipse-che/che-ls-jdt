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
