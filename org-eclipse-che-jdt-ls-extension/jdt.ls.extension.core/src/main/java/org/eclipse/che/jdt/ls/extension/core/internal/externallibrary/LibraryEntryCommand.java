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

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.ls.core.internal.JDTUtils;

/**
 * A command to compute entry.
 *
 * @author Valeriy Svydenko
 * @author Anatolii Bazko
 */
public class LibraryEntryCommand {

  /**
   * Gets jar entry by its uri.
   *
   * @param parameters resource uri is expected
   * @param pm a progress monitor
   * @return entry {@link JarEntry}
   */
  public static JarEntry execute(List<Object> parameters, IProgressMonitor pm) {
    validateParameters(parameters);
    ensureNotCancelled(pm);

    final String resourceUri = (String) parameters.get(0);

    IClassFile classFile = JDTUtils.resolveClassFile(resourceUri);
    if (classFile == null) {
      return null;
    }

    JarEntry jarEntry = new JarEntry();
    jarEntry.setUri(resourceUri);
    jarEntry.setPath(classFile.findPrimaryType().getFullyQualifiedName());
    jarEntry.setName(classFile.getElementName());
    jarEntry.setEntryType(LibraryNavigation.CLASS_FILE_ENTRY_TYPE);
    return jarEntry;
  }

  private static void validateParameters(List<Object> parameters) {
    Preconditions.checkArgument(!parameters.isEmpty(), "Resource uri is expected");
  }
}
