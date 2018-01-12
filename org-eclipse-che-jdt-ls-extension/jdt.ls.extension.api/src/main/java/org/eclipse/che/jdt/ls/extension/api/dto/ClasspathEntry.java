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

import java.util.List;

/**
 * Represents classpath model of the project.
 *
 * @author Valeriy Svydenko
 */
public class ClasspathEntry {
  private int entryKind;
  private String path;
  private List<ClasspathEntry> children;

  public ClasspathEntry() {}

  /** Returns type of the entry. */
  public int getEntryKind() {
    return entryKind;
  }

  public void setEntryKind(int kind) {
    this.entryKind = kind;
  }

  public ClasspathEntry withEntryKind(int kind) {
    this.entryKind = kind;
    return this;
  }

  /** Returns path to the entry. */
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ClasspathEntry withPath(String path) {
    this.path = path;
    return this;
  }

  /** Returns sub entries. */
  public List<ClasspathEntry> getChildren() {
    return children;
  }

  public void setChildren(List<ClasspathEntry> children) {
    this.children = children;
  }

  public ClasspathEntry withChildren(List<ClasspathEntry> children) {
    this.children = children;
    return this;
  }
}
