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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

/** Handler for test detection events. */
public class TestDetectionHandler {

  /**
   * Detects if the java class has tests.
   *
   * @param arguments a list contains file URI, fqn of test method annotation and cursor offset
   * @return test positions @see {@link TestPosition}
   */
  public Object detectTests(List<Object> arguments) {
    String fileUri = (String) arguments.get(0);
    String testAnnotation = (String) arguments.get(1);
    int cursorOffset = ((Double) arguments.get(2)).intValue();

    ICompilationUnit unit = JDTUtils.resolveCompilationUnit(fileUri);
    if (unit == null) {
      return Collections.emptyList();
    }

    List<TestPosition> result = new ArrayList<>();

    try {
      if (cursorOffset == -1) {
        addAllTestsMethod(result, unit, testAnnotation);
      } else {
        IJavaElement elementAt = unit.getElementAt(cursorOffset);
        if (elementAt != null && elementAt.getElementType() == IJavaElement.METHOD) {
          if (isTestMethod((IMethod) elementAt, unit, testAnnotation)) {
            result.add(createTestPosition((IMethod) elementAt));
          }
        } else {
          addAllTestsMethod(result, unit, testAnnotation);
        }
      }
    } catch (Exception e) {
      JavaLanguageServerPlugin.logException("Problem with test detection for " + fileUri, e);
    }
    return result;
  }

  private void addAllTestsMethod(
      List<TestPosition> result, ICompilationUnit compilationUnit, String testAnnotation)
      throws JavaModelException {
    for (IType type : compilationUnit.getAllTypes()) {
      for (IMethod method : type.getMethods()) {
        if (isTestMethod(method, compilationUnit, testAnnotation)) {
          result.add(createTestPosition(method));
        }
      }
    }
  }

  /**
   * Verify if the method is test method.
   *
   * @param unit compilation unit of the method
   * @param method method declaration
   * @return {@code true} if the method is test method otherwise returns {@code false}
   */
  private boolean isTestMethod(IMethod method, ICompilationUnit unit, String testAnnotation) {
    JavaTestFinder javaTestFinder = new JavaTestFinder();
    try {
      int flags = method.getFlags();
      // 'V' is void signature
      return !(method.isConstructor()
              || !Flags.isPublic(flags)
              || Flags.isAbstract(flags)
              || Flags.isStatic(flags)
              || !"V".equals(method.getReturnType()))
          && javaTestFinder.isTest(method, unit, testAnnotation);

    } catch (JavaModelException ignored) {
      return false;
    }
  }

  private TestPosition createTestPosition(IMethod method) throws JavaModelException {
    ISourceRange nameRange = method.getNameRange();
    ISourceRange sourceRange = method.getSourceRange();

    TestPosition testPosition = new TestPosition();
    testPosition.setTestBodyLength(sourceRange.getLength());
    testPosition.setTestNameLength(nameRange.getLength());
    testPosition.setTestNameStartOffset(nameRange.getOffset());
    testPosition.setTestName(method.getElementName());

    return testPosition;
  }
}
