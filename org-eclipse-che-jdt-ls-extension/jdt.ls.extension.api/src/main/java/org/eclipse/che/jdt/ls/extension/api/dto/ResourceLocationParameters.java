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
 * Resource location parameters.
 *
 * @author Anatolii Bazko
 */
public class ResourceLocationParameters {
  private String fileUri;
  private String fqn;
  private int libId;

  public ResourceLocationParameters() {}

  /**
   * Resource located in the source folder.
   *
   * @param fileUri the resource uri
   */
  public ResourceLocationParameters(String fileUri) {
    this.fileUri = fileUri;
  }

  /**
   * Resource located in the external library.
   *
   * @param fqn the fully qualified name
   * @param libId the library identifier
   */
  public ResourceLocationParameters(String fqn, int libId) {
    this.fqn = fqn;
    this.libId = libId;
  }

  public String getFileUri() {
    return fileUri;
  }

  public void setFileUri(String fileUri) {
    this.fileUri = fileUri;
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
