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

import java.util.ArrayList;
import java.util.List;

/** @author Anatolii Bazko */
public class OrganizeImportParams {
  /**
   * The resource uri to organize import upon.<br>
   * Might point to a single file as well as to a directory.
   */
  private String resourceUri;

  /**
   * List of chose FQNs in importing ambiguous types.<br>
   * If there are ambiguous types to import then this list might be used to resolve import conflict.
   */
  private List<String> choices;

  public OrganizeImportParams() {
    this.choices = new ArrayList<>();
  }

  public OrganizeImportParams(String resourceUri) {
    this.resourceUri = resourceUri;
    this.choices = new ArrayList<>();
  }

  public OrganizeImportParams(String resourceUri, List<String> choices) {
    this.resourceUri = resourceUri;
    this.choices = choices;
  }

  public String getResourceUri() {
    return resourceUri;
  }

  public void setResourceUri(String resourceUri) {
    this.resourceUri = resourceUri;
  }

  public List<String> getChoices() {
    return choices;
  }

  public void setChoices(List<String> choices) {
    this.choices = choices;
  }
}
