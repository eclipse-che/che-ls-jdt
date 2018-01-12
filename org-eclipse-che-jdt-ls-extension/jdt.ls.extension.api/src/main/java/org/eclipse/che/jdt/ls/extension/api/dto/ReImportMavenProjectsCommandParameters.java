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
