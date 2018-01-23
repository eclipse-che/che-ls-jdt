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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;

/** Utilities for working with JDT APIs */
public class JavaModelUtil {
  private static final Gson gson =
      new GsonBuilder()
          .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
          .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
          .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
          .create();

  /**
   * Finds java project {@link IJavaProject} by URI
   *
   * @param resourceUri URI of the project source
   * @return instance of {@link IJavaProject}, may return null if it can not associate the uri with
   *     a Java project
   */
  public static IJavaProject getJavaProject(String resourceUri) {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IContainer[] containers = root.findContainersForLocationURI(JDTUtils.toURI(resourceUri));

    if (containers.length == 0) {
      return null;
    }

    IContainer container = containers[0];
    IProject project = container.getProject();
    if (!project.exists()) {
      return null;
    }

    return JavaCore.create(project);
  }

  /**
   * Returns all user created java project which exist in current workspace. This method excludes
   * default {@code jdt.ls-java-project} project.
   *
   * @return all user created java projects in current workspace
   */
  public static List<IJavaProject> getWorkspaceJavaProjects() {
    return Arrays.stream(ResourcesPlugin.getWorkspace().getRoot().getProjects())
        // .filter(project -> !project.getName().equals("jdt.ls-java-project"))
        .map(project -> JavaCore.create((project)))
        .collect(Collectors.toList());
  }

  public static <T> T convertCommandParameter(Object param, Class<T> clazz) {
    return gson.fromJson(gson.toJson(param), clazz);
  }

  static SymbolKind mapKind(IJavaElement element) {
    if (element.getElementType() == IJavaElement.METHOD) {
      // workaround for https://github.com/eclipse/eclipse.jdt.ls/issues/422
      return SymbolKind.Method;
    }
    return DocumentSymbolHandler.mapKind(element);
  }
}
