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

/**
 * Represents a resource.
 *
 * @author Valeriy Svydenko
 */
public class Resource {
  private String uri;
  private boolean pack;

  /** @return true if this resource is package and false if it's compilation unit */
  public boolean isPack() {
    return pack;
  }

  public void setPack(boolean pack) {
    this.pack = pack;
  }

  /** @return uri of the resource */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
