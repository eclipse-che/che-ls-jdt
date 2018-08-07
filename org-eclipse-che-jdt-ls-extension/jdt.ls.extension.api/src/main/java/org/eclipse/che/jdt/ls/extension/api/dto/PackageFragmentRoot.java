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

/**
 * Represents package fragment root.
 *
 * @author Valeriy Svydenko
 */
public class PackageFragmentRoot {
  private String uri;
  private String projectUri;
  private List<PackageFragment> packages;

  /**
   * All package fragments in this package fragment root.
   *
   * @return list of package fragments
   */
  public List<PackageFragment> getPackages() {
    return packages;
  }

  public void setPackages(List<PackageFragment> packages) {
    this.packages = packages;
  }

  /** Returns uri of the resource. */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /** Returns uri of the current project. */
  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }
}
