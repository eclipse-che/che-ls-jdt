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
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.rename;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.RenameKind;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSelectionParams;
import org.eclipse.che.jdt.ls.extension.core.internal.ChangeUtil;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.rename.RenameSupport;
import org.eclipse.lsp4j.Position;

/**
 * The command to validate new name.
 *
 * @author Valeriy Svydenko
 */
public class ValidateNewNameCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * Validates new name.
   *
   * @param arguments contains instance of {@link RenameSelectionParams}
   * @param pm progress monitor
   * @return satus of validation
   */
  public static RefactoringStatus execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);
    ensureNotCancelled(pm);

    RenameSelectionParams params =
        GSON.fromJson(GSON.toJson(arguments.get(0)), RenameSelectionParams.class);

    RefactoringStatus status = new RefactoringStatus();

    try {
      RenameKind renameType = params.getRenameKind();
      Position position = params.getPosition();
      String uri = params.getResourceUri();

      IJavaElement curr = null;
      switch (renameType) {
        case JAVA_ELEMENT:
          curr = JavaModelUtil.getJavaElement(position, uri, pm);
          break;
        case COMPILATION_UNIT:
          curr = JDTUtils.resolveCompilationUnit(uri);
          break;
        case PACKAGE:
          curr = JDTUtils.resolvePackage(uri);
        default:
          break;
      }

      if (curr == null) {
        return status;
      }

      RenameSupport renameSupport =
          RenameSupport.create(curr, params.getNewName(), RenameSupport.UPDATE_REFERENCES);
      JavaRenameProcessor processor = renameSupport.getJavaRenameProcessor();
      org.eclipse.ltk.core.refactoring.RefactoringStatus result =
          processor.checkNewElementName(params.getNewName());

      status = ChangeUtil.convertRefactoringStatus(result);

    } catch (CoreException ex) {
      JavaLanguageServerPlugin.logException("Can't validate new name: " + params.getNewName(), ex);
    }

    return status;
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), RenameSelectionParams.class.getName() + " is expected.");
  }
}
