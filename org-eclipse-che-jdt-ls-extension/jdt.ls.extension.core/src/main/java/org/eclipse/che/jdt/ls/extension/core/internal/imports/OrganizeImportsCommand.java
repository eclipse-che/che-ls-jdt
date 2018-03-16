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
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportParams;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.OrganizeImportsOperation;
import org.eclipse.jdt.ls.core.internal.JDTDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.TextEditConverter;
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
   * Organizes imports either in a folder or in a specific file. In case of the file the ambiguous
   * importing types won't be ignored but rather returned to take decision on the client.
   *
   * @see org.eclipse.jdt.ls.core.internal.commands.OrganizeImportsCommand
   * @param arguments {@link OrganizeImportParams} expected
   */
  public static OrganizeImportsResult execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);
    ensureNotCancelled(pm);

    final OrganizeImportParams organizeImportsParams =
        JavaModelUtil.convertCommandParameter(arguments.get(0), OrganizeImportParams.class);

    File file = new File(JDTUtils.toURI(organizeImportsParams.getResourceUri()));
    if (!file.exists()) {
      return new OrganizeImportsResult();
    }

    if (file.isDirectory()) {
      return doOrganizeImportsInDirectory(arguments, pm);
    } else if (file.isFile()) {
      return doOrganizeImportsInFile(organizeImportsParams);
    }

    return new OrganizeImportsResult();
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), OrganizeImportParams.class.getName() + " is expected.");
  }

  /**
   * Organizes imports in all files of the underlying directory.
   *
   * <p>{@link
   * org.eclipse.jdt.ls.core.internal.commands.OrganizeImportsCommand#organizeImportsInDirectory(String,
   * IProject)}
   */
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

  /**
   * Organize imports in the file.
   *
   * <p>{@link
   * org.eclipse.jdt.ls.core.internal.commands.OrganizeImportsCommand#organizeImportsInFile(String)}
   */
  private static OrganizeImportsResult doOrganizeImportsInFile(
      OrganizeImportParams organizeImportParams) {

    ICompilationUnit cu = JDTUtils.resolveCompilationUnit(organizeImportParams.getResourceUri());
    if (cu == null) {
      return new OrganizeImportsResult();
    }

    ConflictResolver conflictResolver = new ConflictResolver(organizeImportParams.getChoices());
    WorkspaceEdit rootEdit = new WorkspaceEdit();

    try {
      InnovationContext context = new InnovationContext(cu, 0, cu.getBuffer().getLength() - 1);
      CUCorrectionProposal proposal =
          new CUCorrectionProposal("OrganizeImports", cu, IProposalRelevance.ORGANIZE_IMPORTS) {
            @Override
            protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
              CompilationUnit astRoot = context.getASTRoot();
              OrganizeImportsOperation op =
                  new OrganizeImportsOperation(cu, astRoot, true, false, true, conflictResolver);

              editRoot.addChild(op.createTextEdit(null));
            }
          };

      addWorkspaceEdit(cu, proposal, rootEdit);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("Problem organize imports ", e);
    }

    return new OrganizeImportsResult(rootEdit, conflictResolver.getRemainingConflicts());
  }

  private static void addWorkspaceEdit(
      ICompilationUnit cu, CUCorrectionProposal proposal, WorkspaceEdit rootEdit)
      throws CoreException {

    TextChange textChange = proposal.getTextChange();
    TextEdit edit = textChange.getEdit();
    TextEditConverter converter = new TextEditConverter(cu, edit);
    rootEdit.getChanges().put(JDTUtils.toURI(cu), converter.convert());
  }
}
