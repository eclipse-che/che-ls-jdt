/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.ls.core.internal.JDTDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.WorkspaceEdit;

/**
 * A command to organize imports.
 *
 * @author Anatolii Bazko
 */
public class OrganizeImports {

  /**
   * Organize imports in a file on in a directory.
   *
   * @param parameters uri to the file to the directory
   * @return list of updated files
   */
  public static WorkspaceEdit execute(List<Object> parameters, IProgressMonitor pm) {
    Preconditions.checkArgument(parameters.size() >= 1, "File uri is expected.");
    ensureNotCanceled(pm);

    try {
      JDTDelegateCommandHandler delegateCommandHandler = new JDTDelegateCommandHandler();
      return (WorkspaceEdit)
          delegateCommandHandler.executeCommand("java.edit.organizeImports", parameters, pm);
    } catch (Exception e) {
      String fileUri = (String) parameters.get(0);
      JavaLanguageServerPlugin.logException(
          String.format("Failed to organize imports on '%s'", fileUri), e);
      return new WorkspaceEdit();
    }
  }

  private static void ensureNotCanceled(IProgressMonitor pm) {
    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }
  }
}
