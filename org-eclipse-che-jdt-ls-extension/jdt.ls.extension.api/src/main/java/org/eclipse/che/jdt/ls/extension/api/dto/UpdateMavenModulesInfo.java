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

import java.util.LinkedList;
import java.util.List;

/**
 * Represents information about updating maven modules.
 *
 * @author Valeriy Svydenko
 */
public class UpdateMavenModulesInfo {
  /** List of added modules. */
  private List<String> added;
  /** List of removed modules. */
  private List<String> removed;
  /** The URI of the parent project which has changes on <modules></modules>. */
  private String projectUri;

  public UpdateMavenModulesInfo() {
    this.added = new LinkedList<>();
    this.removed = new LinkedList<>();
  }

  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }

  public List<String> getAdded() {
    return added;
  }

  public void setAdded(List<String> added) {
    this.added = added;
  }

  public List<String> getRemoved() {
    return removed;
  }

  public void setRemoved(List<String> removed) {
    this.removed = removed;
  }
}
