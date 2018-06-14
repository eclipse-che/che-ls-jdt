/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.api.dto;

import java.util.List;

/**
 * Represents Java project in terms of JavaModel.
 *
 * @author Valeriy Svydenko
 */
public class JavaProjectStructure {
  private String uri;
  private String name;
  private List<PackageFragmentRoot> packageRoots;

  /**
   * Get all package fragment roots from this project.
   *
   * @return list of the package fragment roots
   */
  public List<PackageFragmentRoot> getPackageRoots() {
    return packageRoots;
  }

  public void setPackageRoots(List<PackageFragmentRoot> packageRoots) {
    this.packageRoots = packageRoots;
  }

  /** Returns project's uri. */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  /** Returns project's name. */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
