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
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.move;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.util.List;
import java.util.function.Function;
import org.eclipse.che.jdt.ls.extension.api.dto.CreateMoveParams;
import org.eclipse.che.jdt.ls.extension.api.dto.Resource;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.RefactoringAvailabilityTester;

/**
 * The command to check availability of Move operation.
 *
 * @author Valeriy Svydenko
 */
public class ValidateMoveCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * The command checks if it's possible to execute Move refactoring.
   *
   * @param arguments list of {@link Resource} expected
   * @return true if operation is available otherwise - false
   */
  public static boolean execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);
    ensureNotCancelled(pm);

    CreateMoveParams moveParams =
        GSON.fromJson(GSON.toJson(arguments.get(0)), CreateMoveParams.class);

    Function<Resource, IJavaElement> mapper =
        resource -> {
          if (resource.isPack()) {
            return JDTUtils.resolvePackage(resource.getUri());
          } else {
            return JDTUtils.resolveCompilationUnit(resource.getUri());
          }
        };
    IJavaElement[] javaElements =
        moveParams.getResources().stream().map(mapper).toArray(IJavaElement[]::new);

    try {
      return RefactoringAvailabilityTester.isMoveAvailable(new IResource[0], javaElements);
    } catch (JavaModelException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
      return false;
    }
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), ValidateMoveCommand.class.getName() + " is expected.");
  }
}
