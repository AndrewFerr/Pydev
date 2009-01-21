package org.python.pydev.debug.newconsole.env;

import java.util.HashMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ui.console.IOConsole;
import org.python.pydev.debug.core.Constants;
import org.python.pydev.plugin.PydevPlugin;

/**
 * This class defines a process that pydev will spawn for the console.
 */
public class PydevSpawnedInterpreterProcess implements IProcess {

    /**
     * Boolean determining if this process was already terminated or not.
     */
    private boolean terminated;
    
    private Process spawnedInterpreterProcess;
    private ILaunch launch;
    private HashMap<String, String> attributes;
    
    public PydevSpawnedInterpreterProcess(Process spawnedInterpreterProcess, ILaunch launch){
        this.spawnedInterpreterProcess = spawnedInterpreterProcess;
        this.launch = launch;
        this.attributes = new HashMap<String, String>();
        this.setAttribute(IProcess.ATTR_PROCESS_TYPE, Constants.PROCESS_TYPE);
    }

    /**
     * @return the console associated with the run (null in this case)
     */
    public IOConsole getIOConsole() {
        return null;
    }
    
    public String getLabel() {
        return "Pydev Interactive Interpreter Process";
    }

    public ILaunch getLaunch() {
        return this.launch;
    }

    public IStreamsProxy getStreamsProxy() {
        return null;
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return this.attributes.get(key);
    }

    public int getExitValue() throws DebugException {
        return 0;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public boolean canTerminate() {
        return true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public void terminate() throws DebugException {
        try {
            if(this.spawnedInterpreterProcess != null){
                this.spawnedInterpreterProcess.destroy();
            }
        } catch (RuntimeException e) {
            PydevPlugin.log(e);
        }
        this.spawnedInterpreterProcess = null;
        terminated = true;
    }


}
