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
package org.eclipse.che.jdt.ls.extension.core.internal.imports;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.ls.core.internal.corext.codemanipulation.OrganizeImportsOperation;

/**
 * Resolves conflicts of importing ambiguous types.
 *
 * @author Anatolii Bazko
 */
public class ConflictResolver implements OrganizeImportsOperation.IChooseImportQuery {
  private List<List<String>> remainingConflicts;
  private final List<String> choices;

  public ConflictResolver(List<String> choices) {
    this.choices = choices;
    this.remainingConflicts = new ArrayList<>();
  }

  @Override
  public TypeNameMatch[] chooseImports(
      TypeNameMatch[][] typeNameMatches, ISourceRange[] iSourceRanges) {

    List<TypeNameMatch> resolvedConflicts = new LinkedList<>();

    outer:
    for (int i = 0; i < typeNameMatches.length; i++) {
      for (int j = 0; j < typeNameMatches[i].length; j++) {
        TypeNameMatch typeNameMatch = typeNameMatches[i][j];

        if (choices.contains(typeNameMatch.getFullyQualifiedName())) {
          resolvedConflicts.add(typeNameMatch);
          continue outer;
        }
      }

      List<String> matches =
          Stream.of(typeNameMatches[i])
              .map(TypeNameMatch::getFullyQualifiedName)
              .collect(Collectors.toList());
      remainingConflicts.add(matches);
    }

    return resolvedConflicts.toArray(new TypeNameMatch[resolvedConflicts.size()]);
  }

  public List<List<String>> getRemainingConflicts() {
    return remainingConflicts;
  }
}
