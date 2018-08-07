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

/**
 * Parameter class for FileStructureCommand
 *
 * @author Thomas Mäder
 */
public class FileStructureCommandParameters {
  private String uri;
  private boolean showInherited;

  public FileStructureCommandParameters() {}

  public FileStructureCommandParameters(String uri, boolean showInherited) {
    this.uri = uri;
    this.showInherited = showInherited;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public boolean getShowInherited() {
    return showInherited;
  }

  public void setShowInherited(boolean showInherited) {
    this.showInherited = showInherited;
  }
}
