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
