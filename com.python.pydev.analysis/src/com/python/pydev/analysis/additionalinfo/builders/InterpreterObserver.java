/*
 * Created on 07/09/2005
 */
package com.python.pydev.analysis.additionalinfo.builders;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IModulesManager;
import org.python.pydev.core.ISystemModulesManager;
import org.python.pydev.core.ModulesKey;
import org.python.pydev.core.ModulesKeyForZip;
import org.python.pydev.core.REF;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.editor.codecompletion.revisited.PythonPathHelper;
import org.python.pydev.parser.ErrorDescription;
import org.python.pydev.parser.PyParser;
import org.python.pydev.parser.fastparser.FastDefinitionsParser;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;
import org.python.pydev.ui.NotConfiguredInterpreterException;
import org.python.pydev.ui.interpreters.IInterpreterObserver;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
import org.python.pydev.utils.JobProgressComunicator;

import com.python.pydev.analysis.additionalinfo.AbstractAdditionalDependencyInfo;
import com.python.pydev.analysis.additionalinfo.AbstractAdditionalInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.AdditionalProjectInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.AdditionalSystemInterpreterInfo;


public class InterpreterObserver implements IInterpreterObserver {

    private static final boolean DEBUG_INTERPRETER_OBSERVER = false;

    /**
     * @see org.python.pydev.ui.interpreters.IInterpreterObserver#notifyDefaultPythonpathRestored(org.python.pydev.ui.interpreters.AbstractInterpreterManager, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void notifyDefaultPythonpathRestored(IInterpreterManager manager, String interpreter, IProgressMonitor monitor){
        if(DEBUG_INTERPRETER_OBSERVER){
            System.out.println("notifyDefaultPythonpathRestored "+ interpreter);
        }
        try {
            try {
                final IInterpreterInfo interpreterInfo = manager.getInterpreterInfo(interpreter, new NullProgressMonitor());
                int grammarVersion = interpreterInfo.getGrammarVersion();
                AbstractAdditionalInterpreterInfo currInfo = AdditionalSystemInterpreterInfo.getAdditionalSystemInfo(manager, interpreter);
                if(currInfo != null){
                    currInfo.clearAllInfo();
                }
                InterpreterInfo defaultInterpreterInfo = (InterpreterInfo) manager.getInterpreterInfo(interpreter, monitor);
                ISystemModulesManager m = defaultInterpreterInfo.getModulesManager();
                AbstractAdditionalInterpreterInfo additionalSystemInfo = restoreInfoForModuleManager(monitor, m, 
                        "(system: " + manager.getManagerRelatedName() + " - " + interpreter + ")",
                        new AdditionalSystemInterpreterInfo(manager, interpreter), null, grammarVersion);

                if (additionalSystemInfo != null) {
                    //ok, set it and save it
                    AdditionalSystemInterpreterInfo.setAdditionalSystemInfo(manager, interpreter, additionalSystemInfo);
                    AbstractAdditionalInterpreterInfo.saveAdditionalSystemInfo(manager, interpreter);
                }
            } catch (NotConfiguredInterpreterException e) {
                //ok, nothing configured, nothing to do...
                PydevPlugin.log(e);
            }
        } catch (Throwable e) {
            PydevPlugin.log(e);
        }
    }

    /**
     * received when the interpreter manager is restored
     *  
     * this means that we have to restore the additional interpreter information we stored
     *  
     * @see org.python.pydev.ui.interpreters.IInterpreterObserver#notifyInterpreterManagerRecreated(org.python.pydev.ui.interpreters.AbstractInterpreterManager)
     */
    public void notifyInterpreterManagerRecreated(final IInterpreterManager iManager) {
        for(final String interpreter:iManager.getInterpreters()){
            if (!AdditionalSystemInterpreterInfo.loadAdditionalSystemInfo(iManager, interpreter)) {
                //not successfully loaded
                Job j = new Job("Pydev... Restoring additional info") {

                    @Override
                    protected IStatus run(IProgressMonitor monitorArg) {
                        try {
                            JobProgressComunicator jobProgressComunicator = new JobProgressComunicator(monitorArg, "Pydev... Restoring additional info", IProgressMonitor.UNKNOWN, this);
                            notifyDefaultPythonpathRestored(iManager, interpreter, jobProgressComunicator);
                            jobProgressComunicator.done();
                        } catch (Exception e) {
                            PydevPlugin.log(e);
                        }
                        return Status.OK_STATUS;
                    }

                };
                j.setPriority(Job.BUILD);
                j.schedule();
            }
        }
    }

    /**
     * Restores the info for a module manager
     * 
     * @param monitor a monitor to keep track of the progress
     * @param m the module manager
     * @param nature the associated nature (may be null if there is no associated nature -- as is the case when
     * restoring system info).
     * 
     * @return the info generated from the module manager
     */
    private AbstractAdditionalInterpreterInfo restoreInfoForModuleManager(IProgressMonitor monitor, IModulesManager m, String additionalFeedback, 
            AbstractAdditionalInterpreterInfo info, PythonNature nature, int grammarVersion) {
        
        //TODO: Check if keeping a zip file open makes things faster...
        //Timer timer = new Timer();
        ModulesKey[] allModules = m.getOnlyDirectModules();
        int i = 0;
        
        FastStringBuffer msgBuffer = new FastStringBuffer();

        for (ModulesKey key : allModules) {
            if(monitor.isCanceled()){
                return null;
            }
            i++;

            if (key.file != null) { //otherwise it should be treated as a compiled module (no ast generation)

                if (key.file.exists()) {

                    boolean isZipModule = key instanceof ModulesKeyForZip;
                    ModulesKeyForZip modulesKeyForZip = null;
                    if(isZipModule){
                        modulesKeyForZip = (ModulesKeyForZip) key;
                        if(DEBUG_INTERPRETER_OBSERVER){
                            System.out.println("Found zip: "+modulesKeyForZip.file+" - "+modulesKeyForZip.zipModulePath+" - "+modulesKeyForZip.name);
                        }
                    }
                    
                    if (PythonPathHelper.isValidSourceFile(key.file.getName()) || 
                            (isZipModule && PythonPathHelper.isValidSourceFile(modulesKeyForZip.zipModulePath))) {
                        
                        
                        if(i % 17 == 0){
                            msgBuffer.clear();
                            msgBuffer.append("Creating ");
                            msgBuffer.append(additionalFeedback);
                            msgBuffer.append(" additional info (" );
                            msgBuffer.append(i );
                            msgBuffer.append(" of " );
                            msgBuffer.append(allModules.length );
                            msgBuffer.append(") for " );
                            msgBuffer.append(key.file.getName());
                            monitor.setTaskName(msgBuffer.toString());
                            monitor.worked(1);
                        }

                        try {
                            
                            //the code below works with the default parser (that has much more info... and is much slower)
                            Object doc;
                            if(isZipModule){
                                doc = REF.getCustomReturnFromZip(modulesKeyForZip.file, modulesKeyForZip.zipModulePath, null);
                                
                            }else{
                                doc = REF.getCustomReturnFromFile(key.file, true, null);
                            }
                            
                            char [] charArray;
                            if(doc instanceof IDocument){
                                IDocument document = (IDocument) doc;
                                charArray = document.get().toCharArray();
                                
                            }else if(doc instanceof FastStringBuffer){
                                FastStringBuffer fastStringBuffer = (FastStringBuffer) doc;
                                charArray = fastStringBuffer.toCharArray();
                                
                            }else if(doc instanceof String){
                                String str = (String) doc;
                                charArray = str.toCharArray();
                                
                            }else if(doc instanceof char[]){
                                charArray = (char[]) doc;
                                
                            }else{
                                throw new RuntimeException("Don't know how to handle: "+doc+" -- "+doc.getClass());
                            }
                            
                            SimpleNode node = FastDefinitionsParser.parse(charArray, key.file.getName());
                            
                            
                            if (node != null) {
                                info.addAstInfo(node, key.name, nature, false);
                            }else{
                                String str = "Unable to generate ast -- using %s.\nError:%s";
                                ErrorDescription errorDesc = null;
                                throw new RuntimeException(StringUtils.format(str, 
                                        PyParser.getGrammarVersionStr(grammarVersion),
                                        (errorDesc!=null && errorDesc.message!=null)?
                                                errorDesc.message:"unable to determine"));
                            }

                        } catch (Exception e) {
                            PydevPlugin.log(IStatus.ERROR, "Problem parsing the file :" + key.file + ".", e);
                        }
                    }
                } else {
                    PydevPlugin.log("The file :" + key.file + " does not exist, but is marked as existing in the pydev code completion.");
                }
            }
        }
        //timer.printDiff("Time to restore additional info");
        return info;
    }


    public void notifyProjectPythonpathRestored(final PythonNature nature, IProgressMonitor monitor) {
        try {
            IModulesManager m = nature.getAstManager().getModulesManager();
            IProject project = nature.getProject();
            
            AbstractAdditionalDependencyInfo currInfo = AdditionalProjectInterpreterInfo.getAdditionalInfoForProject(nature);
            if(currInfo != null){
                currInfo.clearAllInfo();
            }
            
            AdditionalProjectInterpreterInfo newProjectInfo = new AdditionalProjectInterpreterInfo(project);
            String feedback = "(project:" + project.getName() + ")";
            synchronized(m){
                AbstractAdditionalDependencyInfo info = (AbstractAdditionalDependencyInfo) restoreInfoForModuleManager(
                        monitor, m, feedback, newProjectInfo, nature, nature.getGrammarVersion());
    
                if (info != null) {
                    //ok, set it and save it
                    AdditionalProjectInterpreterInfo.setAdditionalInfoForProject(project, info);
                    AdditionalProjectInterpreterInfo.saveAdditionalInfoForProject(nature);
                }
            }
        } catch (Exception e) {
            PydevPlugin.log(e);
            throw new RuntimeException(e);
        }
    }

    public void notifyNatureRecreated(final PythonNature nature, IProgressMonitor monitor) {
        if(!AdditionalProjectInterpreterInfo.loadAdditionalInfoForProject(nature)){
            if(DEBUG_INTERPRETER_OBSERVER){
                System.out.println("Unable to load the info correctly... restoring info from the pythonpath");
            }
            notifyProjectPythonpathRestored(nature, monitor);
        }
    }

}
