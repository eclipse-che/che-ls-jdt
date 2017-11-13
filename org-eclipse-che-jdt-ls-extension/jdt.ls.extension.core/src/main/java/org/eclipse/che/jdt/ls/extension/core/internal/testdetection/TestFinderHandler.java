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
package org.eclipse.che.jdt.ls.extension.core.internal.testdetection;

import static java.util.Collections.emptyList;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.createFolders;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.findFile;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.getPackageName;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.resolveCompilationUnit;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.toURI;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;

/** Class for finding test methods in the different areas. */
public class TestFinderHandler {
  private static final String FILE_CONTEXT_TYPE = "FILE";
  private static final String FOLDER_CONTEXT_TYPE = "FOLDER";
  private static final String SET_CONTEXT_TYPE = "SET";
  private static final String PROJECT_CONTEXT_TYPE = "PROJECT";
  private static final String CURSOR_POSITION_CONTEXT_TYPE = "CURSOR_POSITION";

  private static String fileUri;
  private static String projectUri;
  private static String testMethodAnnotation;
  private static String testClassAnnotation;
  private static String contextType;
  private static List<String> classes;
  private static int cursorOffset;

  /**
   * Finds tests in some area. The area is defined by first parameter of arguments - it is context
   * type.
   *
   * <p>The context type can be:
   *
   * <ul>
   *   <li>FILE - find tests in the file
   *   <li>FOLDER - find tests in the folder/package
   *   <li>SET - find tests in custom set of classes
   *   <li>PROJECT - find tests in the project
   *   <li>CURSOR_POSITION - find class in cursor position
   * </ul>
   *
   * The values of arguments are:
   *
   * <p>0: context type (String) 1: project URI (String) 2: fqn of test method annotation (String)
   * 3: fqn of test class annotation (String). If context type is CURSOR_POSITION 4: file URI
   * (String) 5: cursor offset (double) If context type is FILE or FOLDER 4: file URI (String) If
   * contex type is SET 4: list of classes (List<String>)
   *
   * @param arguments list of arguments
   * @return test methods' declarations
   */
  public static List<String> find(List<Object> arguments) {
    readArguments(arguments);
    JavaTestFinder finder = new JavaTestFinder();

    switch (contextType) {
      case FILE_CONTEXT_TYPE:
        JavaLanguageServerPlugin.logInfo(
            "*******************************************File URI string --> " + fileUri);
        URI uri = toURI(fileUri);
        JavaLanguageServerPlugin.logInfo(
            "*******************************************File URI --> " + uri);
        ICompilationUnit iCompilationUnit = resolveCompilationUnitInner(uri);
        JavaLanguageServerPlugin.logInfo(
            "*******************************************Inner compilation unit -> "
                + iCompilationUnit);
        return finder.findTestClassDeclaration(resolveCompilationUnit(fileUri));
      case FOLDER_CONTEXT_TYPE:
        return finder.findTestClassesInPackage(fileUri, testMethodAnnotation, testClassAnnotation);
      case SET_CONTEXT_TYPE:
        return finder.getFqns(classes);
      case PROJECT_CONTEXT_TYPE:
        return finder.findTestClassesInProject(
            projectUri, testMethodAnnotation, testClassAnnotation);
      case CURSOR_POSITION_CONTEXT_TYPE:
        return finder.findTestMethodDeclaration(resolveCompilationUnit(fileUri), cursorOffset);
    }

    return emptyList();
  }

  private static void readArguments(List<Object> arguments) {
    contextType = (String) arguments.get(0);
    projectUri = (String) arguments.get(1);
    testMethodAnnotation = (String) arguments.get(2);
    testClassAnnotation = (String) arguments.get(3);
    switch (contextType) {
      case CURSOR_POSITION_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        cursorOffset = ((Double) arguments.get(5)).intValue();
        break;
      case FILE_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        break;
      case FOLDER_CONTEXT_TYPE:
        fileUri = (String) arguments.get(4);
        break;
      case SET_CONTEXT_TYPE:
        classes = (List<String>) arguments.get(4);
        break;
    }
  }

  private static ICompilationUnit resolveCompilationUnitInner(URI uri) {
    if (uri != null && !"jdt".equals(uri.getScheme()) && uri.isAbsolute()) {
      IFile resource = findFile(uri);
      JavaLanguageServerPlugin.logInfo(
          "*******************************************Resource -> " + resource);
      if (resource != null) {
        if (!ProjectUtils.isJavaProject(resource.getProject())) {
          return null;
        }

        IJavaElement element = JavaCore.create(resource);
        JavaLanguageServerPlugin.logInfo(
            "*******************************************Element -> " + element);
        if (element instanceof ICompilationUnit) {
          return (ICompilationUnit) element;
        }
      }

      return resource == null ? getFakeCompilationUnit(uri, new NullProgressMonitor()) : null;
    } else {
      JavaLanguageServerPlugin.logInfo(
          "*******************************************Return NULL -> ");
      return null;
    }
  }

  private static ICompilationUnit getFakeCompilationUnit(URI uri, IProgressMonitor monitor) {
    if (uri != null && "file".equals(uri.getScheme()) && uri.getPath().endsWith(".java")) {
      Path path = Paths.get(uri);
      if (!Files.isReadable(path)) {
        JavaLanguageServerPlugin.logInfo(
            "*******************************************Return NULL2 -> ");
        return null;
      } else {
        IProject project = JavaLanguageServerPlugin.getProjectsManager().getDefaultProject();
        if (project != null && project.isAccessible()) {
          IJavaProject javaProject = JavaCore.create(project);
          String packageName = getPackageName(javaProject, uri);
          String fileName = path.getName(path.getNameCount() - 1).toString();
          String packagePath = packageName.replace(".", "/");
          IPath filePath =
              (new org.eclipse.core.runtime.Path("src")).append(packagePath).append(fileName);
          IFile file = project.getFile(filePath);
          if (!file.isLinked()) {
            try {
              createFolders(file.getParent(), monitor);
              file.createLink(uri, 256, monitor);
            } catch (CoreException var12) {
              String errMsg =
                  "Failed to create linked resource from " + uri + " to " + project.getName();
              JavaLanguageServerPlugin.logException(errMsg, var12);
            }
          }

          JavaLanguageServerPlugin.logInfo(
              "*******************************************file.isLinked() -> " + file.isLinked());
          return file.isLinked() ? (ICompilationUnit) JavaCore.create(file, javaProject) : null;
        } else {
          JavaLanguageServerPlugin.logInfo(
              "*******************************************Return NULL3 -> ");
          return null;
        }
      }
    } else {
      JavaLanguageServerPlugin.logInfo(
          "*******************************************Return NULL4 -> ");
      return null;
    }
  }
}
