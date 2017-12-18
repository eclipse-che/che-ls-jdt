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

import java.util.Collections;
import java.util.List;

/**
 * Contains list of removed/added projects ot update eclipse workspace.
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
