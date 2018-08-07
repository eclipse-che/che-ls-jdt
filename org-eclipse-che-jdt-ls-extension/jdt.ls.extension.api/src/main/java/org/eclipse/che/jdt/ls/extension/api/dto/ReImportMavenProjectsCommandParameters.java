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
 * Parameter class for ReImportMavenProjectsCommand
 *
 * @author Mykola Morhun
 */
public class ReImportMavenProjectsCommandParameters {
  private List<String> projectsToUpdate;

  /** Returns URIs to projects which should be updated. */
  public List<String> getProjectsToUpdate() {
    return projectsToUpdate;
  }

  public void setProjectsToUpdate(List<String> projectsToUpdate) {
    this.projectsToUpdate = projectsToUpdate;
  }

  public ReImportMavenProjectsCommandParameters withProjectsToUpdate(
      List<String> projectsToUpdate) {
    this.projectsToUpdate = projectsToUpdate;
    return this;
  }
}
