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
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.CheWorkspaceEdit;
import org.eclipse.che.jdt.ls.extension.api.dto.MoveSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.Resource;
import org.eclipse.che.jdt.ls.extension.core.internal.ChangeUtil;
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
import org.eclipse.ltk.core.refactoring.Change;

/**
 * The command to perform move.
 *
 * @author Valeriy Svydenko
 */
public class MoveCommand {
  private static final Gson GSON = GsonUtils.getInstance();

  /**
   * The command executes Move refactoring.
   *
   * @param arguments {@link MoveSettings} expected
   * @return information about changes
   */
  public static CheWorkspaceEdit execute(List<Object> arguments, IProgressMonitor pm) {
    validateArguments(arguments);

    ensureNotCancelled(pm);

    CheWorkspaceEdit edit = new CheWorkspaceEdit();

    MoveSettings moveSettings = GSON.fromJson(GSON.toJson(arguments.get(0)), MoveSettings.class);

    String destinationUri = moveSettings.getDestination();
    if (destinationUri == null || destinationUri.isEmpty()) {
      return edit;
    }

    List<IJavaElement> elements = convertToJavaElements(moveSettings.getElements());
    IJavaElement[] javaElements = new IJavaElement[elements.size()];
    javaElements = elements.toArray(javaElements);
    ;
    IResource[] resources = {};

    try {
      IMovePolicy policy = ReorgPolicyFactory.createMovePolicy(resources, javaElements);
      JavaMoveProcessor processor = policy.canEnable() ? new JavaMoveProcessor(policy) : null;
      if (processor == null) {
        return edit;
      }

      IPackageFragment resolvePackage = getDestination(JDTUtils.toURI(destinationUri));
      if (resolvePackage == null) {
        return edit;
      }

      IReorgDestination destination = ReorgDestinationFactory.createDestination(resolvePackage);
      org.eclipse.ltk.core.refactoring.RefactoringStatus status =
          processor.setDestination(destination);

      if (status == null || !status.isOK()) {
        return edit;
      }

      setMoveSettings(processor, moveSettings);

      Change changes = processor.createChange(pm);
      if (changes == null) {
        return edit;
      }

      ChangeUtil.convertChanges(changes, edit);

    } catch (CoreException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }
    return edit;
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

  private static void setMoveSettings(JavaMoveProcessor processor, MoveSettings moveSettings) {
    processor.setUpdateReferences(moveSettings.isUpdateReferences());
    boolean updateQualifiedNames = moveSettings.isUpdateQualifiedNames();
    processor.setUpdateQualifiedNames(updateQualifiedNames);
    String filePatterns = moveSettings.getFilePatterns();
    if (updateQualifiedNames && filePatterns != null) {
      processor.setFilePatterns(filePatterns);
    }
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
        !arguments.isEmpty(), MoveCommand.class.getName() + " is expected.");
  }
}
