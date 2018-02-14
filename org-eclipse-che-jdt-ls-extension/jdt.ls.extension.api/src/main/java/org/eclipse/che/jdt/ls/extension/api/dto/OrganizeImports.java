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

import java.util.ArrayList;
import java.util.List;

/** @author Anatolii Bazko */
public class OrganizeImports {
  private String resourceUri;
  private List<String> choices;

  public OrganizeImports() {
    this.choices = new ArrayList<>();
  }

  public OrganizeImports(String resourceUri) {
    this.resourceUri = resourceUri;
    this.choices = new ArrayList<>();
  }

  public OrganizeImports(String resourceUri, List<String> choices) {
    this.resourceUri = resourceUri;
    this.choices = choices;
  }

  /** The resource {@link java.net.URI} */
  public String getResourceUri() {
    return resourceUri;
  }

  public void setResourceUri(String resourceUri) {
    this.resourceUri = resourceUri;
  }

  /** Returns choices to resolve import conflicts. */
  public List<String> getChoices() {
    return choices;
  }

  public void setChoices(List<String> choices) {
    this.choices = choices;
  }
}
