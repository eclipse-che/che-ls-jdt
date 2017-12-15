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
  private List<String> addedProjects;
  private List<String> removedProjects;

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

  public List<String> getAddedProjects() {
    return addedProjects;
  }

  public void setAddedProjects(List<String> addedProjects) {
    this.addedProjects = addedProjects;
  }

  public ReImportMavenProjectsResult withAddedProjects(List<String> addedProjects) {
    this.addedProjects = addedProjects;
    return this;
  }

  public List<String> getRemovedProjects() {
    return removedProjects;
  }

  public void setRemovedProjects(List<String> removedProjects) {
    this.removedProjects = removedProjects;
  }

  public ReImportMavenProjectsResult withRemovedProjects(List<String> removedProjects) {
    this.removedProjects = removedProjects;
    return this;
  }
}
