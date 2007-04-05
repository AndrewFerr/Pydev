/*
 * Created on Dec 10, 2006
 * @author Fabio
 */
package com.python.pydev.refactoring.wizards.rename;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.python.pydev.core.IDefinition;
import org.python.pydev.core.IModule;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.codecompletion.revisited.ProjectModulesManager;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;
import org.python.pydev.editor.refactoring.RefactoringRequest;
import org.python.pydev.parser.visitors.scope.ASTEntry;
import org.python.pydev.plugin.nature.PythonNature;

import com.python.pydev.refactoring.actions.PyFindAllOccurrences;
import com.python.pydev.refactoring.refactorer.AstEntryRefactorerRequestConstants;
import com.python.pydev.refactoring.refactorer.RefactorerFindDefinition;

/**
 * This class provides helper methods for finding things in the workspace. 
 * 
 * The user is only required to implement {@link #getEntryOccurrences(String, SourceModule)} to
 * return the available references in the given module.
 * 
 * @author Fabio
 */
public abstract class AbstractRenameWorkspaceRefactorProcess extends AbstractRenameRefactorProcess{

    public static final boolean DEBUG_FILTERED_MODULES = false || PyFindAllOccurrences.DEBUG_FIND_REFERENCES;
	private RefactorerFindDefinition refactorerFindDefinition = new RefactorerFindDefinition();
    
    /**
     * May be used by subclasses
     */
    protected AbstractRenameWorkspaceRefactorProcess() {
    	
    }
    public AbstractRenameWorkspaceRefactorProcess(Definition definition) {
        super(definition);
    }
    
    /**
     * Gets and returns only the occurrences that point to what we're looking for, meaning that
     * we have to filter out references that may be pointing to some other definition,
     * and not the one we're actually refering to.
     * 
     * @param initialName the name we're looking for
     * @param module the module we're analyzing right now
     * @return a list with the references that point to the definition we're renaming. 
     */
    protected List<ASTEntry> getOccurrencesInOtherModule(RefactoringStatus status, String initialName, SourceModule module, PythonNature nature) {
        List<ASTEntry> entryOccurrences = findReferencesOnOtherModule(status, initialName, module);

        if(getRecheckWhereDefinitionWasFound()){
            for (Iterator<ASTEntry> iter = entryOccurrences.iterator(); iter.hasNext();) {
                ASTEntry entry = iter.next();
                int line = entry.node.beginLine;
                int col = entry.node.beginColumn;
                try {
                	ArrayList<IDefinition> definitions = new ArrayList<IDefinition>();
					refactorerFindDefinition.findActualDefinition(request, module, initialName, definitions, line, col, nature);
					//Definition[] definitions = module.findDefinition(new CompletionState(line-1, col-1, initialName, nature, ""), line, col, nature, null);
                    for (IDefinition def : definitions) {
                    	if(def instanceof Definition){
	                        Definition localDefinition = (Definition) def;
							//if within one module any of the definitions pointed to some class in some other module,
	                        //that means that the tokens in this module actually point to some other class 
	                        //(with the same name), and we can't actually rename them.
	                        String foundModName = localDefinition.module.getName();
	                        if(foundModName != null && !foundModName.equals(this.definition.module.getName())){
	                            if(DEBUG_FILTERED_MODULES){
	                                System.out.println("The entries found on module:"+module.getName()+" had the definition found on module:"+
	                                        foundModName+" and were removed from the elements to be renamed.");
	                                
	                            }
	                            return new ArrayList<ASTEntry>();
	                        }
                    	}
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
            }
        }
        return entryOccurrences;
    }
    
    /**
     * @return true if the definitions found should be re-checked for the module where it was defined
     * and false otherwise (the visitor already got correct matches)
     */
    protected abstract boolean getRecheckWhereDefinitionWasFound();
    
    /**
     * Default implementation for checking the tokens in the workspace.
     */
    @Override
    protected void findReferencesToRenameOnWorkspace(RefactoringRequest request, RefactoringStatus status) {
        request.pushMonitor(new SubProgressMonitor(request.getMonitor(), 50));
        try{
            findReferencesToRenameOnLocalScope(request, status);
            //if the user has set that we should only find references in the local scope in the checkInitialOnLocalScope
            //we should not try to find other references in the workspace.
        }finally{
            request.getMonitor().done();
            request.popMonitor();
        }
        boolean onlyInLocalScope = (Boolean)request.getAdditionalInfo(AstEntryRefactorerRequestConstants.FIND_REFERENCES_ONLY_IN_LOCAL_SCOPE, false);
        if(!onlyInLocalScope && !status.hasFatalError()){
            doCheckInitialOnWorkspace(status, request);
        }
    }
    
    /**
     * This method is made to be used in the checkInitialOnWorkspace implementation.
     * 
     * It will find files with possible references in the workspace (from the token
     * name we're searching) and for each file that maps to a module it will 
     * call getOccurrencesInOtherModule, and will add those occurrences to
     * the map with the file pointing to the entries.
     * 
     * @param status used to add some error status to the refactoring
     * @param request the request used for the refactoring
     */
    protected void doCheckInitialOnWorkspace(RefactoringStatus status, RefactoringRequest request){
        request.pushMonitor(new SubProgressMonitor(request.getMonitor(), 50));
        try{
            Set<IFile> references = new HashSet<IFile>(findFilesWithPossibleReferences(request));
            int total = references.size();
            request.getMonitor().beginTask("Possible references to analyze:"+total, total);
            request.getMonitor().setTaskName(StringUtils.format("Analyzing: %s files", total));
            int i=0;
            for (IFile file : references) {
                i++;
                request.communicateWork(StringUtils.format("Analyzing %s (%s of %s)", file.getName(), i, total));
                IProject project = file.getProject();
                PythonNature nature = PythonNature.getPythonNature(project);
                if(nature != null){
                	if(!nature.startRequests()){
                		continue;
                	}
                	try{
	                    ProjectModulesManager modulesManager = (ProjectModulesManager) nature.getAstManager().getModulesManager();
	                    
	                    request.checkCancelled();
	                    String modName = modulesManager.resolveModuleInDirectManager(file, project);
	                    
	                    if(modName != null){
	                        if(!request.moduleName.equals(modName)){
	                            //we've already checked the module from the request...
	                            
	                            request.checkCancelled();
	                            IModule module = nature.getAstManager().getModule(modName, nature, true, false);
	                            
	                            if(module instanceof SourceModule){
	                                
	                                request.checkCancelled();
	                                List<ASTEntry> entryOccurrences = getOccurrencesInOtherModule(status, request.initialName, (SourceModule) module, nature);
	                                
	                                if(entryOccurrences.size() > 0){
	                                    addOccurrences(entryOccurrences, file, modName);
	                                }
	                            }
	                        }
                        }
                    }finally{
                    	nature.endRequests();
                    }
                }
            }
        }catch (OperationCanceledException e) {
            //that's ok
        }catch (Exception e) {
            throw new RuntimeException(e);
        }finally{
            request.getMonitor().done();
            request.popMonitor();
        }
        
    }
    
    /**
     * This method is called for each module that may have some reference to the definition
     * we're looking for. 
     * 
     * It will be called for all the modules but the one in the request (for that one
     * the findReferencesToRenameOnLocalScope is called).
     * 
     * @param initialName this is the name of the token we're looking for
     * @param module this is the module that may contain references to that module
     * @return a list of entries that are references to the given module.
     */
    protected abstract List<ASTEntry> findReferencesOnOtherModule(RefactoringStatus status, String initialName, SourceModule module);


}
