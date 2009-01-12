/*
 * Created on Sep 21, 2006
 * @author Fabio
 */
package com.python.pydev.analysis;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.python.pydev.core.REF;
import org.python.pydev.core.TestDependent;
import org.python.pydev.editor.TestIndentPrefs;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;

import com.python.pydev.analysis.messages.IMessage;

public class OccurrencesAnalyzer2Test extends AnalysisTestsBase {
    
    public static void main(String[] args) {
        try {
            OccurrencesAnalyzer2Test analyzer2 = new OccurrencesAnalyzer2Test();
            analyzer2.setUp();
            analyzer2.testNoStaticComplain();
            analyzer2.tearDown();
            System.out.println("finished");
            
            junit.textui.TestRunner.run(OccurrencesAnalyzer2Test.class);
            System.out.println("finished all");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void testErrorNotShownOnDynamicClass() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.getWithAttr.whatever\n"
        );
        checkNoError();

    }
    
    public void testErrorNotShownOnDynamicClass2() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.getWithAttr.whatever.other\n"
        );
        checkNoError();
        
    }
    
    public void testErrorNotShownOnDynamicClass3() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.childGetWithAttr.whatever\n"+
                "print importer.childGetWithAttr.whatever.other\n"
        );
        checkNoError();
        
    }
    
    
    public void testErrorNotShownOnClassFromMethod() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.logger.debug('10')\n"
        );
        checkNoError();
        
    }
    
    public void testErrorNotShownOnNoneClass() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.initialNone.foo\n"
        );
        checkNoError();
        
    }
    
    public void testErrorNotShownOnDynamicClass4() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.globals_struct.bar\n"
        );
        checkNoError();
        
    }
    
    public void testErrorNotShownOnDynamicClass5() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.Struct.bar\n"
        );
        checkNoError();
    }
    
    public void testErrorNotShownOnDynamicClass6() {
        doc = new Document(
                "from extendable.noerr.importer import WithGetAttr2\n"+
                "print WithGetAttr2.anything\n"
        );
        checkNoError();
    }
    
    public void testErrorNotShownOnDynamicClass7() {
        doc = new Document(
                "from extendable.noerr.importer import Struct\n"+
                "print Struct.anything\n"
        );
        checkNoError();
    }
    
    public void testErrorNotShownOnDynamicClass8() {
        doc = new Document(
                "from extendable.noerr.importer import StructSub\n"+
                "print StructSub.anything\n"
        );
        checkNoError();
    }
    
    public void testErrorShownOnInitialSetClass() {
        doc = new Document(
                "from extendable.noerr import importer\n"+
                "print importer.initialSet.m1\n"+
                "print importer.initialSet.m2\n"//has error
        );
        IMessage[] messages = checkError(1);
        assertEquals("Undefined variable from import: m2", messages[0].getMessage());
    }

    public void testNoErrorPathInPackage() {
        doc = new Document(
                "import extendable\n"+
                "print extendable.__path__\n"
        );
        checkNoError();
    }
    
    public void testErrorPathNotInModule() {
        doc = new Document(
                "from extendable import static\n"+
                "print static.__path__\n"
        );
        IMessage[] messages = checkError(1);
        assertEquals("Undefined variable from import: __path__", messages[0].getMessage());
    }
    
    public void testErrorPathNotInModule2() {
        doc = new Document(
                "from extendable import * #@UnusedWildImport\n"+
                "__path__\n"
        );
        
        //__path__ does not come on "import *" 
        IMessage[] messages = checkError(1);
        assertEquals("Undefined variable: __path__", messages[0].getMessage());
    }
    
    public void testNoEffectInException() {
        doc = new Document(
                "def raise_exception():\n"+
                "    x = None\n"+
                "    raise Exception, '%(number)s' % {\n"+
                "        'number': x is None,\n"+
                "    }\n"
        );
        checkNoError();
    }
    
    public void testNoEffectInGenExp() {
        doc = new Document(
                "for val in (3 in (1, 2), 'something else'):\n"+
                "    print 'val was', val\n"
        );
        checkNoError();
    }
    
    public void testPathFound() throws IOException{
        
        analyzer = new OccurrencesAnalyzer();
        File file = new File(TestDependent.TEST_PYSRC_LOC+"extendable/with_path.py");
        Document doc = new Document(REF.getFileContents(file));
        msgs = analyzer.analyzeDocument(nature, (SourceModule) AbstractModule.createModule("extendable.with_path", file, nature, 0), 
                prefs, doc, new NullProgressMonitor(), new TestIndentPrefs(true, 4));
        
        printMessages(msgs, 0);
    }
    
    public void testPathFound2() throws IOException{
        
        analyzer = new OccurrencesAnalyzer();
        File file = new File(TestDependent.TEST_PYSRC_LOC+"extendable/__init__.py");
        Document doc = new Document(REF.getFileContents(file));
        msgs = analyzer.analyzeDocument(nature, (SourceModule) AbstractModule.createModule("extendable.__init__", file, nature, 0), 
                prefs, doc, new NullProgressMonitor(), new TestIndentPrefs(true, 4));
        
        printMessages(msgs, 0);
    }
    
    public void testInitDef() throws IOException{
        doc = new Document(
            "from extendable import help\n"+
            "print help.about\n"
        );
        checkNoError();

    }
    
    public void testNoStaticComplain() throws IOException{
        doc = new Document(
                "class SomeClass(object):\n" +
                "    @staticmethod\n" +
                "    def m1(cls):\n"+
                "        pass\n"
        );
        checkNoError();
    }
    
    public void testNoStaticComplain2() throws IOException{
        doc = new Document(
                "class SomeClass(object):\n" +
                "    @staticmethod\n" +
                "    def m1():\n"+
                "        pass\n"
        );
        checkNoError();
    }
    
    public void testNoStaticComplain3() throws IOException{
        doc = new Document(
                "class SomeClass(object):\n" +
                "    @staticmethod\n" +
                "    def m1(self):\n"+
                "        pass\n"
        );
        checkNoError();
    }
    
}
