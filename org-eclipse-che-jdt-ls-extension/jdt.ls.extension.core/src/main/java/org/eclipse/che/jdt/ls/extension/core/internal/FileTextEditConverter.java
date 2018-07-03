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
import java.util.List;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

/**
 * Converts an {@link org.eclipse.text.edits.TextEdit} to {@link org.eclipse.lsp4j.TextEdit} for
 * {@link IDocument}.
 *
 * @author Valeriy Svydenko
 */
public class FileTextEditConverter extends TextEditVisitor {

  private TextEdit source;
  private IDocument document;
  private List<org.eclipse.lsp4j.TextEdit> converted;

  private FileTextEditConverter(IDocument document, TextEdit edit) {
    this.source = edit;
    this.converted = new ArrayList<>();
    if (document == null) {
      throw new IllegalArgumentException("Document can not be null");
    }
    this.document = document;
  }

  public static List<org.eclipse.lsp4j.TextEdit> convert(IDocument document, TextEdit edit) {
    FileTextEditConverter converter = new FileTextEditConverter(document, edit);
    return converter.convert();
  }

  private List<org.eclipse.lsp4j.TextEdit> convert() {
    if (this.source != null) {
      this.source.accept(this);
    }
    return converted;
  }

  /* (non-Javadoc)
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.InsertEdit)
   */
  @Override
  public boolean visit(InsertEdit edit) {
    setNewText(edit.getText(), edit.getOffset(), edit.getLength());
    return super.visit(edit);
  }

  /* (non-Javadoc)
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.DeleteEdit)
   */
  @Override
  public boolean visit(DeleteEdit edit) {
    setNewText("", edit.getOffset(), edit.getLength());
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.ReplaceEdit)
   */
  @Override
  public boolean visit(ReplaceEdit edit) {
    setNewText(edit.getText(), edit.getOffset(), edit.getLength());
    return super.visit(edit);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.MoveSourceEdit)
   */
  @Override
  public boolean visit(MoveSourceEdit edit) {
    if (edit.getParent() != null
        && edit.getTargetEdit() != null
        && edit.getParent().equals(edit.getTargetEdit().getParent())) {
      setNewText("", edit.getOffset(), edit.getLength());
      return false;
    }
    return super.visit(edit);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
   * MoveTargetEdit)
   */
  @Override
  public boolean visit(MoveTargetEdit edit) {
    return updateRegion(edit, edit.getSourceEdit().getOffset(), edit.getSourceEdit().getLength());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
   * CopyTargetEdit)
   */
  @Override
  public boolean visit(CopyTargetEdit edit) {
    return updateRegion(edit, edit.getSourceEdit().getOffset(), edit.getSourceEdit().getLength());
  }

  private boolean updateRegion(TextEdit edit, int offset, int length) {
    try {
      org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
      te.setRange(toRange(document, edit.getOffset(), edit.getLength()));

      edit.apply(document, TextEdit.UPDATE_REGIONS);
      String content = document.get(offset, length);
      te.setNewText(content);
      converted.add(te);
    } catch (MalformedTreeException | BadLocationException e) {
      JavaLanguageServerPlugin.logException("Error converting TextEdits", e);
    }
    return false; // do not visit children
  }

  private void setNewText(String text, int offset, int length) {
    org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
    te.setNewText(text);
    te.setRange(toRange(document, offset, length));
    converted.add(te);
  }

  private static Range toRange(IDocument document, int offset, int length) {
    Range range = newRange();
    if (offset > 0 || length > 0) {
      int[] loc = null;
      int[] endLoc = null;
      if (document != null) {
        loc = toLine(document, offset);
        endLoc = toLine(document, offset + length);
      }
      if (loc == null) {
        loc = new int[2];
      }
      if (endLoc == null) {
        endLoc = new int[2];
      }
      setPosition(range.getStart(), loc);
      setPosition(range.getEnd(), endLoc);
    }
    return range;
  }

  private static int[] toLine(IDocument document, int offset) {
    try {
      int line = document.getLineOfOffset(offset);
      int column = offset - document.getLineOffset(line);
      return new int[] {line, column};
    } catch (BadLocationException e) {
      JavaLanguageServerPlugin.logException(e.getMessage(), e);
    }
    return null;
  }

  private static Range newRange() {
    return new Range(new Position(), new Position());
  }

  private static void setPosition(Position position, int[] coords) {
    assert coords.length == 2;
    position.setLine(coords[0]);
    position.setCharacter(coords[1]);
  }
}
