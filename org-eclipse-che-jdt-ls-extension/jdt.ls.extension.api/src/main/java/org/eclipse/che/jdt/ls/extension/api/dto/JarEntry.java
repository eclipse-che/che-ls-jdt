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
 * This class represents an entry of jar of external library.
 *
 * @author Valeriy Svydenko
 */
public class JarEntry {
  private String name;
  private String path;
  private String entryType;
  private String uri;

  public JarEntry() {}

  public JarEntry(String name, String path, String entryType, String uri) {
    this.name = name;
    this.path = path;
    this.entryType = entryType;
    this.uri = uri;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getEntryType() {
    return entryType;
  }

  public void setEntryType(String entryType) {
    this.entryType = entryType;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
