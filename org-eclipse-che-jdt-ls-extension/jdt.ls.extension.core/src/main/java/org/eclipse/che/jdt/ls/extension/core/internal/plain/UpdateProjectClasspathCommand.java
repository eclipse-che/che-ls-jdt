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

import static java.lang.String.format;
import static org.eclipse.core.runtime.Path.fromOSString;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateClasspathParameters;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/**
 * The command for updating .classpath for simple java project.
 *
 * @param arguments a list contains {@link UpdateClasspathParameters}
 * @param pm a progress monitor
 * @author Valeriy Svydenko
 */
public class UpdateProjectClasspathCommand {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  /**
   * Updates .classpath in the given simple java project.
   *
   * @param arguments contains one argument: information about classpath {@link
   *     UpdateClasspathParameters}}
   * @param pm a progress monitor
   * @return uri of updated project
   */
  public static Object execute(List<Object> arguments, IProgressMonitor pm) {
    Preconditions.checkArgument(arguments.size() >= 1, "Information about .classpath is expected");

    UpdateClasspathParameters parameters =
        gson.fromJson(gson.toJson(arguments.get(0)), UpdateClasspathParameters.class);

    final String projectUri = parameters.getProjectUri();
    final List<ClasspathEntry> entries = parameters.getEntries();

    IJavaProject jProject = JavaModelUtil.getJavaProject(projectUri);

    if (jProject == null) {
      throw new IllegalArgumentException(format("Project for '%s' not found", projectUri));
    }

    try {
      jProject.setRawClasspath(createModifiedEntry(entries), jProject.getOutputLocation(), pm);
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }

    return projectUri;
  }

  private static IClasspathEntry[] createModifiedEntry(List<ClasspathEntry> entries) {
    List<IClasspathEntry> coreClasspathEntries = new ArrayList<>(entries.size());
    for (ClasspathEntry entry : entries) {
      IPath path = fromOSString(entry.getPath());
      int entryKind = entry.getEntryKind();
      if (IClasspathEntry.CPE_LIBRARY == entryKind) {
        coreClasspathEntries.add(JavaCore.newLibraryEntry(path, null, null));
      } else if (IClasspathEntry.CPE_SOURCE == entryKind) {
        coreClasspathEntries.add(JavaCore.newSourceEntry(path));
      } else if (IClasspathEntry.CPE_VARIABLE == entryKind) {
        coreClasspathEntries.add(JavaCore.newVariableEntry(path, null, null));
      } else if (IClasspathEntry.CPE_CONTAINER == entryKind) {
        coreClasspathEntries.add(JavaCore.newContainerEntry(path));
      } else if (IClasspathEntry.CPE_PROJECT == entryKind) {
        coreClasspathEntries.add(JavaCore.newProjectEntry(path));
      }
    }
    return coreClasspathEntries.toArray(new IClasspathEntry[coreClasspathEntries.size()]);
  }
}
