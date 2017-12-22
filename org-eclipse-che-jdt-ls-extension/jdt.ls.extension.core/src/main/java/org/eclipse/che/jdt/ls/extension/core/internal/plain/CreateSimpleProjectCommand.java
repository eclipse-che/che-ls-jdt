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
package org.eclipse.che.jdt.ls.extension.core.internal.plain;

import static org.eclipse.jdt.core.JavaCore.NATURE_ID;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.PATH_SEPARATOR;

import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

/**
 * Creates simple java project with default .classpath file.
 *
 * @param arguments a list contains project URI and source folder's name
 * @param pm a progress monitor
 * @return uri of created project
 * @author Valeriy Svydenko
 */
public class CreateSimpleProjectCommand {

  private static final String OUTPUT_FOLDER = "bin";

  /**
   * Creates simple java project.
   *
   * @param arguments contains two arguments: project uri and source folder's name
   * @param pm a progress monitor
   * @return uri of the project
   */
  public static Object execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(
        arguments.size() >= 2, "Project uri and source folder are expected");

    final String projectUri = (String) arguments.get(0);
    final String sourceFolder = (String) arguments.get(1);

    String projectName = projectUri.substring(projectUri.lastIndexOf(PATH_SEPARATOR) + 1);

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    JavaLanguageServerPlugin.logInfo("Creating the Simple Java project");
    // Create project
    try {
      IProjectDescription description = new ProjectDescription();
      description.setName(projectName);
      description.setNatureIds(new String[] {NATURE_ID});
      description.setLocationURI(JDTUtils.toURI(projectUri));

      project.create(description, pm);
      project.open(pm);

      // Turn into Java project
      IJavaProject javaProject = JavaCore.create(project);

      // Add build output folder
      IFolder output = project.getFolder(OUTPUT_FOLDER);
      javaProject.setOutputLocation(output.getFullPath(), pm);

      // Add source folder
      IFolder source = project.getFolder(sourceFolder);
      IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(source);
      IClasspathEntry src = JavaCore.newSourceEntry(root.getPath());

      // Find default JVM
      IClasspathEntry jre = JavaRuntime.getDefaultJREContainerEntry();

      // Add JVM to project class path
      javaProject.setRawClasspath(new IClasspathEntry[] {jre, src}, pm);

      JavaLanguageServerPlugin.logInfo("Finished creating the Simple Java project");
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException("Problem with creation Simple Java project", e);
    }
    return projectUri;
  }
}
