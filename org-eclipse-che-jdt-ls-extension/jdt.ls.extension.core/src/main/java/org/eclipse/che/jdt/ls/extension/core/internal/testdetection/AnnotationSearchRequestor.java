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

import java.util.Collection;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/** Request for searching test classes. */
public class AnnotationSearchRequestor extends SearchRequestor {

  private final Collection<IType> fResult;
  private final ITypeHierarchy fHierarchy;

  AnnotationSearchRequestor(ITypeHierarchy hierarchy, Collection<IType> result) {
    fHierarchy = hierarchy;
    fResult = result;
  }

  @Override
  public void acceptSearchMatch(SearchMatch match) {
    if (match.getAccuracy() == SearchMatch.A_ACCURATE && !match.isInsideDocComment()) {
      Object element = match.getElement();
      if (element instanceof IType) {
        addTypeAndSubtypes((IType) element);
      } else if (element instanceof IMethod) {
        addTypeAndSubtypes(((IMethod) element).getDeclaringType());
      }
    }
  }

  private void addTypeAndSubtypes(IType type) {
    if (!isAccessibleType(type)) {
      return;
    }

    if (fResult.add(type)) {
      IType[] subclasses = fHierarchy.getSubclasses(type);
      for (IType subclass : subclasses) {
        addTypeAndSubtypes(subclass);
      }
    }
  }

  private boolean isAccessibleType(IType type) {
    try {
      if (isAccessibleClass(type) && !Flags.isAbstract(type.getFlags())) {
        return true;
      }
    } catch (JavaModelException e) {
      return false;
    }
    return false;
  }

  private static boolean isAccessibleClass(IType type) throws JavaModelException {
    int flags = type.getFlags();
    if (Flags.isInterface(flags)) {
      return false;
    }
    IJavaElement parent = type.getParent();
    while (true) {
      if (parent instanceof ICompilationUnit || parent instanceof IClassFile) {
        return true;
      }
      if (!(parent instanceof IType) || !Flags.isStatic(flags) || !Flags.isPublic(flags)) {
        return false;
      }
      flags = ((IType) parent).getFlags();
      parent = parent.getParent();
    }
  }
}
