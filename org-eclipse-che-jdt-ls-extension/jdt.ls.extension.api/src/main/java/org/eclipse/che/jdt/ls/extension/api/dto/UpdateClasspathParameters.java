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
 * Represents information about project classpath.
 *
 * @author Valeriy Svydenko
 */
public class UpdateClasspathParameters {
  private String uri;
  private List<ClasspathEntry> entries;

  public UpdateClasspathParameters() {}

  /** Returns project URI. */
  public String getProjectUri() {
    return uri;
  }

  public void setProjectUri(String uri) {
    this.uri = uri;
  }

  /** Returns classpath entries. */
  public List<ClasspathEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<ClasspathEntry> entries) {
    this.entries = entries;
  }
}
