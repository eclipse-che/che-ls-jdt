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
 * Represents package fragment.
 *
 * @author Valeriy Svydenko
 */
public class PackageFragment {
  private String uri;
  private String projectUri;
  private boolean defaultPackage;
  private String name;

  /** Returns uri of the resource. */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /** Returns whether this package fragment is a default package. This is a handle-only method. */
  public boolean isDefaultPackage() {
    return defaultPackage;
  }

  public void setDefaultPackage(boolean defaultPackage) {
    this.defaultPackage = defaultPackage;
  }

  /** Returns the name of this element. This is a handle-only method. */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** Returns project uri. */
  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }
}
