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

import org.eclipse.che.jdt.ls.extension.api.ResourceKind;
import org.eclipse.lsp4j.ResourceChange;

/**
 * A resource change describes kind of changed resource.
 *
 * @author Valeriy Svydenko
 */
public class CheResourceChange extends ResourceChange {
  /** Kind of changed resource. */
  private ResourceKind resourceKind;

  /** Human readable description about change.. */
  private String description;

  /** Returns kind of changed resource. */
  public ResourceKind getResourceKind() {
    return resourceKind;
  }

  public void setResourceKind(ResourceKind resourceKind) {
    this.resourceKind = resourceKind;
  }

  /** Returns human readable description of the change. */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
