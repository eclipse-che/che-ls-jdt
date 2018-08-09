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
  private List<String> created;
  private List<String> removed;

  public UpdateMavenModulesInfo() {
    this.created = new LinkedList<>();
    this.removed = new LinkedList<>();
  }

  public List<String> getCreated() {
    return created;
  }

  public void setCreated(List<String> created) {
    this.created = created;
  }

  public List<String> getRemoved() {
    return removed;
  }

  public void setRemoved(List<String> removed) {
    this.removed = removed;
  }
}
