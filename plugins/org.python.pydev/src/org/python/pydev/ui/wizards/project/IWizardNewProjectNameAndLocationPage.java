/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.python.pydev.ui.wizards.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.IWizardPage;
import org.python.pydev.core.IPythonNature;

/**
 * The first page in the New Project wizard must implement this interface.
 */
public interface IWizardNewProjectNameAndLocationPage extends IWizardPage
{
    /**
     * Returns a flag indicating whether the default python src folder
     * should be created.
     */
    public boolean shouldCreatSourceFolder();

    /**
     * @return a string as specified in the constants in IPythonNature
     * @see IPythonNature#PYTHON_VERSION_XXX 
     * @see IPythonNature#JYTHON_VERSION_XXX
     * @see IPythonNature#IRONPYTHON_VERSION_XXX
     */
    public String getProjectType();

    /**
     * Returns a handle to the new project.
     */
    public IProject getProjectHandle();
    
    public String getProjectName();

    /**
     * Gets the location path for the new project.
     */
    public IPath getLocationPath();

    /**
     * @return "Default" to mean that the default interpreter should be used or the complete path to an interpreter
     * configured.
     * 
     * Note that this changes from the python nature, where only the path is returned (because at this point, we
     * want to give the user a visual indication that it's the Default interpreter if that's the one selected)
     */
    public String getProjectInterpreter();

}
