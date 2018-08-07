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

import java.util.Collections;
import java.util.List;

/**
 * Contains list of removed/added projects to update eclipse workspace.
 *
 * @author Anatolii Bazko
 */
public class UpdateWorkspaceParameters {
  private List<String> addedProjectsUri;
  private List<String> removedProjectsUri;

  public UpdateWorkspaceParameters() {
    this.addedProjectsUri = Collections.emptyList();
    this.removedProjectsUri = Collections.emptyList();
  }

  public UpdateWorkspaceParameters(List<String> addedProjectsUri, List<String> removedProjectsUri) {
    this.addedProjectsUri = addedProjectsUri;
    this.removedProjectsUri = removedProjectsUri;
  }

  public List<String> getAddedProjectsUri() {
    return addedProjectsUri;
  }

  public void setAddedProjectsUri(List<String> addedProjectsUri) {
    this.addedProjectsUri = addedProjectsUri;
  }

  public List<String> getRemovedProjectsUri() {
    return removedProjectsUri;
  }

  public void setRemovedProjectsUri(List<String> removedProjectsUri) {
    this.removedProjectsUri = removedProjectsUri;
  }
}
