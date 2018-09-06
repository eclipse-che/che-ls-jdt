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

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;

/**
 * The command to get all available source folder paths.
 *
 * @author V. Rubezhny
 */
public class GetProjectSourceLocationsCommand {
  /**
   * Find all possible destination source folder paths.
   *
   * @return list of the destination source folder paths
   */
  public static List<String> execute(List<Object> arguments, IProgressMonitor pm) {
    ensureNotCancelled(pm);
    validateArguments(arguments);

    final String projectUri = (String) arguments.get(0);
    IJavaProject javaProject = JavaModelUtil.getJavaProject(projectUri);

    List<String> result = new ArrayList<>();
    if (javaProject != null && javaProject.exists()) {
      try {
        result.addAll(getAllPackageFragmentPaths(javaProject));
      } catch (CoreException e) {
        JavaLanguageServerPlugin.logException(e.getMessage(), e);
      }
    }
    return result;
  }

  private static List<String> getAllPackageFragmentPaths(IJavaProject javaProject)
      throws CoreException {
    List<String> result = new ArrayList<>();
    for (IPackageFragmentRoot packageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
      if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE
          && javaProject.getPath().isPrefixOf(packageFragmentRoot.getPath())) {
        result.addAll(getPackageFragmentPaths(packageFragmentRoot));
      }
    }
    return result;
  }

  private static List<String> getPackageFragmentPaths(IPackageFragmentRoot packageFragmentRoot)
      throws CoreException {
    List<String> result = new ArrayList<>();
    IJavaElement[] children = packageFragmentRoot.getChildren();
    if (children != null) {
      for (IJavaElement child : children) {
        if (child instanceof IPackageFragment) {
          IResource resource = child.getResource();
          if (resource != null && resource.getLocationURI() != null) {
            result.add(ResourceUtils.fixURI(resource.getLocationURI()));
          }
        }
      }
    }
    return result;
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(arguments.size() >= 1, "Project uri is expected");
  }
}
