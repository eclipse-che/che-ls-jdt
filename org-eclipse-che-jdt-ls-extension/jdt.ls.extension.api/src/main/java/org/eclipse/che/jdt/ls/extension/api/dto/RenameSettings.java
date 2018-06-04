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

import org.eclipse.che.jdt.ls.extension.api.MatchStrategy;
import org.eclipse.che.jdt.ls.extension.api.RenameKind;
import org.eclipse.lsp4j.RenameParams;

/**
 * Settings for all Rename refactorings.
 *
 * @author Valeriy Svydenko
 */
public class RenameSettings {
  private RenameKind renameKind;
  private RenameParams renameParams;
  private boolean delegateUpdating;
  private boolean deprecateDelegates;
  private boolean updateQualifiedNames;
  private boolean updateSubpackages;
  private boolean updateReferences;
  private boolean updateSimilarDeclarations;
  private boolean updateTextualMatches;
  private MatchStrategy matchStrategy;
  private String filePatterns;

  /** Information about changed element {@link RenameParams} */
  public void setRenameParams(RenameParams renameParams) {
    this.renameParams = renameParams;
  }

  public RenameParams getRenameParams() {
    return renameParams;
  }

  /** Returns type of renaming. */
  public RenameKind getRenameKind() {
    return renameKind;
  }

  public void setRenameKind(RenameKind renameKind) {
    this.renameKind = renameKind;
  }

  // IDelegateUpdating
  /**
   * If refactoring object is capable of creating appropriate delegates for the refactored elements.
   * This value used to set whether to create delegates.
   */
  public boolean isDelegateUpdating() {
    return delegateUpdating;
  }

  public void setDelegateUpdating(boolean delegateUpdating) {
    this.delegateUpdating = delegateUpdating;
  }

  /**
   * If refactoring object is capable of creating appropriate delegates for the refactored elements.
   * This value used to set whether to deprecate delegates.
   */
  public boolean isDeprecateDelegates() {
    return deprecateDelegates;
  }

  public void setDeprecateDelegates(boolean deprecateDelegates) {
    this.deprecateDelegates = deprecateDelegates;
  }

  // IQualifiedNameUpdating

  /**
   * If this refactoring object is capable of updating qualified names in non Java files. then this
   * value is used to inform the refactoring object whether references in non Java files should be
   * updated.
   */
  public boolean isUpdateQualifiedNames() {
    return updateQualifiedNames;
  }

  public void setUpdateQualifiedNames(boolean updateQualifiedNames) {
    this.updateQualifiedNames = updateQualifiedNames;
  }

  public String getFilePatterns() {
    return filePatterns;
  }

  public void setFilePatterns(String filePatterns) {
    this.filePatterns = filePatterns;
  }

  // ISubpackagesUpdating

  /**
   * Informs the refactoring object whether subpackages should be updated. This value used to set
   * whether to updating packages.
   *
   * @return <code>true</code> if subpackages updating is enabled
   */
  public boolean isUpdateSubpackages() {
    return updateSubpackages;
  }

  public void setUpdateSubpackages(boolean updateSubpackages) {
    this.updateSubpackages = updateSubpackages;
  }

  // IReferenceUpdating

  /**
   * Informs the refactoring object whether references should be updated. * @return <code>true
   * </code> iff reference updating is enabled
   */
  public boolean isUpdateReferences() {
    return updateReferences;
  }

  public void setUpdateReferences(boolean updateReferences) {
    this.updateReferences = updateReferences;
  }

  // ISimilarDeclarationUpdating

  /**
   * If this refactoring object is capable of updating similar declarations of the renamed element,
   * then this value is used to inform the refactoring object whether similar declarations should be
   * updated.
   *
   * @return
   */
  public boolean isUpdateSimilarDeclarations() {
    return updateSimilarDeclarations;
  }

  public void setUpdateSimilarDeclarations(boolean updateSimilarDeclarations) {
    this.updateSimilarDeclarations = updateSimilarDeclarations;
  }

  /** method is used to set the match strategy for determining similarly named elements. */
  public MatchStrategy getMatchStrategy() {
    return matchStrategy;
  }

  /**
   * @param matchStrategy must be one of {@link org.eclipse.che.jdt.ls.extension.api.MatchStrategy}
   *     values.
   */
  public void setMatchStrategy(MatchStrategy matchStrategy) {
    this.matchStrategy = matchStrategy;
  }

  // ITextUpdating

  /**
   * This method is used to inform the refactoring object whether references in regular (non
   * JavaDoc) comments and string literals should be updated.
   */
  public boolean isUpdateTextualMatches() {
    return updateTextualMatches;
  }

  public void setUpdateTextualMatches(boolean updateTextualMatches) {
    this.updateTextualMatches = updateTextualMatches;
  }
}
