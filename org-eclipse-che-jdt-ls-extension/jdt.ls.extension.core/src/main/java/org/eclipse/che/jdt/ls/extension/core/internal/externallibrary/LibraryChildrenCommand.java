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
package org.eclipse.che.jdt.ls.extension.core.internal.externallibrary;

import com.google.gson.Gson;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A command to compute jar's children.
 *
 * @author Valeriy Svydenko
 */
public class LibraryChildrenCommand {
  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Gets children of external library node.
   *
   * @param parameters first parameter must be of type {@link ExternalLibrariesParameters} which
   *     contains library id
   * @param pm a progress monitor
   * @return list of entries
   */
  public static List<JarEntry> execute(List<Object> parameters, IProgressMonitor pm) {
    ExternalLibrariesParameters params =
        gson.fromJson(gson.toJson(parameters.get(0)), ExternalLibrariesParameters.class);
    try {
      return LibraryNavigation.getPackageFragmentRootContent(params.getNodeId(), pm);
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
  }
}
