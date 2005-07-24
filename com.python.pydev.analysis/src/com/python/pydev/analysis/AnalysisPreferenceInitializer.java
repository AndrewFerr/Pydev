/*
 * Created on 24/07/2005
 */
package com.python.pydev.analysis;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;

public class AnalysisPreferenceInitializer extends AbstractPreferenceInitializer{

    public static final String DEFAULT_SCOPE = "com.python.pydev.analysis";
    
    public static final String USE_PYDEV_ANALYSIS = "USE_PYDEV_ANALYSIS";
    public static final boolean DEFAULT_USE_PYDEV_ANALYSIS = true;

    public static final String SEVERITY_UNUSED_VARIABLE = "SEVERITY_UNUSED_VARIABLE";
    public static final int DEFAULT_SEVERITY_UNUSED_VARIABLE = IMarker.SEVERITY_WARNING;
    
    public static final String SEVERITY_UNUSED_IMPORT = "SEVERITY_UNUSED_IMPORT";
    public static final int DEFAULT_SEVERITY_UNUSED_IMPORT = IMarker.SEVERITY_WARNING;

    public static final String SEVERITY_UNDEFINED_VARIABLE = "SEVERITY_UNDEFINED_VARIABLE";
    public static final int DEFAULT_SEVERITY_UNDEFINED_VARIABLE = IMarker.SEVERITY_ERROR;
    
    public static final String SEVERITY_DUPLICATED_SIGNATURE = "SEVERITY_DUPLICATED_SIGNATURE";
    public static final int DEFAULT_SEVERITY_DUPLICATED_SIGNATURE = IMarker.SEVERITY_ERROR;
    
    

    @Override
    public void initializeDefaultPreferences() {
        Preferences node = new DefaultScope().getNode(DEFAULT_SCOPE);
        
        node.putBoolean(USE_PYDEV_ANALYSIS, DEFAULT_USE_PYDEV_ANALYSIS);
        node.putInt(SEVERITY_UNUSED_IMPORT, DEFAULT_SEVERITY_UNUSED_IMPORT);
        node.putInt(SEVERITY_UNUSED_VARIABLE, DEFAULT_SEVERITY_UNUSED_VARIABLE);
        node.putInt(SEVERITY_UNDEFINED_VARIABLE, DEFAULT_SEVERITY_UNDEFINED_VARIABLE);
        node.putInt(SEVERITY_DUPLICATED_SIGNATURE, DEFAULT_SEVERITY_DUPLICATED_SIGNATURE);
    }



}
