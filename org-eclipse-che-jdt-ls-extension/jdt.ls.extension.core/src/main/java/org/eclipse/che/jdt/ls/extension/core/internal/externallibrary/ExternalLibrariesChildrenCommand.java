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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibrary;

import com.google.gson.Gson;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A command to compute an external library nodes.
 *
 * @author Valeriy Svydenko
 */
public class ExternalLibrariesChildrenCommand {
  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Gets children of external library.
   *
   * @param parameters first parameter must be of type {@link ExternalLibrariesParameters} which
   *     contains library id and its path
   * @param pm a progress monitor
   * @return list of external library's entries
   */
  public static List<JarEntry> execute(List<Object> parameters, IProgressMonitor pm) {
    ExternalLibrariesParameters params =
        gson.fromJson(gson.toJson(parameters.get(0)), ExternalLibrariesParameters.class);
    try {
      return LibraryNavigation.getChildren(params.getNodeId(), params.getNodePath(), pm);
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
  }
}
