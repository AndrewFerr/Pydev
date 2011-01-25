/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on Sep 13, 2005
 * 
 * @author Fabio Zadrozny
 */
package com.python.pydev.analysis.additionalinfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.Tuple;
import org.python.pydev.plugin.PydevPlugin;

import com.python.pydev.analysis.AnalysisPlugin;


public class AdditionalSystemInterpreterInfo extends AbstractAdditionalDependencyInfo{

    private IInterpreterManager manager;
    private String additionalInfoInterpreter;
    
    /**
     * holds system info (interpreter name points to system info)
     */
    private static Map<Tuple<String, String>, AbstractAdditionalInterpreterInfo> additionalSystemInfo = 
        new HashMap<Tuple<String, String>, AbstractAdditionalInterpreterInfo>();

    public AdditionalSystemInterpreterInfo(IInterpreterManager manager, String interpreter) throws MisconfigurationException {
        super(false); //don't call init just right now...
        this.manager = manager;
        this.additionalInfoInterpreter = interpreter;
        init();
    }
    
    public IInterpreterManager getManager(){
        return manager;
    }
    
    public String getAdditionalInfoInterpreter(){
        return additionalInfoInterpreter;
    }
    
    /**
     * @return the path to the folder we want to keep things on
     * @throws MisconfigurationException 
     */
    protected File getPersistingFolder() throws MisconfigurationException {
        File base;
        try {
            IPath stateLocation = AnalysisPlugin.getDefault().getStateLocation();
            String osString = stateLocation.toOSString();
            if (osString.length() > 0) {
                char c = osString.charAt(osString.length() - 1);
                if (c != '\\' && c != '/') {
                    osString += '/';
                }
            }
            base = new File(osString);
        } catch (NullPointerException e) {
            //it may fail in tests... (save it in default folder in this cases)
            PydevPlugin.log(IStatus.ERROR, "Error getting persisting folder", e, false);
            base = new File(".");
        }
        File file = new File(base, getInterpreterRelatedName());
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    @Override
    protected File getPersistingLocation() throws MisconfigurationException {
        return new File(getPersistingFolder(), manager.getManagerRelatedName() + ".pydevsysteminfo");
    }
    
    

    private String getInterpreterRelatedName() throws MisconfigurationException {
        IInterpreterInfo info = manager.getInterpreterInfo(this.additionalInfoInterpreter, new NullProgressMonitor());
        return manager.getManagerRelatedName() + "_"+ info.getExeAsFileSystemValidPath();
    }


    @Override
    protected void setAsDefaultInfo() {
        AdditionalSystemInterpreterInfo.setAdditionalSystemInfo(manager, this.additionalInfoInterpreter, this);
    }

    /**
     * @param interpreter 
     * @return whether the info was successfully loaded or not
     * @throws MisconfigurationException 
     */
    public static boolean loadAdditionalSystemInfo(IInterpreterManager manager, String interpreter) throws MisconfigurationException {
        AbstractAdditionalInterpreterInfo info = new AdditionalSystemInterpreterInfo(manager, interpreter);
        //when it is successfully loaded, it sets itself as the default (for its type)
        return info.load();
    }

    /**
     * @param m the module manager that we want to get info on (python, jython...)
     * @return the additional info for the system
     * @throws MisconfigurationException 
     */
    public static AbstractAdditionalInterpreterInfo getAdditionalSystemInfo(IInterpreterManager manager, String interpreter) throws MisconfigurationException {
        Tuple<String,String> key = new Tuple<String, String>(manager.getManagerRelatedName(), interpreter);
        AbstractAdditionalInterpreterInfo info = additionalSystemInfo.get(key);
        if(info == null){
            //temporary until it's loaded!
			return new AdditionalSystemInterpreterInfo(manager, interpreter);
        }
        return info;
    }

    /**
     * sets the additional info (overrides if already set)
     * @param manager the manager we want to set info on
     * @param additionalSystemInfoToSet the info to set
     */
    public static void setAdditionalSystemInfo(IInterpreterManager manager, String interpreter, 
            AbstractAdditionalInterpreterInfo additionalSystemInfoToSet) {
        
        additionalSystemInfo.put(new Tuple<String, String>(manager.getManagerRelatedName(), interpreter), 
                additionalSystemInfoToSet);
    }

}
