package com.python.pydev.refactoring.tdd;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.ui.filetypes.FileTypesPreferencesPage;
import org.python.pydev.ui.wizards.files.AbstractPythonWizard;
import org.python.pydev.ui.wizards.files.PythonAbstractPathPage;


/**
 * Python token wizard (to create class, method
 */
public class PythonElementWizard extends AbstractPythonWizard {

    private final static class PythonElementPathPage extends PythonAbstractPathPage {
        private String validatedElement;
        private Text textElement;
        private Combo comboSelectElement;
        private Label labelModule;

        private PythonElementPathPage(String pageName, IStructuredSelection selection) {
            super(pageName, selection);
        }

        @Override
        protected boolean shouldCreatePackageSelect() {
            return false;
        }

        @Override
        protected boolean shouldCreateTemplates() {
            return false;
        }

        @Override
        protected void createNameSelect(Composite topLevel, boolean setFocus) {
            setFocus = false; //the focus will always appear in the element.
            super.createNameSelect(topLevel, setFocus);
            createElementSelect(topLevel);
        }

        private void createElementSelect(Composite topLevel) {
            //Make sure that the user understands that the name is the module name and the element
            //is the class/method.
            final Button button = new Button(topLevel, SWT.CHECK);
            button.setText("Element");
            GridData data = new GridData();
            data.grabExcessHorizontalSpace = false;
            button.setLayoutData(data);
            
            textElement = new Text(topLevel, SWT.BORDER);
            textElement.addKeyListener(this); //call validatePage on text changes.
            
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String textEl = textElement.getText();
                    if(button.getSelection()){
                        textElement.setEnabled(true);
                        
                        
                    }else{
                        if(textEl.length() > 0){
                            String textModule = textName.getText();
                            if(textModule.length() > 0){
                                textPackage.setText(textPackage.getText()+"."+textModule);
                            }
                            textName.setText(textEl);
                            textElement.setText("");
                        }
                        textElement.setEnabled(false);
                    }
                    validatePage();
                }
            });
            
            comboSelectElement = new Combo(topLevel, SWT.READ_ONLY|SWT.SINGLE|SWT.BORDER);
            comboSelectElement.setItems(new String[]{"Class", "Method", "Module"});
        }
        

        @Override
        protected boolean checkAdditionalErrors() {
            if(checkError(checkValidElement(textElement.getText()))){
                return true;
            }

            return false;
        }

        private String checkValidElement(String text) {
            validatedElement = null;
            String error = checkNameText(text);
            if(error != null){
                return error;
            }
            validatedElement = text;
            return null;
        }
        
        public String getValidatedElement() {
            return validatedElement;
        }

        @Override
        protected Label createNameLabel(Composite topLevel) {
            labelModule = super.createNameLabel(topLevel);
            labelModule.setText("Module:");
            return labelModule;
        }
    }


    public PythonElementWizard() {
        super("Create a new Python element");
    }

    public static final String WIZARD_ID = "org.python.pydev.ui.wizards.files.PythonModuleWizard";

    @Override
    protected PythonAbstractPathPage createPathPage() {
        return new PythonElementPathPage(this.description, selection);
    }

    /**
     * We will create a new module (file) here given the source folder and the package specified (which
     * are currently validated in the page) 
     * @param monitor 
     * @throws CoreException 
     */
    @Override
    protected IFile doCreateNew(IProgressMonitor monitor) throws CoreException {
        IContainer validatedSourceFolder = filePage.getValidatedSourceFolder();
        if(validatedSourceFolder == null){
            return null;
        }
        IContainer validatedPackage = filePage.getValidatedPackage();
        if(validatedPackage == null){
            return null;
        }
        String validatedName = filePage.getValidatedName()+FileTypesPreferencesPage.getDefaultDottedPythonExtension();
        
        IFile file = validatedPackage.getFile(new Path(validatedName));
        if(!file.exists()){
            file.create(new ByteArrayInputStream(new byte[0]), true, monitor);
        }
        
        return file;
    }

    
    /**
     * Applies the template if one was specified.
     */
    @Override
    protected void afterEditorCreated(IEditorPart openEditor) {
        if(!(openEditor instanceof PyEdit)){
            return; //only works for PyEdit...
        }
        
        PythonElementPathPage f = (PythonElementPathPage) filePage;
        System.out.println(f.getValidatedElement());
        
//        TemplatePersistenceData selectedTemplate = filePage.getSelectedTemplate();
//        if(selectedTemplate == null){
//            return; //no template selected, nothing to apply!
//        }
//        
//        Template template = selectedTemplate.getTemplate();
//
//        PyEdit pyEdit = (PyEdit) openEditor;
//        Region region = new Region(0, 0);
//        PyDocumentTemplateContext context = PyTemplateCompletionProcessor.createContext(new PyContextType(), 
//                pyEdit.getPySourceViewer(), region);
//        
//        TemplateProposal templateProposal = new TemplateProposal(template, context, region, null);
//        templateProposal.apply(pyEdit.getPySourceViewer(), '\n', 0, 0);
    }



}
