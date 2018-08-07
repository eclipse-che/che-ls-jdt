/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.jdt.ls.extension.core.internal.refactoring.move;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;
import org.eclipse.che.jdt.ls.extension.api.dto.MoveSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.Resource;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ProjectUtils;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.reorg.IReorgDestination;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.reorg.IReorgPolicy.IMovePolicy;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.reorg.ReorgDestinationFactory;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.reorg.ReorgPolicyFactory;

/**
 * The command to check destination.
 *
 * @author Valeriy Svydenko
 */
public class VerifyMoveDestinationCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * The command checks if destination is available.
   *
   * @param arguments instance of {@link MoveSettings}
   * @return status of the move operation {@link RefactoringStatus}
   */
  public static RefactoringStatus execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);
    ensureNotCancelled(pm);

    MoveSettings moveSettings = GSON.fromJson(GSON.toJson(arguments.get(0)), MoveSettings.class);
    String destinationUri = moveSettings.getDestination();

    if (destinationUri == null || destinationUri.isEmpty()) {
      return createBadStatus("URI of the destination hs to be set.");
    }

    List<IJavaElement> elements = convertToJavaElements(moveSettings.getElements());
    IJavaElement[] javaElements = new IJavaElement[elements.size()];
    javaElements = elements.toArray(javaElements);

    IResource[] resources = {};
    RefactoringStatus result = new RefactoringStatus();

    try {
      IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(resources, javaElements);
      JavaMoveProcessor processor = policy.canEnable() ? new JavaMoveProcessor(policy) : null;
      if (processor == null) {
        return createBadStatus("Can't create move processor");
      }

      IPackageFragment resolvePackage = getDestination(JDTUtils.toURI(destinationUri));
      if (resolvePackage == null) {
        return createBadStatus("The destination can't be used.");
      }

      IReorgDestination destination = ReorgDestinationFactory.createDestination(resolvePackage);
      org.eclipse.ltk.core.refactoring.RefactoringStatus status =
          processor.setDestination(destination);

      if (status == null) {
        return createBadStatus("Can't validate destination.");
      }
      result.setRefactoringSeverity(RefactoringSeverity.valueOf(status.getSeverity()));
      List<RefactoringStatusEntry> entries = new LinkedList<>();
      for (org.eclipse.ltk.core.refactoring.RefactoringStatusEntry entry : status.getEntries()) {
        RefactoringStatusEntry resultEntry = new RefactoringStatusEntry();
        resultEntry.setMessage(entry.getMessage());
        resultEntry.setRefactoringSeverity(RefactoringSeverity.valueOf(entry.getSeverity()));
        entries.add(resultEntry);
      }
      result.setRefactoringStatusEntries(entries);
    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }
    return result;
  }

  private static IPackageFragment getDestination(URI destinationUri) throws JavaModelException {
    IFolder resource =
        (IFolder)
            JDTUtils.findResource(
                destinationUri,
                ResourcesPlugin.getWorkspace().getRoot()::findContainersForLocationURI);
    if (resource == null) {
      return null;
    }
    IProject project = resource.getProject();
    if (!ProjectUtils.isJavaProject(project)) {
      return null;
    }
    IJavaElement element = JavaCore.create(resource);
    IJavaProject javaProject = element.getJavaProject();
    return javaProject.findPackageFragment(element.getPath());
  }

  private static RefactoringStatus createBadStatus(String message) {
    RefactoringStatus status = new RefactoringStatus();
    status.setRefactoringSeverity(RefactoringSeverity.FATAL);
    RefactoringStatusEntry entry = new RefactoringStatusEntry();
    entry.setRefactoringSeverity(RefactoringSeverity.FATAL);
    entry.setMessage(message);
    status.setRefactoringStatusEntries(Collections.singletonList(entry));

    return status;
  }

  private static List<IJavaElement> convertToJavaElements(List<Resource> resources) {
    List<IJavaElement> result = new LinkedList();
    for (Resource resourceToMove : resources) {
      if (resourceToMove.isPack()) {
        IPackageFragment pack = JDTUtils.resolvePackage(resourceToMove.getUri());
        if (pack != null) {
          result.add(pack);
        }
      } else {
        ICompilationUnit unit = JDTUtils.resolveCompilationUnit(resourceToMove.getUri());
        if (unit != null) {
          result.add(unit);
        }
      }
    }
    return result;
  }

  private static void validateArguments(List<Object> arguments) {
    Preconditions.checkArgument(
        !arguments.isEmpty(), VerifyMoveDestinationCommand.class.getName() + " is expected.");
  }
}
