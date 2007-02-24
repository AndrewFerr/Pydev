/*
 * Created on 21/08/2005
 */
package com.python.pydev.analysis.visitors;

import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.IDefinition;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.Tuple3;
import org.python.pydev.core.structure.CompletionRecursionException;
import org.python.pydev.editor.codecompletion.revisited.CompletionStateFactory;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceToken;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;

import com.python.pydev.analysis.scopeanalysis.AbstractScopeAnalyzerVisitor;

/**
 * The import checker not only generates information on errors for unresolved modules, but also gathers
 * dependency information so that we can do incremental building of dependent modules.
 * 
 * @author Fabio
 */
public class ImportChecker {

    /**
     * this is the nature we are analyzing
     */
    private IPythonNature nature;

    /**
     * this is the name of the module that we are analyzing
     */
    private String moduleName;

    private AbstractScopeAnalyzerVisitor visitor;

    /**
     * This is the information stored about some import:
     * Contains the actual module, the representation in the current module and whether it was resolved or not.
     */
    public static class ImportInfo{
        /**
         * This is the module where this info was found
         */
    	public IModule mod;
        /**
         * This is the token that relates to this import info (in the module it was found)
         */
    	public IToken token;
    	/**
         * This is the representation where it was found 
    	 */
    	public String rep;
        /**
         * Determines whether it was resolved or not (if not resolved, the other attributes may be null)
         */
    	public boolean wasResolved;
	    	
    	public ImportInfo(IModule mod, String rep, IToken token, boolean wasResolved){
    		this.mod = mod;
    		this.rep = rep;
            this.token = token;
    		this.wasResolved = wasResolved;
    	}
        
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("ImportInfo(");
            buffer.append(" Resolved:");
            buffer.append(wasResolved);
            if(wasResolved){
                buffer.append(" Rep:");
                buffer.append(rep);
                buffer.append(" Mod:");
                buffer.append(mod.getName());
            }
            buffer.append(")");
            return buffer.toString();
        }

        /**
         * @return the definition that matches this
         */
		public Definition getModuleDefinitionFromImportInfo(IPythonNature nature) {
            try {
                IDefinition[] definitions = this.mod.findDefinition(CompletionStateFactory.getEmptyCompletionState(this.rep, nature), -1, -1, nature, null);
                for (IDefinition definition : definitions) {
                    if(definition instanceof Definition){
                        Definition d = (Definition) definition;
                        if(d.module != null && d.value.length() == 0 && d.ast == null){
                            return d;
                        }
                        
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
		}
    }
    
    /**
     * constructor - will remove all dependency info on the project that we will start to analyze
     */
    public ImportChecker(AbstractScopeAnalyzerVisitor visitor, IPythonNature nature, String moduleName) {
        this.nature = nature;
        this.moduleName = moduleName;
        this.visitor = visitor;
    }

    /**
     * @param token MUST be an import token
     * @param reportUndefinedImports 
     * 
     * @return the module where the token was found and a String representing the way it was found 
     * in the module.
     * 
     * Note: it may return information even if the token was not found in the representation required. This is useful
     * to get dependency info, because it is actually dependent on the module, event though it does not have the
     * token we were looking for.
     */
    public ImportInfo visitImportToken(IToken token, boolean reportUndefinedImports) {
        return visitImportToken(reportUndefinedImports, token, moduleName, nature, visitor);
    }

    /**
     * This is so that we can use it without actually being in some visit.
     */
    public static ImportInfo visitImportToken(boolean reportUndefinedImports, IToken token, String moduleName,
            IPythonNature nature, AbstractScopeAnalyzerVisitor visitor) {
        //try to find it as a relative import
        boolean wasResolved = false;
        Tuple3<IModule, String, IToken> modTok = null;
		if(token instanceof SourceToken){
        	
        	ICodeCompletionASTManager astManager = nature.getAstManager();
        	ICompletionState state = CompletionStateFactory.getEmptyCompletionState(token.getRepresentation(), nature);
            try {
				modTok = astManager.findOnImportedMods(new IToken[]{token}, state, moduleName);
			} catch (CompletionRecursionException e1) {
				modTok = null;//unable to resolve it
			}
        	if(modTok != null && modTok.o1 != null){

        		if(modTok.o2.length() == 0){
        		    wasResolved = true;
                    
        		} else{
        			try {
						if( astManager.getRepInModule(modTok.o1, modTok.o2, nature) != null){
							wasResolved = true;
						}
					} catch (CompletionRecursionException e) {
						//not resolved...
					}
                }
        	}
        	
            
            //if it got here, it was not resolved
        	if(!wasResolved && reportUndefinedImports){
                visitor.onAddUnresolvedImport(token);
        	}
            
        }
        
        //might still return a modTok, even if the token we were looking for was not found.
        if(modTok != null){
        	return new ImportInfo(modTok.o1, modTok.o2, modTok.o3, wasResolved);
        }else{
        	return new ImportInfo(null, null, null, wasResolved);
        }
    }

}
