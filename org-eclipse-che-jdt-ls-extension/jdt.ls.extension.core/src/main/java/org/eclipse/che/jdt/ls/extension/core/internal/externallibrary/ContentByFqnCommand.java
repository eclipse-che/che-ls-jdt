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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibrary;

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ClassContent;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/**
 * A command to get a content of file by fqn.
 *
 * @author Valeriy Svydenko
 */
public class ContentByFqnCommand {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  /**
   * Get file's content by fqn.
   *
   * @param parameters first parameter must be of type {@link ExternalLibrariesParameters} which
   *     contains project URI and the path of the library file
   * @param pm a progress monitor
   * @return content of the file {@link ClassContent}
   */
  public static ClassContent execute(List<Object> parameters, IProgressMonitor pm) {
    ExternalLibrariesParameters params =
        gson.fromJson(gson.toJson(parameters.get(0)), ExternalLibrariesParameters.class);
    try {
      String projectUri = params.getProjectUri();
      IJavaProject project = JavaModelUtil.getJavaProject(projectUri);
      if (project == null) {
        throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
      }
      return LibraryNavigation.getContent(project, params.getNodePath());
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }
}
