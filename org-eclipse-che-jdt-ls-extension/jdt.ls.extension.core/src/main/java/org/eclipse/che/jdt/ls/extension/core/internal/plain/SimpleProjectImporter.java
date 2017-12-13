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

import static org.eclipse.jdt.core.IJavaProject.CLASSPATH_FILE_NAME;

import java.util.Collection;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.ls.core.internal.AbstractProjectImporter;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.managers.BasicFileDetector;

/**
 * The importer for simple java project. The project would be imported if it contained .classpath
 * file in the project root folder.
 *
 * @author Valeriy Svydenko
 */
public class SimpleProjectImporter extends AbstractProjectImporter {
  private static final String POM_FILE = "pom.xml";

  private Collection<java.nio.file.Path> projectDir;

  @Override
  public boolean applies(IProgressMonitor monitor) throws CoreException {
    if (projectDir == null) {
      BasicFileDetector classpathDetector =
          new BasicFileDetector(rootFolder.toPath(), CLASSPATH_FILE_NAME).addExclusions("**/bin");

      BasicFileDetector pomDetector =
          new BasicFileDetector(rootFolder.toPath(), POM_FILE).addExclusions("**/bin");
      pomDetector.maxDepth(1);
      Collection<java.nio.file.Path> pomParent = pomDetector.scan(monitor);

      // is Maven project
      if (!pomParent.isEmpty()) {
        return false;
      }

      projectDir = classpathDetector.scan(monitor);
    }
    return !projectDir.isEmpty();
  }

  @Override
  public void reset() {
    projectDir = null;
  }

  @Override
  public void importToWorkspace(IProgressMonitor monitor) throws CoreException {
    if (!applies(monitor)) {
      return;
    }
    SubMonitor subMonitor = SubMonitor.convert(monitor, projectDir.size());
    JavaLanguageServerPlugin.logInfo("Importing Simple Java project");
    projectDir.forEach(d -> importDir(d, subMonitor.newChild(1)));
    subMonitor.done();
  }

  private void importDir(java.nio.file.Path dir, IProgressMonitor pm) {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IPath projectPath = new Path(dir.toAbsolutePath().toString());
    try {
      String name = projectPath.lastSegment();
      IProject project = workspace.getRoot().getProject(name);
      IProjectDescription description =
          JavaLanguageServerPlugin.getProjectsManager().getDefaultProject().getDescription();
      description.setName(name);
      description.setLocation(projectPath);

      project.create(description, pm);
      project.open(IResource.NONE, pm);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.log(e.getStatus());
      throw new RuntimeException(e);
    } finally {
      pm.done();
    }
  }
}
