package org.python.pydev.debug.newconsole.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.NotConfiguredInterpreterException;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;

/**
 * Helper to choose which kind of jython run will it be.
 */
final class ChooseProcessTypeDialog extends Dialog {
    
    private Button checkboxForCurrentEditor;

    private Button checkboxPython;
    
    private Button checkboxJython;

    private PyEdit activeEditor;

    private IInterpreterManager interpreterManager;
    
    private List<IPythonNature> natures = new ArrayList<IPythonNature>();

    ChooseProcessTypeDialog(Shell shell, PyEdit activeEditor) {
        super(shell);
        this.activeEditor = activeEditor;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        checkboxForCurrentEditor = new Button(area, SWT.RADIO);
        checkboxForCurrentEditor.setToolTipText("Creates a console with the PYTHONPATH used by the current editor (and jython/python depending on the project type).");
        configureEditorButton();

        
        checkboxPython = new Button(area, SWT.RADIO);
        checkboxPython.setToolTipText("Creates a Python console with the PYTHONPATH containing all the python projects in the workspace.");
        configureButton(checkboxPython, "Python", PydevPlugin.getPythonInterpreterManager());
        
        checkboxJython = new Button(area, SWT.RADIO);
        checkboxJython.setToolTipText("Creates a Jython console with the PYTHONPATH containing all the python projects in the workspace.");
        configureButton(checkboxJython, "Jython", PydevPlugin.getJythonInterpreterManager());
        
        return area;
    }
    
    /**
     * Configures a button related to a given interpreter manager.
     */
    private void configureButton(Button checkBox, String python, IInterpreterManager interpreterManager) {
        boolean enabled = false;
        String text;
        try{
            if(interpreterManager.getDefaultInterpreter() != null){
                text = python+" console";
                enabled = true;
            }else{
                throw new NotConfiguredInterpreterException();
            }
        }catch(MisconfigurationException e){
            text = "Unable to create console for "+python+" (interpreter not configured)";
        }
        checkBox.setText(text);
        checkBox.setEnabled(enabled);
    }

    /**
     * Configures a button related to an editor.
     * @throws MisconfigurationException 
     */
    private void configureEditorButton(){
        boolean enabled = false;
        String text;
        try{
            if(this.activeEditor != null){
                IPythonNature nature = this.activeEditor.getPythonNature();
                if(nature != null){
                    
                    if(nature.getRelatedInterpreterManager().getDefaultInterpreter() != null){
                        text = "Console for currently active editor";
                        enabled = true;
                    }else{
                        throw new NotConfiguredInterpreterException();
                    }
                }else{
                    text = "No python nature configured for the current editor";
                }
            }else{
                text = "Unable to create console for current editor (no active editor)";
            }
        }catch(MisconfigurationException e){
            //expected
            text = "Unable to create console for current editor (interpreter not configured for the editor)";
        }
        checkboxForCurrentEditor.setText(text);
        checkboxForCurrentEditor.setEnabled(enabled);
    }

    
    /**
     * Sets the internal pythonpath chosen.
     */
    @Override
    protected void okPressed() {
        if(checkboxForCurrentEditor.isEnabled() && checkboxForCurrentEditor.getSelection()){
            IProject project = this.activeEditor.getProject();
            PythonNature nature = PythonNature.getPythonNature(project);
            natures.add(nature);
            IInterpreterManager relatedInterpreterManager = nature.getRelatedInterpreterManager();
            this.interpreterManager = relatedInterpreterManager;
            
        }else if(checkboxPython.isEnabled() && checkboxPython.getSelection()){
            this.interpreterManager = PydevPlugin.getPythonInterpreterManager();
            
        }else if(checkboxJython.isEnabled() && checkboxJython.getSelection()){
            this.interpreterManager = PydevPlugin.getJythonInterpreterManager();
            
        }
        
        super.okPressed();
    }

    
    /**
     * @return the pythonpath to be used or null if not configured.
     */
    public Collection<String> getPythonpath(IInterpreterInfo interpreter) {
        
        if(this.interpreterManager != null){
            if(this.natures.size() == 1){
                //chosen for the editor
                IPythonNature nature = this.natures.get(0);
                return new ArrayList<String>(nature.getPythonPathNature().getCompleteProjectPythonPath(
                        interpreter, this.interpreterManager));

            }
            
            //we need to get the natures matching the one selected in all the projects.
            IWorkspace w = ResourcesPlugin.getWorkspace();
            HashSet<String> pythonpath = new HashSet<String>();
            for(IProject p:w.getRoot().getProjects()){
                PythonNature nature = PythonNature.getPythonNature(p);
                try{
                    if(nature != null){
                        if(nature.getRelatedInterpreterManager() == this.interpreterManager){
                            natures.add(nature);
                            pythonpath.addAll(nature.getPythonPathNature().
                                    getCompleteProjectPythonPath(interpreter, this.interpreterManager));
                        }
                    }
                }catch(Exception e){
                    PydevPlugin.log(e);
                }
            }
            return pythonpath;
        }
        
        return null;
    }

    public IInterpreterManager getInterpreterManager() {
        return this.interpreterManager;
    }
    
    public List<IPythonNature> getNatures() {
        return natures;
    }
}