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
 * Resource location parameters.
 *
 * @author Anatolii Bazko
 */
public class ResourceLocation {
  private String fqn;
  private String libId;

  public ResourceLocation() {}

  /**
   * Resource located in the external library.
   *
   * @param fqn the fully qualified name
   * @param libId the library identifier
   */
  public ResourceLocation(String fqn, String libId) {
    this.fqn = fqn;
    this.libId = libId;
  }

  public String getLibId() {
    return libId;
  }

  public void setLibId(String libId) {
    this.libId = libId;
  }

  public String getFqn() {
    return fqn;
  }

  public void setFqn(String fqn) {
    this.fqn = fqn;
  }
}
