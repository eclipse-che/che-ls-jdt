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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;
import org.eclipse.che.jdt.ls.extension.api.ResourceKind;
import org.eclipse.che.jdt.ls.extension.api.dto.CheResourceChange;
import org.eclipse.che.jdt.ls.extension.api.dto.CheWorkspaceEdit;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.manipulation.util.BasicElementLabels;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.Messages;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.TextEditConverter;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.changes.MoveCompilationUnitChange;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.changes.MovePackageChange;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.changes.RenameCompilationUnitChange;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.changes.RenamePackageChange;
import org.eclipse.jdt.ls.core.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.ls.core.internal.corext.util.JavaElementUtil;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.text.edits.TextEdit;

/**
 * Utility methods for converting Refactoring changes.
 *
 * @author Valeriy Svydenko
 */
public class ChangeUtil {

  /**
   * Converts changes to resource changes if resource changes are supported by the client otherwise
   * converts to TextEdit changes.
   *
   * @param change changes after Refactoring operation
   * @param edit instance of workspace edit changes
   * @throws CoreException
   */
  public static void convertChanges(Change change, CheWorkspaceEdit edit, IProgressMonitor pm)
      throws CoreException {
    if (change == null || !(change instanceof CompositeChange)) {
      return;
    }

    if (change instanceof CompositeChange) {
      convertCompositeChange(change, edit, pm);
    }
  }

  /**
   * Converts {@link RefactoringStatus} to dto object {@link
   * org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus}.
   *
   * @param status the object to be converted
   * @return dto object which describes status of the refactoring
   */
  public static org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus convertRefactoringStatus(
      RefactoringStatus status) {
    org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus result =
        new org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus();
    result.setRefactoringSeverity(RefactoringSeverity.valueOf(status.getSeverity()));

    List<RefactoringStatusEntry> entries = new ArrayList<>();
    for (org.eclipse.ltk.core.refactoring.RefactoringStatusEntry entry : status.getEntries()) {
      RefactoringStatusEntry newEntry = new RefactoringStatusEntry();
      newEntry.setMessage(entry.getMessage());
      newEntry.setRefactoringSeverity(RefactoringSeverity.valueOf(entry.getSeverity()));
      entries.add(newEntry);
    }

    result.setRefactoringStatusEntries(entries);
    return result;
  }

  private static void convertCompositeChange(
      Change change, CheWorkspaceEdit edit, IProgressMonitor pm) throws CoreException {
    if (change instanceof CompositeChange) {
      for (Change child : ((CompositeChange) change).getChildren()) {
        convertCompositeChange(child, edit, pm);
      }
    }
    if (change instanceof TextFileChange) {
      convertTextFileChange((TextFileChange) change, edit, pm);
      return;
    }

    Object modifiedElement = change.getModifiedElement();
    if (modifiedElement == null) {
      return;
    }
    if (!(modifiedElement instanceof IJavaElement)) {
      return;
    }

    if (change instanceof TextChange) {
      convertTextChange(edit, (IJavaElement) modifiedElement, (TextChange) change);
    } else if (change instanceof ResourceChange) {
      ResourceChange resourceChange = (ResourceChange) change;
      convertResourceChange(edit, resourceChange);
    }
  }

  private static void convertTextFileChange(
      TextFileChange change, CheWorkspaceEdit edit, IProgressMonitor pm) throws CoreException {
    IFile file = change.getFile();
    IDocument textDocument = change.getCurrentDocument(pm);
    TextEdit textEdit = change.getEdit();
    FileTextEditConverter converter = new FileTextEditConverter(textDocument, textEdit);
    edit.getChanges().put(JDTUtils.getFileURI(file), converter.convert());
  }

  private static void convertResourceChange(CheWorkspaceEdit edit, ResourceChange resourceChange)
      throws CoreException {
    if (!JavaLanguageServerPlugin.getPreferencesManager()
        .getClientPreferences()
        .isWorkspaceEditResourceChangesSupported()) {
      return;
    }

    // Resource change is needed and supported by client
    if (resourceChange instanceof RenameCompilationUnitChange) {
      convertCUResourceChange(edit, (RenameCompilationUnitChange) resourceChange);
    } else if (resourceChange instanceof RenamePackageChange) {
      convertRenamePackcageChange(edit, (RenamePackageChange) resourceChange);
    } else if (resourceChange instanceof MoveCompilationUnitChange) {
      convertMoveCUChange(edit, (MoveCompilationUnitChange) resourceChange);
    } else if (resourceChange instanceof MovePackageChange) {
      convertMovePackageChange(edit, (MovePackageChange) resourceChange);
    }
  }

  private static void convertMovePackageChange(
      CheWorkspaceEdit edit, MovePackageChange packChange) {
    CheResourceChange rc = new CheResourceChange();
    rc.setResourceKind(ResourceKind.FOLDER);
    IPath newPackageFragment =
        new Path(packChange.getPackage().getElementName().replace('.', IPath.SEPARATOR));
    String destinationUri = JDTUtils.getFileURI(packChange.getDestination().getResource());
    rc.setNewUri(destinationUri + JDTUtils.PATH_SEPARATOR + newPackageFragment);
    rc.setCurrent(JDTUtils.getFileURI(packChange.getPackage().getResource()));
    rc.setDescription(packChange.getName());
    edit.getCheResourceChanges().add(rc);
  }

  private static void convertMoveCUChange(CheWorkspaceEdit edit, MoveCompilationUnitChange cuChange)
      throws JavaModelException {
    ICompilationUnit modifiedCU = cuChange.getCu();
    String name = modifiedCU.getElementName();
    CheResourceChange rc = new CheResourceChange();
    rc.setResourceKind(ResourceKind.FILE);
    rc.setCurrent(JDTUtils.toURI(modifiedCU));
    IPackageFragment destinationPackage = cuChange.getDestinationPackage();
    String newPackageUri = JDTUtils.getFileURI(destinationPackage.getResource());
    String newUri = newPackageUri + JDTUtils.PATH_SEPARATOR + name;
    rc.setNewUri(newUri);
    rc.setDescription(cuChange.getName());
    edit.getCheResourceChanges().add(rc);

    // update package
    CompilationUnit unit =
        new RefactoringASTParser(IASTSharedValues.SHARED_AST_LEVEL).parse(modifiedCU, true);
    ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
    updatePackageStatement(
        unit,
        destinationPackage.getElementName(),
        unit.getPackage().getName().getFullyQualifiedName(),
        rewrite,
        modifiedCU);
    TextEdit textEdit = rewrite.rewriteAST();
    convertTextEdit(edit, modifiedCU, textEdit);
  }

  private static void convertRenamePackcageChange(
      CheWorkspaceEdit edit, RenamePackageChange packageChange) throws CoreException {
    IPackageFragment pack = (IPackageFragment) packageChange.getModifiedElement();
    List<ICompilationUnit> units = new ArrayList<>();
    if (packageChange.getRenameSubpackages()) {
      IPackageFragment[] allPackages = JavaElementUtil.getPackageAndSubpackages(pack);
      for (int i = 0; i < allPackages.length; i++) {
        IPackageFragment currentPackage = allPackages[i];
        units.addAll(Arrays.asList(currentPackage.getCompilationUnits()));
      }
    } else {
      units.addAll(Arrays.asList(pack.getCompilationUnits()));
    }

    // update package's declaration
    for (ICompilationUnit cu : units) {
      CompilationUnit unit =
          new RefactoringASTParser(IASTSharedValues.SHARED_AST_LEVEL).parse(cu, true);
      ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
      updatePackageStatement(
          unit, packageChange.getNewName(), packageChange.getOldName(), rewrite, cu);
      TextEdit textEdit = rewrite.rewriteAST();
      convertTextEdit(edit, cu, textEdit);
    }

    CheResourceChange rc = new CheResourceChange();
    IPath newPackageFragment = new Path(packageChange.getNewName().replace('.', IPath.SEPARATOR));
    IPath oldPackageFragment = new Path(packageChange.getOldName().replace('.', IPath.SEPARATOR));
    IPath newPackagePath =
        pack.getResource()
            .getLocation()
            .removeLastSegments(oldPackageFragment.segmentCount())
            .append(newPackageFragment);
    String newUri = ResourceUtils.fixURI(newPackagePath.toFile().toURI());
    rc.setNewUri(newUri);
    rc.setResourceKind(ResourceKind.FOLDER);
    rc.setDescription(packageChange.getName());
    String current = ResourceUtils.fixURI(pack.getResource().getRawLocationURI());
    if (packageChange.getRenameSubpackages() || !pack.hasSubpackages()) {
      rc.setCurrent(current);
      edit.getCheResourceChanges().add(rc);
    } else {
      for (ICompilationUnit unit : units) {
        CheResourceChange cuResourceChange = new CheResourceChange();
        cuResourceChange.setResourceKind(ResourceKind.FILE);
        cuResourceChange.setCurrent(ResourceUtils.fixURI(unit.getResource().getLocationURI()));
        IPath newCUPath = newPackagePath.append(unit.getPath().lastSegment());
        cuResourceChange.setNewUri(ResourceUtils.fixURI(newCUPath.toFile().toURI()));

        String description =
            Messages.format(
                RefactoringCoreMessages.MoveCompilationUnitChange_name,
                new String[] {BasicElementLabels.getFileName(unit), packageChange.getNewName()});
        cuResourceChange.setDescription(description);

        edit.getCheResourceChanges().add(cuResourceChange);
      }
    }
  }

  private static void convertCUResourceChange(
      CheWorkspaceEdit edit, RenameCompilationUnitChange cuChange) {
    ICompilationUnit modifiedCU = (ICompilationUnit) cuChange.getModifiedElement();
    CheResourceChange rc = new CheResourceChange();
    rc.setResourceKind(ResourceKind.FILE);
    String newCUName = cuChange.getNewName();
    IPath currentPath = modifiedCU.getResource().getLocation();
    rc.setCurrent(ResourceUtils.fixURI(modifiedCU.getResource().getRawLocationURI()));
    IPath newPath = currentPath.removeLastSegments(1).append(newCUName);
    rc.setNewUri(ResourceUtils.fixURI(newPath.toFile().toURI()));
    rc.setDescription(cuChange.getName());
    edit.getCheResourceChanges().add(rc);
  }

  private static void convertTextChange(
      CheWorkspaceEdit root, IJavaElement element, TextChange textChange) {
    TextEdit textEdits = textChange.getEdit();
    if (textEdits == null) {
      return;
    }
    ICompilationUnit compilationUnit =
        (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
    convertTextEdit(root, compilationUnit, textEdits);
  }

  private static void convertTextEdit(
      CheWorkspaceEdit root, ICompilationUnit unit, TextEdit textEdits) {
    TextEdit[] children = textEdits.getChildren();
    if (children.length == 0) {
      return;
    }
    for (TextEdit textEdit : children) {
      TextEditConverter converter = new TextEditConverter(unit, textEdit);
      String uri = JDTUtils.toURI(unit);
      Map<String, List<org.eclipse.lsp4j.TextEdit>> changes = root.getChanges();
      if (changes.containsKey(uri)) {
        changes.get(uri).addAll(converter.convert());
      } else {
        changes.put(uri, converter.convert());
      }
    }
  }

  private static ICompilationUnit getNewCompilationUnit(IType type, String newName) {
    ICompilationUnit cu = type.getCompilationUnit();
    if (isPrimaryType(type)) {
      IPackageFragment parent = type.getPackageFragment();
      String renamedCUName = JavaModelUtil.getRenamedCUName(cu, newName);
      return parent.getCompilationUnit(renamedCUName);
    } else {
      return cu;
    }
  }

  private static boolean isPrimaryType(IType type) {
    String cuName = type.getCompilationUnit().getElementName();
    String typeName = type.getElementName();
    return type.getDeclaringType() == null
        && JavaCore.removeJavaLikeExtension(cuName).equals(typeName);
  }

  private static void updatePackageStatement(
      CompilationUnit astCU,
      String newName,
      String oldName,
      ASTRewrite rewriter,
      ICompilationUnit cu)
      throws JavaModelException {
    boolean defaultPackage = newName.isEmpty();
    AST ast = astCU.getAST();
    if (defaultPackage) {
      // remove existing package statement
      PackageDeclaration pkg = astCU.getPackage();
      if (pkg != null) {
        int pkgStart;
        Javadoc javadoc = pkg.getJavadoc();
        if (javadoc != null) {
          pkgStart = javadoc.getStartPosition() + javadoc.getLength() + 1;
        } else {
          pkgStart = pkg.getStartPosition();
        }
        int extendedStart = astCU.getExtendedStartPosition(pkg);
        if (pkgStart != extendedStart) {
          String commentSource = cu.getSource().substring(extendedStart, pkgStart);
          ASTNode comment =
              rewriter.createStringPlaceholder(commentSource, ASTNode.PACKAGE_DECLARATION);
          rewriter.set(astCU, CompilationUnit.PACKAGE_PROPERTY, comment, null);
        } else {
          rewriter.set(astCU, CompilationUnit.PACKAGE_PROPERTY, null, null);
        }
      }
    } else {
      org.eclipse.jdt.core.dom.PackageDeclaration pkg = astCU.getPackage();
      if (pkg != null) {
        // rename package statement
        String newPackageFragmentName =
            pkg.getName().getFullyQualifiedName().replaceFirst(oldName, newName);
        Name name = ast.newName(newPackageFragmentName);
        rewriter.set(pkg, PackageDeclaration.NAME_PROPERTY, name, null);
      } else {
        // create new package statement
        pkg = ast.newPackageDeclaration();
        pkg.setName(ast.newName(newName));
        rewriter.set(astCU, CompilationUnit.PACKAGE_PROPERTY, pkg, null);
      }
    }
  }
}
