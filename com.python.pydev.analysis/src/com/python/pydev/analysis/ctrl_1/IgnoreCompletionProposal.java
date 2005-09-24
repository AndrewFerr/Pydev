/*
 * Created on 22/09/2005
 */
package com.python.pydev.analysis.ctrl_1;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.PyCompletionProposal;
import org.python.pydev.parser.PyParser;
import org.python.pydev.plugin.PydevPlugin;

public class IgnoreCompletionProposal extends PyCompletionProposal {

    private PyEdit edit;

    public IgnoreCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo, int priority, PyEdit edit) {
        super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString, contextInformation, additionalProposalInfo, priority);
        this.edit = edit;
        
    }
    
    @Override
    public void apply(IDocument document) {
        try {
            //first do the completion
            document.replace(fReplacementOffset, fReplacementLength, fReplacementString);

            //ok, after doing it, let's call for a reparse
            edit.getParser().parseNow(true);
        } catch (BadLocationException x) {
            PydevPlugin.log(x);
        }
    }
    
    @Override
    public Point getSelection(IDocument document) {
        return new Point(fCursorPosition, 0); //don't move the cursor
    }

}
