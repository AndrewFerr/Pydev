/*
 * Created on 08/09/2005
 */
package com.python.pydev.codecompletion.parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant;
import org.python.pydev.editor.codecompletion.PyCodeCompletion;
import org.python.pydev.editor.codecompletion.revisited.modules.CompiledModule;

import com.python.pydev.analysis.additionalinfo.AbstractAdditionalInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.AdditionalInfoTestsBase;
import com.python.pydev.analysis.additionalinfo.AdditionalProjectInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.IInfo;
import com.python.pydev.codecompletion.ctxinsensitive.CtxParticipant;

public class ParameterCompletionTest extends AdditionalInfoTestsBase {

    public static void main(String[] args) {

        try {
            // DEBUG_TESTS_BASE = true;
            ParameterCompletionTest test = new ParameterCompletionTest();
            test.setUp();
            test.testCompletion2();
            test.tearDown();
            System.out.println("Finished");

            junit.textui.TestRunner.run(ParameterCompletionTest.class);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CompiledModule.COMPILED_MODULES_ENABLED = false;

        useOriginalRequestCompl = true;
        participant = new CtxParticipant();
        
        ExtensionHelper.testingParticipants = new HashMap<String, List>();
        ArrayList<IPyDevCompletionParticipant> participants = new ArrayList<IPyDevCompletionParticipant>();
        participants.add(participant);
        ExtensionHelper.testingParticipants.put(ExtensionHelper.PYDEV_COMPLETION, participants);
        
        codeCompletion = new PyCodeCompletion();
        this.restorePythonPath(false);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        useOriginalRequestCompl = false;
        ExtensionHelper.testingParticipants = null;
    }
    

    // ------------------------------------------------------------------------------------------------- tests
    
    public void testSetup() {
        AbstractAdditionalInterpreterInfo additionalInfo = AdditionalProjectInterpreterInfo.getAdditionalInfoForProject(nature.getProject());
        assertTrue(additionalInfo.getAllTokens().size() > 0);
        List<IInfo> tokensStartingWith = additionalInfo.getTokensStartingWith("existingM", AbstractAdditionalInterpreterInfo.INNER);
        assertTrue(tokensStartingWith.size() == 1);
        assertIsIn("existingMethod", "testAssist.assist", tokensStartingWith);
    }

    
    public void testCompletion() throws CoreException, BadLocationException {
        String s = "" +
                "def m1(a):\n" +
                "    a.existingM";
        requestCompl(s, -1, -1, new String[]{"existingMethod()"}); //at least 3 chars needed by default
    }
    
    public void testCompletion2() throws CoreException, BadLocationException {
        String s = "" +
        "def m1(a):\n" +
        "    a.another()\n" +
        "    a.assertE";
        requestCompl(s, -1, -1, new String[]{"assertEquals"}); //at least 3 chars needed by default
    }

    // ----------------------------------------------------------------------------------------------- asserts
    
    
    private void assertIsIn(String tok, String mod, List<IInfo> tokensStartingWith) {
        for (IInfo info : tokensStartingWith) {
            if(info.getName().equals(tok)){
                if(info.getDeclaringModuleName().equals(mod)){
                    return;
                }
            }
        }
        fail("The tok "+tok+" was not found for the module "+mod);
    }

}
