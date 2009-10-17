/* 
 * Copyright (C) 2006, 2007  Dennis Hunziker, Ueli Kistler
 * Copyright (C) 2007  Reto Schuettel, Robin Stocker
 */

package org.python.pydev.refactoring.tests.core;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.python.pydev.core.IModulesManager;
import org.python.pydev.core.ISystemModulesManager;
import org.python.pydev.core.REF;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.codecompletion.revisited.CodeCompletionTestsBase;
import org.python.pydev.refactoring.ast.PythonModuleManager;
import org.python.pydev.refactoring.ast.adapters.ModuleAdapter;
import org.python.pydev.refactoring.ast.visitors.VisitorFactory;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;

public abstract class AbstractIOTestCase extends TestCase implements IInputOutputTestCase {
	private String generated;
	protected TestData data;
	protected CodeCompletionTestsBase codeCompletionTestsBase = new CodeCompletionTestsBase();

	protected ModuleAdapter createModuleAdapterFromDataSource() throws Throwable {
	    return createModuleAdapterFromDataSource(null);
	}
	
	/**
	 * @param version IPythonNature.PYTHON_VERSION_XXX
	 */
    protected ModuleAdapter createModuleAdapterFromDataSource(String version) throws Throwable {
        codeCompletionTestsBase.restorePythonPath(
                REF.getFileAbsolutePath(data.file.getParentFile()), 
                true);
        PythonModuleManager pythonModuleManager = new PythonModuleManager(CodeCompletionTestsBase.nature);
        if(version != null){
            //As the files will be found in the system, we need to set the system modules manager info.
            IModulesManager modulesManager = pythonModuleManager.getIModuleManager();
            ISystemModulesManager systemModulesManager = modulesManager.getSystemModulesManager();
            systemModulesManager.setInfo(new InterpreterInfo(version, "", new ArrayList<String>()));
            
            CodeCompletionTestsBase.nature.setVersion(version, null);
        }
        ModuleAdapter module = VisitorFactory.createModuleAdapter(
                pythonModuleManager,
                data.file, new Document(data.source), CodeCompletionTestsBase.nature);
        return module;
    }
    


	public AbstractIOTestCase(String name) {
		this(name, false);
	}

	public AbstractIOTestCase(String name, boolean ignoreEmptyLines) {
		super(name);
	}
	

    protected void assertContentsEqual(String expected, String generated) {
        assertEquals(StringUtils.replaceNewLines(expected, "\n"), StringUtils.replaceNewLines(generated, "\n"));
    }
	
	@Override
	protected void setUp() throws Exception {
		PythonModuleManager.setTesting(true);
        codeCompletionTestsBase.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		PythonModuleManager.setTesting(false);
		codeCompletionTestsBase.tearDown();
	}
	
	protected String getGenerated() {
		return generated.trim();
	}

	public void setTestGenerated(String source) {
		this.generated = source;
	}
	
	public void setData(TestData data) {
		this.data = data;
	}
	
	public String getExpected() {
		return data.result;
	}
}
