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
package org.eclipse.che.jdt.ls.extension.core.internal;

import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECTS_CLASSPATH;
import static org.eclipse.jdt.core.IJavaElementDelta.F_ADDED_TO_CLASSPATH;
import static org.eclipse.jdt.core.IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED;
import static org.eclipse.jdt.core.IJavaElementDelta.F_CLASSPATH_CHANGED;
import static org.eclipse.jdt.core.IJavaElementDelta.F_REMOVED_FROM_CLASSPATH;
import static org.eclipse.jdt.core.IJavaElementDelta.F_REORDER;
import static org.eclipse.jdt.core.IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.JDTLanguageServer;

/**
 * Provides Java Model Element Change events to the clients
 *
 * @author Victor Rubezhny
 */
@SuppressWarnings("restriction")
public class JavaModelEventProvider implements IElementChangedListener {

  private static final int CLASSPATH_CHANGED_MASK =
      F_ADDED_TO_CLASSPATH
          | F_CLASSPATH_CHANGED
          | F_REMOVED_FROM_CLASSPATH
          | F_RESOLVED_CLASSPATH_CHANGED
          | F_ARCHIVE_CONTENT_CHANGED;

  public JavaModelEventProvider() {}

  @Override
  public void elementChanged(ElementChangedEvent event) {
    Set<IProject> projects = getAffectedProjects(event.getDelta(), new HashSet<>());
    if (projects.isEmpty()) {
      return;
    }

    try {
      Set<String> projectLocations = new HashSet<>();
      for (IProject project : projects) {
        projectLocations.add(ResourceUtils.fixURI(project.getLocationURI()));
      }

      JDTLanguageServer ls = JavaLanguageServerPlugin.getInstance().getProtocol();
      ls.getClientConnection()
          .executeClientCommand(
              CLIENT_UPDATE_PROJECTS_CLASSPATH,
              (Object[]) projectLocations.toArray(new String[projectLocations.size()]));
    } catch (Exception e) {
      // Ignore.
      JavaLanguageServerPlugin.logException(
          "An exception occured while reporting project CLASSPATH change", e);
    }
  }

  private Set<IProject> getAffectedProjects(
      IJavaElementDelta delta, Set<IProject> affectedProjects) {
    if (((delta.getFlags() & CLASSPATH_CHANGED_MASK) != 0)
        || (delta.getFlags() == F_REORDER && delta.getElement() instanceof IPackageFragmentRoot)) {
      IJavaProject javaProject = delta.getElement().getJavaProject();
      if (javaProject != null) {
        affectedProjects.add(javaProject.getProject());
      }
    }

    for (IJavaElementDelta childDelta : delta.getAffectedChildren()) {
      affectedProjects.addAll(getAffectedProjects(childDelta, affectedProjects));
    }
    return affectedProjects;
  }
}
