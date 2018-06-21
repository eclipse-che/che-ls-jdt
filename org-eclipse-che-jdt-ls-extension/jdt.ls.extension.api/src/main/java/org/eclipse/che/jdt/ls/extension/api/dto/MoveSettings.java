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
 * Describes settings of Move refactoring operation.
 *
 * @author Valeriy Svydenko
 */
public class MoveSettings {
  private String destination;
  private List<Resource> elements;
  private boolean updateReferences;
  private boolean updateQualifiedNames;
  private String filePatterns;

  /** Returns URI of resource which was selected to be destination. */
  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  /** Returns elements that will be moved. */
  public List<Resource> getElements() {
    return elements;
  }

  public void setElements(List<Resource> elements) {
    this.elements = elements;
  }

  /** @return true if refactoring should update references in classes, false otherwise */
  public boolean isUpdateReferences() {
    return updateReferences;
  }

  public void setUpdateReferences(boolean updateReferences) {
    this.updateReferences = updateReferences;
  }

  /** Used to ask the refactoring object whether references in non Java files should be updated. */
  public boolean isUpdateQualifiedNames() {
    return updateQualifiedNames;
  }

  public void setUpdateQualifiedNames(boolean updateQualifiedNames) {
    this.updateQualifiedNames = updateQualifiedNames;
  }

  /**
   * if {@link #isUpdateQualifiedNames()} return true refactoring will use this file pattern to
   * search qualified names
   *
   * @return the file pattern
   */
  public String getFilePatterns() {
    return filePatterns;
  }

  public void setFilePatterns(String filePatterns) {
    this.filePatterns = filePatterns;
  }
}
