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
 * Object describes initialization of the move refactoring operation.
 *
 * @author Valeriy Svydenko
 */
public class CreateMoveParams {
  private String projectUri;
  private List<Resource> resources;

  /** Returns URI of the project */
  public String getProjectUri() {
    return projectUri;
  }

  public void setProjectUri(String projectUri) {
    this.projectUri = projectUri;
  }

  /** Returns list of the elements to be moved. */
  public List<Resource> getResources() {
    return resources;
  }

  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }
}
