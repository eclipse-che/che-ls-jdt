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
package org.eclipse.che.jdt.ls.extension.core.internal.pom;

import static org.eclipse.jdt.ls.core.internal.handlers.WorkspaceDiagnosticsHandler.toDiagnosticsArray;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
   * @param progressMonitor progress monitor
   * @return diagnostics
   */
  public static PublishDiagnosticsParams execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(arguments.size() >= 1, "Resource uri is expected");

    if (pm.isCanceled()) {
      throw new OperationCanceledException();
    }

    final String fileUri = (String) arguments.get(0);

    IFile file = JDTUtils.findFile(fileUri);

    String uri = JDTUtils.getFileURI(file);
    IMarker[] markers = null;

    try {
      markers = file.findMarkers(null, true, 1);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("Can't find markers for: " + fileUri, e);
    }
    IDocument document = JsonRpcHelpers.toDocument(file);

    return new PublishDiagnosticsParams(
        ResourceUtils.toClientUri(uri), toDiagnosticsArray(document, markers));
  }
}
