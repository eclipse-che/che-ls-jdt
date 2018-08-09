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
 * Represents information about updating maven project.
 *
 * @author Valeriy Svydenko
 */
public class MavenProjectUpdateInfo {
  private boolean created;
  private String projectUri;

  public boolean isCreated() {
    return created;
  }

  public void setCreated(boolean created) {
    this.created = created;
  }

  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }
}
