/**
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.python.pydev.analysis.actions;

import com.python.pydev.analysis.additionalinfo.AbstractAdditionalInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.IInfo;

/**
 * Contains information about some IInfo and its related additional info.
 * 
 * @author Fabio
 */
public class AdditionalInfoAndIInfo{
    
    public final AbstractAdditionalInterpreterInfo additionalInfo;
    public final IInfo info;
    
    public AdditionalInfoAndIInfo(AbstractAdditionalInterpreterInfo additionalInfo, IInfo info) {
        this.additionalInfo = additionalInfo;
        this.info = info;
    }

}
