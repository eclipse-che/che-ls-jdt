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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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
      if (element instanceof IType || element instanceof IMethod) {
        IMember member = (IMember) element;
        IType type =
            member.getElementType() == IJavaElement.TYPE
                ? (IType) member
                : member.getDeclaringType();
        addTypeAndSubtypes(type);
      }
    }
  }

  private void addTypeAndSubtypes(IType type) {
    if (fResult.add(type)) {
      IType[] subclasses = fHierarchy.getSubclasses(type);
      for (IType subclass : subclasses) {
        addTypeAndSubtypes(subclass);
      }
    }
  }
}
