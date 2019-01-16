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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static org.eclipse.jdt.core.IClasspathEntry.CPE_SOURCE;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Computes source folders of the project.
 *
 * @author Valeriy Svydenko
 */
public class GetSourceFoldersCommand {

  /**
   * Gets source folders.
   *
   * @param arguments contains one argument: project uri of the project
   * @param pm a progress monitor
   * @return list of source folders
   */
  public static List<String> execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(arguments.size() >= 1, "Project uri is expected");
    final String projectUri = (String) arguments.get(0);

    IJavaProject jProject = JavaModelUtil.getJavaProject(projectUri);

    if (jProject == null) {
      // the java project might not have been created yet.
      return Collections.emptyList();
    }

    List<String> sourceFolders = new ArrayList<>();
    IClasspathEntry[] classpath;
    try {
      classpath = jProject.getRawClasspath();
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
    for (IClasspathEntry entry : classpath) {
      if (CPE_SOURCE == entry.getEntryKind()) {
        sourceFolders.add(JavaModelUtil.getFolderLocation(entry.getPath()));
      }
    }
    return sourceFolders;
  }
}
