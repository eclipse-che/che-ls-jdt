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

import java.util.List;

public class ReImportMavenProjectsResult {
  private List<String> updatedProjects;

  public List<String> getUpdatedProjects() {
    return updatedProjects;
  }

  public void setUpdatedProjects(List<String> updatedProjects) {
    this.updatedProjects = updatedProjects;
  }

  public ReImportMavenProjectsResult withUpdatedProjects(List<String> updatedProjects) {
    this.updatedProjects = updatedProjects;
    return this;
  }
}
