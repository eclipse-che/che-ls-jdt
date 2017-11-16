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
import static java.util.Collections.singletonList;
import static org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil.getJavaProject;
import static org.eclipse.jdt.ls.core.internal.JDTUtils.resolveCompilationUnit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ls.core.internal.JDTUtils;

/** Class which finds test classes and test methods for java test frameworks. */
public class JavaTestFinder {
  /**
   * Finds test method which is related to the cursor position.
   *
   * @param compilationUnit compilation unit of class
   * @param cursorOffset cursor position
   * @return declaration of test method. (Example: full.qualified.name.of.Class#methodName)
   */
  public static List<String> findTestMethodDeclaration(
      ICompilationUnit compilationUnit, int cursorOffset) {
    if (compilationUnit == null) {
      return emptyList();
    }
    IType primaryType = compilationUnit.findPrimaryType();
    String qualifiedName = primaryType.getFullyQualifiedName();
    try {
      IJavaElement element = compilationUnit.getElementAt(cursorOffset);
      if (element instanceof IMethod) {
        IMethod method = (IMethod) element;
        qualifiedName = qualifiedName + '#' + method.getElementName();
      }
    } catch (JavaModelException e) {
      return emptyList();
    }
    return singletonList(qualifiedName);
  }

  /**
   * Finds test class declaration.
   *
   * @param compilationUnit compilation unit of class
   * @return declaration of test class which should be ran.
   */
  public static List<String> findTestClassDeclaration(ICompilationUnit compilationUnit) {
    if (compilationUnit == null) {
      return emptyList();
    }
    IType primaryType = compilationUnit.findPrimaryType();
    return singletonList(primaryType.getFullyQualifiedName());
  }

  /**
   * Finds test classes in package.
   *
   * @param packageUri URI of package folder
   * @param testMethodAnnotation java annotation which describes test method in the test framework
   * @param testClassAnnotation java annotation which describes test class in the test framework
   * @return list of test classes which should be ran.
   */
  public static List<String> findTestClassesInPackage(
      String packageUri, String testMethodAnnotation, String testClassAnnotation) {
    IPackageFragment packageFragment;

    IJavaProject javaProject = getJavaProject(packageUri);
    if (javaProject == null) {
      return null;
    }

    try {
      packageFragment =
          javaProject.findPackageFragment(JDTUtils.findFile(packageUri).getFullPath());
    } catch (JavaModelException e) {
      return emptyList();
    }
    return packageFragment == null
        ? emptyList()
        : findClassesInContainer(packageFragment, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Finds test classes in project.
   *
   * @param projectUri URI of java project
   * @param testMethodAnnotation java annotation which describes test method in the test framework
   * @param testClassAnnotation java annotation which describes test class in the test framework
   * @return list of test classes which should be ran.
   */
  public static List<String> findTestClassesInProject(
      String projectUri, String testMethodAnnotation, String testClassAnnotation) {

    IJavaProject javaProject = getJavaProject(projectUri);

    if (javaProject == null) {
      return emptyList();
    }

    return findClassesInContainer(javaProject, testMethodAnnotation, testClassAnnotation);
  }

  /**
   * Checks if a method is test method.
   *
   * @param method method which should be checked
   * @param compilationUnit parent of the method
   * @param testAnnotation java annotation which describes test method in the test framework
   * @return {@code true} if the method is test method
   */
  public static boolean isTest(
      IMethod method, ICompilationUnit compilationUnit, String testAnnotation) {
    try {
      IAnnotation[] annotations = method.getAnnotations();
      IAnnotation test = null;
      for (IAnnotation annotation : annotations) {
        String annotationElementName = annotation.getElementName();
        if ("Test".equals(annotationElementName)) {
          test = annotation;
          break;
        }
        if (testAnnotation.equals(annotationElementName)) {
          return true;
        }
      }
      return test != null && isImportOfTestAnnotationExist(compilationUnit, testAnnotation);
    } catch (JavaModelException e) {
      return false;
    }
  }

  /**
   * Gets fqns of the classes.
   *
   * @param classes list of classes
   * @return list of fqns
   */
  public static List<String> getFqns(List<String> classes) {
    if (classes == null) {
      return emptyList();
    }
    List<String> result = new LinkedList<>();
    for (String classPath : classes) {
      ICompilationUnit compilationUnit = resolveCompilationUnit(classPath);
      if (compilationUnit != null) {
        IType primaryType = compilationUnit.findPrimaryType();
        result.add(primaryType.getFullyQualifiedName());
      }
    }

    return result;
  }

  private static boolean isImportOfTestAnnotationExist(
      ICompilationUnit compilationUnit, String testAnnotation) {
    try {
      IImportDeclaration[] imports = compilationUnit.getImports();
      for (IImportDeclaration importDeclaration : imports) {
        String elementName = importDeclaration.getElementName();
        if (testAnnotation.equals(elementName)) {
          return true;
        }
        if (importDeclaration.isOnDemand()
            && testAnnotation.startsWith(
                elementName.substring(0, elementName.length() - 2))) { // remove .*
          return true;
        }
      }
    } catch (JavaModelException e) {
      return false;
    }
    return false;
  }

  private static List<String> findClassesInContainer(
      IJavaElement container, String testMethodAnnotation, String testClassAnnotation) {
    List<String> result = new LinkedList<>();
    try {
      IRegion region = getRegion(container);
      ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
      IType[] allClasses = hierarchy.getAllClasses();

      // search for all types with references to RunWith and Test and all subclasses
      HashSet<IType> candidates = new HashSet<>(allClasses.length);
      SearchRequestor requestor = new AnnotationSearchRequestor(hierarchy, candidates);

      IJavaSearchScope scope =
          SearchEngine.createJavaSearchScope(allClasses, IJavaSearchScope.SOURCES);
      int matchRule = SearchPattern.R_CASE_SENSITIVE;

      SearchPattern testPattern =
          SearchPattern.createPattern(
              testMethodAnnotation,
              IJavaSearchConstants.ANNOTATION_TYPE,
              IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
              matchRule);

      SearchPattern runWithPattern =
          testClassAnnotation == null || testClassAnnotation.isEmpty()
              ? testPattern
              : SearchPattern.createPattern(
                  testClassAnnotation,
                  IJavaSearchConstants.ANNOTATION_TYPE,
                  IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
                  matchRule);

      SearchPattern annotationsPattern = SearchPattern.createOrPattern(runWithPattern, testPattern);
      SearchParticipant[] searchParticipants =
          new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
      new SearchEngine().search(annotationsPattern, searchParticipants, scope, requestor, null);

      for (IType candidate : candidates) {
        result.add(candidate.getFullyQualifiedName());
      }
    } catch (Exception e) {
      emptyList();
    }

    return result;
  }

  private static IRegion getRegion(IJavaElement element) throws JavaModelException {
    IRegion result = JavaCore.newRegion();
    if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
      // for projects only add the contained source folders
      IPackageFragmentRoot[] packageFragmentRoots =
          ((IJavaProject) element).getPackageFragmentRoots();
      for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
        if (!packageFragmentRoot.isArchive()) {
          result.add(packageFragmentRoot);
        }
      }
    } else {
      result.add(element);
    }
    return result;
  }
}
