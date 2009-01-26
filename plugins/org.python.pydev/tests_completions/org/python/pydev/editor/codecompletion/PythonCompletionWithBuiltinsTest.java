/*
 * Created on 13/08/2005
 */
package org.python.pydev.editor.codecompletion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.REF;
import org.python.pydev.core.TestDependent;
import org.python.pydev.core.structure.CompletionRecursionException;
import org.python.pydev.editor.codecompletion.revisited.CodeCompletionTestsBase;
import org.python.pydev.editor.codecompletion.revisited.CompletionCache;
import org.python.pydev.editor.codecompletion.revisited.CompletionStateFactory;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.CompiledModule;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;
import org.python.pydev.editor.codecompletion.shell.AbstractShell;
import org.python.pydev.editor.codecompletion.shell.PythonShell;
import org.python.pydev.editor.codecompletion.shell.PythonShellTest;
import org.python.pydev.plugin.nature.PythonNature;

public class PythonCompletionWithBuiltinsTest extends CodeCompletionTestsBase{
    
    
    protected boolean isInTestFindDefinition = false;
    
    public static void main(String[] args) {
        try {
            PythonCompletionWithBuiltinsTest builtins = new PythonCompletionWithBuiltinsTest();
            builtins.setUp();
            builtins.testFindDefinition();
            builtins.tearDown();
            
            junit.textui.TestRunner.run(PythonCompletionWithBuiltinsTest.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    protected PythonNature createNature() {
        return new PythonNature(){
            @Override
            public boolean isJython() throws CoreException {
                return false;
            }
            @Override
            public boolean isPython() throws CoreException {
                return true;
            }
            @Override
            public int getGrammarVersion() {
                return IPythonNature.LATEST_GRAMMAR_VERSION;
            }
            
            @Override
            public String resolveModule(File file) {
                if(isInTestFindDefinition){
                    return null;
                }
                return super.resolveModule(file);
            }
        };
    }

    private static PythonShell shell;
    
    /*
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
        
        ADD_MX_TO_FORCED_BUILTINS = false;

        CompiledModule.COMPILED_MODULES_ENABLED = true;
        this.restorePythonPath(TestDependent.GetCompletePythonLib(true)+"|"+
                TestDependent.PYTHON_WXPYTHON_PACKAGES+"|"+
                TestDependent.PYTHON_MX_PACKAGES+"|"+
                TestDependent.PYTHON_NUMPY_PACKAGES, false);
        
        codeCompletion = new PyCodeCompletion();

        //we don't want to start it more than once
        if(shell == null){
            shell = PythonShellTest.startShell();
        }
        AbstractShell.putServerShell(nature, AbstractShell.COMPLETION_SHELL, shell);
    
    }

    /*
     * @see TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        CompiledModule.COMPILED_MODULES_ENABLED = false;
        super.tearDown();
        AbstractShell.putServerShell(nature, AbstractShell.COMPLETION_SHELL, null);
    }
    
    public void testRecursion() throws FileNotFoundException, CoreException, BadLocationException, CompletionRecursionException{
        String file = TestDependent.TEST_PYSRC_LOC+"testrec3/rec.py";
        String strDoc = "RuntimeError.";
        File f = new File(file);
        try{
            nature.getAstManager().getCompletionsForToken(f, new Document(REF.getFileContents(f)), 
                    CompletionStateFactory.getEmptyCompletionState("RuntimeError", nature, new CompletionCache()));
        }catch(CompletionRecursionException e){
            //that's ok... we're asking for it here...
        }
        requestCompl(f, strDoc, strDoc.length(), -1, new String[]{"__doc__", "__getitem__()", "__init__()", "__str__()"});   
    }
    

    
    public void testCompleteImportBuiltin() throws BadLocationException, IOException, Exception{
        
        String s;
        
        s = "from datetime import datetime\n" +
        "datetime.";
        
        //for some reason, this is failing only when the module is specified...
        File file = new File(TestDependent.TEST_PYDEV_PLUGIN_LOC+"tests/pysrc/simpledatetimeimport.py");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        requestCompl(file, s, s.length(), -1, new String[]{"today()", "now()", "utcnow()"});

        
        
        s = "from datetime import datetime, date, MINYEAR,";
        requestCompl(s, s.length(), -1, new String[] { "date", "datetime", "MINYEAR", "MAXYEAR", "timedelta" });
        
        s = "from datetime.datetime import ";
        requestCompl(s, s.length(), -1, new String[] { "today", "now", "utcnow" });

    
    
        // Problem here is that we do not evaluate correctly if
        // met( ddd,
        //      fff,
        //      ccc )
        //so, for now the test just checks that we do not get in any sort of
        //look... 
        s = "" +
        
        "class bla(object):pass\n" +
        "\n"+
        "def newFunc(): \n"+
        "    callSomething( bla.__get#complete here... stack error \n"+
        "                  keepGoing) \n";

        //If we improve the parser to get the error above, uncomment line below to check it...
        //requestCompl(s, s.indexOf('#'), 1, new String[]{"__getattribute__"});
        requestCompl(s, s.indexOf('#'), 0, new String[]{});


        //check for builtins..1
        s = "" +
        "\n" +
        "";
        requestCompl(s, s.length(), -1, new String[]{"RuntimeError"});

        //check for builtins..2
        s = "" +
        "from testlib import *\n" +
        "\n" +
        "";
        requestCompl(s, s.length(), -1, new String[]{"RuntimeError"});

        //check for builtins..3 (builtins should not be available because it is an import request for completions)
        requestCompl("from testlib.unittest import  ", new String[]{"__file__", "__name__", "__init__", "__path__", "anothertest"
                , "AnotherTest", "GUITest", "guitestcase", "main", "relative", "t", "TestCase", "testcase", "TestCaseAlias", 
                });

    }

    
    
    public void testBuiltinsInNamespace() throws BadLocationException, IOException, Exception{
        String s = "__builtins__.";
        requestCompl(s, s.length(), -1, new String[]{"RuntimeError"});
    }
    
    public void testBuiltinsInNamespace2() throws BadLocationException, IOException, Exception{
        String s = "__builtins__.RuntimeError.";
        requestCompl(s, s.length(), -1, new String[]{"__doc__", "__getitem__()", "__init__()", "__str__()"});
    }
    
    public void testPreferForcedBuiltin() throws BadLocationException, IOException, Exception{
        if(TestDependent.HAS_MX_DATETIME){
            String s = ""+
            "from mx import DateTime\n"+
            "DateTime.";
            requestCompl(s, s.length(), -1, new String[]{"now()"});
        }
    }
    
    
    public void testNumpy() throws BadLocationException, IOException, Exception{
        if(TestDependent.HAS_NUMPY_INSTALLED){
            String s = ""+
            "from numpy import less\n"+
            "less.";
            requestCompl(new File(TestDependent.TEST_PYSRC_LOC+"extendable/not_existent.py"), 
                    s, s.length(), -1, new String[]{"types", "ntypes", "nout", "nargs", "nin"});
        }
    }
    
    public void testDeepNested6() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "from extendable.nested2 import hub\n"+
        "hub.c1.f.";
        requestCompl(s, s.length(), -1, new String[] { "curdir"});
    }
    
    public void testDeepNested10() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "from extendable.nested3 import hub2\n"+
        "hub2.c.a.";
        requestCompl(s, s.length(), -1, new String[] { "fun()"});
    }
    
    public void testRelativeOnSameProj() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "import prefersrc\n" +
        "prefersrc.";
        AbstractModule.MODULE_NAME_WHEN_FILE_IS_UNDEFINED = "foo";
        try {
            requestCompl(s, s.length(), -1, new String[] { "OkGotHere" }, nature2);
        } finally {
            AbstractModule.MODULE_NAME_WHEN_FILE_IS_UNDEFINED = "";
        }
    }
    
    public void testDeepNested7() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "from extendable.nested2 import hub\n"+
        "hub.c1.f.curdir.";
        requestCompl(s, s.length(), -1, new String[] { "upper()"});
    }
    
    public void testDeepNested8() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "from extendable.nested2 import hub\n"+
        "hub.C1.f.sep."; //changed: was altsep (may be None in linux).
        requestCompl(s, s.length(), -1, new String[] { "upper()"});
    }
    
    public void testDeepNested9() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "from extendable.nested2 import hub\n"+
        "hub.C1.f.inexistant.";
        requestCompl(s, s.length(), -1, new String[] { });
    }
    
    public void testDictAssign() throws CoreException, BadLocationException{
        String s;
        s = "" +
        "a = {}\n"+
        "a.";
        requestCompl(s, s.length(), -1, new String[] { "keys()" });
    }
    

    public void testPreferSrc() throws BadLocationException, IOException, Exception{
        String s = ""+
        "import prefersrc\n"+
        "prefersrc.";
        requestCompl(s, s.length(), -1, new String[]{"PreferSrc"});
    }
    
    public void testPreferCompiledOnBootstrap() throws BadLocationException, IOException, Exception{
        String s = ""+
        "from extendable.bootstrap_dll import umath\n"+
        "umath.";
        IModule module = nature.getAstManager().getModule("extendable.bootstrap_dll.umath", nature, true);
        assertTrue("Expected CompiledModule. Found: "+module.getClass(), module instanceof CompiledModule);
        requestCompl(s, s.length(), -1, new String[]{"less"});
    }
    
    public void testPreferCompiledOnBootstrap2() throws BadLocationException, IOException, Exception{
        String s = ""+
        "from extendable.bootstrap_dll.umath import ";
        IModule module = nature.getAstManager().getModule("extendable.bootstrap_dll.umath", nature, true);
        assertTrue(module instanceof CompiledModule);
        requestCompl(s, s.length(), -1, new String[]{"less"});
    }
    
    public void testWxPython1() throws BadLocationException, IOException, Exception{
        if(TestDependent.HAS_WXPYTHON_INSTALLED){ //we can only test what we have
            String s = ""+
            "from wxPython.wx import *\n"+
            "import wx\n"+
            "class HelloWorld(wx.App):\n"+
            "   def OnInit(self):\n"+
            "       frame = wx.Frame(None,-1,\"hello world\")\n"+
            "       frame.Show(True)\n"+
            "       self.SetTopWindow(frame)\n"+
            "       b=wx.Button(frame,-1,\"Button\")\n"+
            "       return True\n"+
            "app = HelloWorld(0)\n"+
            "app.MainLoop()\n"+
            "app.";
            requestCompl(s, s.length(), -1, new String[]{"MainLoop()"});
        }
    }
    

    public void testCompleteImportBuiltinReference2() throws BadLocationException, IOException, Exception{
        String s;
        if(TestDependent.HAS_WXPYTHON_INSTALLED){ //we can only test what we have
            s = "" +
            "from wx import ";
            requestCompl(s, s.length(), -1, new String[]{"glcanvas"});
        }
    }
    
    public void testGlu() throws IOException, CoreException, BadLocationException {
        if(TestDependent.HAS_GLU_INSTALLED){
            final String s = "from OpenGL import ";
            requestCompl(s, s.length(), -1, new String[]{"GLU", "GLUT"});
        }
    }
    
    public void testGlu2() throws IOException, CoreException, BadLocationException {
        if(TestDependent.HAS_GLU_INSTALLED){
            final String s = "from OpenGL.GL import ";
            requestCompl(s, s.length(), -1, new String[]{"glPushMatrix"});
        }
    }
    
    public void testCompleteImportBuiltinReference() throws BadLocationException, IOException, Exception{
        
        String s;

        if(TestDependent.HAS_WXPYTHON_INSTALLED){ //we can only test what we have
            s = "" +
            "from wxPython.wx import wxButton\n"+
            "                \n"+   
            "wxButton.";         
            requestCompl(s, s.length(), -1, new String[]{"Close()"});

            s = "" +
            "import wxPython\n"+
            "                \n"+   
            "wxPython.";         
            requestCompl(s, s.length(), -1, new String[]{"wx"});
        }

        s = "" +
        "import os\n"+
        "                \n"+   
        "os.";         
        File file = new File(TestDependent.TEST_PYDEV_PLUGIN_LOC+"tests/pysrc/simpleosimport.py");
        assertTrue(file.exists());
        assertTrue(file.isFile());
        requestCompl(file, s, s.length(), -1, new String[]{"path"});
        
        s = "" +
        "import os\n"+
        "                \n"+   
        "os.";         
        requestCompl(s, s.length(), -1, new String[]{"path"});
        
        if(TestDependent.HAS_QT_INSTALLED){ //we can only test what we have
            //check for builtins with reference..3
            s = "" +
            "from qt import *\n"+
            "                \n"+   
            "q = QLabel()    \n"+     
            "q.";         
            requestCompl(s, s.length(), -1, new String[]{"AlignAuto"});
        }

        //check for builtins with reference..3
        s = "" +
        "from testlib.unittest import anothertest\n"+
        "anothertest.";         
        requestCompl(s, s.length(), 4, new String[]{"__file__", "__name__", "AnotherTest","t"});

    }
    

    public void testInstanceCompletion() throws Exception {
        String s = 
            "class A:\n" +
            "    def __init__(self):\n" +
            "        self.list1 = []\n" +
            "if __name__ == '__main__':\n" +
            "    a = A()\n" +
            "    a.list1.";
        
        requestCompl(s, -1, new String[] {"pop()", "remove()"});
    }
    
    
    public void test__all__() throws Exception {
        String s = 
            "from extendable.all_check import *\n" +
            "";
        
        //should keep the variables from the __builtins__ in this module
        requestCompl(s, -1, new String[] {"ThisGoes", "RuntimeError"});
    }

    
    public void testFindDefinition() throws Exception {
        isInTestFindDefinition = true;
        try {
            CompiledModule mod = new CompiledModule("os", nature.getAstManager());
            Definition[] findDefinition = mod.findDefinition(
                    CompletionStateFactory.getEmptyCompletionState("walk", nature, new CompletionCache()), -1, -1, nature);
            assertEquals(1, findDefinition.length);
            assertEquals("os", findDefinition[0].module.getName());
        } finally {
            isInTestFindDefinition = false;
        }
    }
    
    
}
