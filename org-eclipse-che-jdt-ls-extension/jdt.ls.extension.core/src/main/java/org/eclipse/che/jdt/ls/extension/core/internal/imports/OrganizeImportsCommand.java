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

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.jdt.ls.extension.api.dto.ImportConflicts;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImports;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.ls.core.internal.JDTDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.TextEditConverter;
import org.eclipse.jdt.ls.core.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.ls.core.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.ls.core.internal.corrections.InnovationContext;
import org.eclipse.jdt.ls.core.internal.corrections.proposals.CUCorrectionProposal;
import org.eclipse.jdt.ls.core.internal.corrections.proposals.IProposalRelevance;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.TextEdit;

/** @author Anatolii Bazko */
public class OrganizeImportsCommand {

  /**
   * Organizes imports either in a folder or in a specific file.
   *
   * @see org.eclipse.jdt.ls.core.internal.commands.OrganizeImportsCommand
   */
  public static OrganizeImportsResult execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);
    ensureNotCancelled(pm);

    final OrganizeImports organizeImports =
        JavaModelUtil.convertCommandParameter(arguments.get(0), OrganizeImports.class);

    File file = new File(JDTUtils.toURI(organizeImports.getResourceUri()));
    if (!file.exists()) {
      return new OrganizeImportsResult();
    }

    if (file.isDirectory()) {
      return doOrganizeImportsInDirectory(arguments, pm);
    } else {
      ICompilationUnit cu = JDTUtils.resolveCompilationUnit(organizeImports.getResourceUri());
      if (cu != null) {
        ConflictResolverChooseImportQuery query =
            new ConflictResolverChooseImportQuery(organizeImports.getChoices());

        return doOrganizeImportsInCompilationUnit(cu, query);
      }
    }

    return new OrganizeImportsResult();
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), OrganizeImports.class.getName() + " is expected.");
  }

  /** Organizes imports in all files of the given directory. Conflicts will be ignored. */
  private static OrganizeImportsResult doOrganizeImportsInDirectory(
      List<Object> arguments, IProgressMonitor pm) {

    JDTDelegateCommandHandler jdtDelegateCommandHandler = new JDTDelegateCommandHandler();
    try {
      WorkspaceEdit workspaceEdit =
          (WorkspaceEdit)
              jdtDelegateCommandHandler.executeCommand("java.edit.organizeImports", arguments, pm);

      return new OrganizeImportsResult(workspaceEdit);
    } catch (Exception e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }

    return new OrganizeImportsResult();
  }

  /** Organize imports in the specific unit. All exposed conflicts require user's interaction. */
  private static OrganizeImportsResult doOrganizeImportsInCompilationUnit(
      ICompilationUnit unit, ConflictResolverChooseImportQuery query) {

    WorkspaceEdit workspaceEdit = new WorkspaceEdit();

    try {
      InnovationContext context = new InnovationContext(unit, 0, unit.getBuffer().getLength() - 1);
      CUCorrectionProposal proposal =
          new CUCorrectionProposal("OrganizeImports", unit, IProposalRelevance.ORGANIZE_IMPORTS) {
            @Override
            protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
              CompilationUnit astRoot = context.getASTRoot();
              OrganizeImportsOperation op =
                  new OrganizeImportsOperation(unit, astRoot, true, false, true, query);

              editRoot.addChild(op.createTextEdit(null));
            }
          };

      addWorkspaceEdit(unit, proposal, workspaceEdit);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("Problem organize imports ", e);
    }

    return new OrganizeImportsResult(workspaceEdit, query.getRemainedConflicts());
  }

  private static void addWorkspaceEdit(
      ICompilationUnit cu, CUCorrectionProposal proposal, WorkspaceEdit rootEdit)
      throws CoreException {

    TextChange textChange = proposal.getTextChange();
    TextEdit edit = textChange.getEdit();
    TextEditConverter converter = new TextEditConverter(cu, edit);
    rootEdit.getChanges().put(JDTUtils.toURI(cu), converter.convert());
  }

  /** It is designed to preserve and resolve exposed conflicts. */
  private static class ConflictResolverChooseImportQuery implements IChooseImportQuery {
    private List<ImportConflicts> remainedConflicts;
    private final List<String> choices;

    private ConflictResolverChooseImportQuery(List<String> choices) {
      this.choices = choices;
      this.remainedConflicts = new ArrayList<>();
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

        ImportConflicts importConflicts = new ImportConflicts(matches);
        remainedConflicts.add(importConflicts);
      }

      return resolvedConflicts.toArray(new TypeNameMatch[resolvedConflicts.size()]);
    }

    public List<ImportConflicts> getRemainedConflicts() {
      return remainedConflicts;
    }
  }
}
