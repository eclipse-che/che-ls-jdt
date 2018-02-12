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
package org.eclipse.che.jdt.ls.extension.core.internal.navigation;

import static org.eclipse.che.jdt.ls.extension.core.internal.Utils.ensureNotCancelled;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.jdt.ls.extension.core.internal.GsonUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.jdt.ls.core.internal.hover.JavaElementLabels;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Command to find implementers of a type or a method
 *
 * @author dbocharo
 */
public class FindImplementersHandler {
  private static final Gson gson = GsonUtils.getInstance();

  /**
   * Finds implementers for an element defined by position
   *
   * @param parameters first parameter must be of type {@link TextDocumentPositionParams}, which
   *     defines a position of an element which search implementers for
   * @param pm a progress monitor
   * @return an object of type {@link ImplementersResponse}, which contains description of a
   *     searched element and implementers
   */
  @SuppressWarnings("restriction")
  public static ImplementersResponse getImplementers(List<Object> parameters, IProgressMonitor pm) {
    TextDocumentPositionParams param =
        gson.fromJson(gson.toJson(parameters.get(0)), TextDocumentPositionParams.class);

    ITypeRoot typeRoot = JDTUtils.resolveTypeRoot(param.getTextDocument().getUri());

    ImplementersResponse implementersResponse = new ImplementersResponse();
    List<SymbolInformation> implementers = new ArrayList<>();

    ensureNotCancelled(pm);

    try {
      IJavaElement elementToSearch =
          JDTUtils.findElementAtSelection(
              typeRoot,
              param.getPosition().getLine(),
              param.getPosition().getCharacter(),
              JavaLanguageServerPlugin.getPreferencesManager(),
              pm);
      if (elementToSearch != null) {
        implementersResponse.setSearchedElement(elementToSearch.getElementName());
        if (IJavaElement.TYPE == elementToSearch.getElementType()) {
          findSubTypes(elementToSearch, implementers, pm);
        } else if (IJavaElement.METHOD == elementToSearch.getElementType()) {
          findTypesWithSubMethods(elementToSearch, implementers, pm);
        }
      }
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
    implementersResponse.setImplementers(implementers);
    return implementersResponse;
  }

  private static void findSubTypes(
      IJavaElement element, List<SymbolInformation> implementers, IProgressMonitor pm)
      throws JavaModelException {
    IType type = (IType) element;
    ITypeHierarchy typeHierarchy = type.newTypeHierarchy(pm);
    IType[] implTypes = typeHierarchy.getAllSubtypes(type);

    for (IType implType : implTypes) {
      SymbolInformation dto = convertToSymbolInformation(implType);
      implementers.add(dto);
    }
  }

  @SuppressWarnings("restriction")
  private static void findTypesWithSubMethods(
      IJavaElement element, List<SymbolInformation> implementers, IProgressMonitor pm)
      throws JavaModelException {
    IMethod selectedMethod = (IMethod) element;
    IType parentType = selectedMethod.getDeclaringType();
    if (parentType == null) {
      return;
    }
    ITypeHierarchy typeHierarchy = parentType.newTypeHierarchy(pm);
    IType[] subTypes = typeHierarchy.getAllSubtypes(parentType);

    MethodOverrideTester methodOverrideTester = new MethodOverrideTester(parentType, typeHierarchy);

    for (IType type : subTypes) {
      IMethod method = methodOverrideTester.findOverridingMethodInType(type, selectedMethod);
      if (method == null) {
        continue;
      }
      SymbolInformation openDeclaration = convertToSymbolInformation(method);
      implementers.add(openDeclaration);
    }
  }

  @SuppressWarnings("restriction")
  private static SymbolInformation convertToSymbolInformation(IJavaElement javaElement)
      throws JavaModelException {
    SymbolInformation symbolInformation = new SymbolInformation();
    symbolInformation.setKind(DocumentSymbolHandler.mapKind(javaElement));
    symbolInformation.setName(
        JavaElementLabels.getElementLabel(javaElement, JavaElementLabels.ALL_DEFAULT));
    Location location = JDTUtils.toLocation(javaElement);
    location.setUri(ResourceUtils.toClientUri(location.getUri()));
    symbolInformation.setLocation(location);
    return symbolInformation;
  }
}
