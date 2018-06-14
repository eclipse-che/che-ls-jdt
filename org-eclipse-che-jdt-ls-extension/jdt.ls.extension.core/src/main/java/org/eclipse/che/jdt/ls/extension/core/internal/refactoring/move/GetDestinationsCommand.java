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
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.move;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaProjectStructure;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragment;
import org.eclipse.che.jdt.ls.extension.api.dto.PackageFragmentRoot;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

/**
 * The command to get all available destinations.
 *
 * @author Valeriy Svydenko
 */
public class GetDestinationsCommand {
  /**
   * Find all possible destinations.
   *
   * @return list of the destinations
   */
  public static List<JavaProjectStructure> execute(List<Object> arguments, IProgressMonitor pm) {
    ensureNotCancelled(pm);

    List<IJavaProject> workspaceJavaProjects = JavaModelUtil.getWorkspaceJavaProjects();
    List<JavaProjectStructure> result = new ArrayList<>();
    for (IJavaProject javaProject : workspaceJavaProjects) {
      if (javaProject.exists()) {
        JavaProjectStructure project = new JavaProjectStructure();
        String projectUri = JDTUtils.getFileURI(javaProject.getResource());
        project.setName(projectUri.substring(projectUri.lastIndexOf(JDTUtils.PATH_SEPARATOR) + 1));
        project.setUri(projectUri);
        try {
          project.setPackageRoots(toPackageRoots(javaProject, projectUri));
        } catch (CoreException e) {
          JavaLanguageServerPlugin.logException(e.getMessage(), e);
        }
        result.add(project);
      }
    }
    return result;
  }

  private static List<PackageFragmentRoot> toPackageRoots(
      IJavaProject javaProject, String projectUri) throws CoreException {
    IPackageFragmentRoot[] packageFragmentRoots = javaProject.getAllPackageFragmentRoots();
    List<PackageFragmentRoot> result = new ArrayList<>();
    for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
      if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE
          && javaProject.getPath().isPrefixOf(packageFragmentRoot.getPath())) {
        PackageFragmentRoot root = new PackageFragmentRoot();
        root.setUri(JDTUtils.getFileURI(packageFragmentRoot.getResource()));
        root.setProjectUri(projectUri);
        root.setPackages(toPackageFragments(packageFragmentRoot, projectUri));
        result.add(root);
      }
    }
    return result;
  }

  private static List<PackageFragment> toPackageFragments(
      IPackageFragmentRoot packageFragmentRoot, String projectUri) throws CoreException {
    IJavaElement[] children = packageFragmentRoot.getChildren();
    if (children == null) {
      return null;
    }
    List<PackageFragment> result = new ArrayList<>();
    for (IJavaElement child : children) {
      if (child instanceof IPackageFragment) {
        IPackageFragment packageFragment = (IPackageFragment) child;
        PackageFragment fragment = new PackageFragment();
        fragment.setName(packageFragment.getElementName());
        fragment.setUri(JDTUtils.getFileURI(packageFragment.getResource()));
        fragment.setProjectUri(projectUri);
        result.add(fragment);
      }
    }
    return result;
  }
}
