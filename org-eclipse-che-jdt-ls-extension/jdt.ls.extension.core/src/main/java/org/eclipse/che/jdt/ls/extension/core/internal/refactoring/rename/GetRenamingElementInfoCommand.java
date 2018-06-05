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
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.rename;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.RenameKind;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSelectionParams;
import org.eclipse.che.jdt.ls.extension.api.dto.RenamingElementInfo;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.che.jdt.ls.extension.core.internal.JavaModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

/**
 * Returns information about renamed type.
 *
 * @author Valeriy Svydenko
 */
public class GetRenamingElementInfoCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * Analyzes curesor position and finds type of selected element.
   *
   * @param arguments contains instance of {@link RenameSelectionParams}
   * @param pm progress monitor
   * @return information about selected element
   */
  public static RenamingElementInfo execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);

    ensureNotCancelled(pm);

    RenamingElementInfo result = new RenamingElementInfo();

    RenameSelectionParams params =
        GSON.fromJson(GSON.toJson(arguments.get(0)), RenameSelectionParams.class);

    try {
      RenameKind selectedElement = params.getRenameKind();
      if (RenameKind.JAVA_ELEMENT.equals(selectedElement)) {
        IJavaElement element =
            JavaModelUtil.getJavaElement(params.getPosition(), params.getResourceUri(), pm);
        if (element == null) {
          result.setRenameKind(RenameKind.UNKNOWN);
          return result;
        }
        result.setElementName(element.getElementName());
        result.setRenameKind(getRenameKind(element));
      } else if (RenameKind.COMPILATION_UNIT.equals(selectedElement)) {
        result.setRenameKind(selectedElement);
        ICompilationUnit cu = JDTUtils.resolveCompilationUnit(params.getResourceUri());
        result.setElementName(cu.getElementName());
      } else if (RenameKind.PACKAGE.equals(selectedElement)) {
        result.setRenameKind(selectedElement);
        IPackageFragment pack = JDTUtils.resolvePackage(params.getResourceUri());
        result.setElementName(pack.getElementName());
      }

      return result;
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }

    return result;
  }

  private static RenameKind getRenameKind(IJavaElement element) throws CoreException {
    if (element == null) {
      return null;
    }
    switch (element.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT:
        return RenameKind.PACKAGE;
      case IJavaElement.COMPILATION_UNIT:
        return RenameKind.COMPILATION_UNIT;
      case IJavaElement.TYPE:
        return RenameKind.TYPE;
      case IJavaElement.METHOD:
        final IMethod method = (IMethod) element;
        if (method.isConstructor()) {
          return RenameKind.TYPE;
        } else {
          return RenameKind.METHOD;
        }
      case IJavaElement.FIELD:
        if (JdtFlags.isEnum((IMember) element)) {
          return RenameKind.ENUM_CONSTANT;
        }
        return RenameKind.FIELD;
      case IJavaElement.TYPE_PARAMETER:
        return RenameKind.TYPE_PARAMETER;
      case IJavaElement.LOCAL_VARIABLE:
        return RenameKind.LOCAL_VARIABLE;
    }
    return null;
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), RenameSelectionParams.class.getName() + " is expected.");
  }
}
