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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;
import static org.eclipse.jdt.ls.core.internal.handlers.WorkspaceDiagnosticsHandler.toDiagnosticsArray;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.JsonRpcHelpers;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

/**
 * Command to recompute diagnostics.
 *
 * @author Valeriy Svydenko
 */
public class RecomputePomDiagnosticsCommand {
  /**
   * Recomputes diagnostics for the pom.xml.
   *
   * @param arguments contains uri of pom.xml
   * @param pm progress monitor
   * @return true if diagnostics were published otherwise returns false
   */
  public static Boolean execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(arguments.size() >= 1, "Resource uri is expected");

    ensureNotCancelled(pm);

    final String fileUri = (String) arguments.get(0);

    PublishDiagnosticsParams diagnostics = computeDiagnostics(fileUri);
    if (diagnostics == null) {
      return false;
    }

    JavaLanguageServerPlugin.getInstance().getClientConnection().publishDiagnostics(diagnostics);

    return true;
  }

  static PublishDiagnosticsParams computeDiagnostics(String fileUri) {
    IFile file = JDTUtils.findFile(fileUri);
    if (file == null) {
      return null;
    }

    IMarker[] markers = null;

    try {
      markers = file.findMarkers(null, true, 1);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("Can't find markers for: " + fileUri, e);
    }
    IDocument document = JsonRpcHelpers.toDocument(file);

    return new PublishDiagnosticsParams(
        ResourceUtils.toClientUri(fileUri), toDiagnosticsArray(document, markers));
  }
}
