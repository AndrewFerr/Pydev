/*
 * License: Common Public License v1.0
 * Created on Oct 25, 2004
 * 
 * @author Fabio Zadrozny
 */
package org.python.pydev.builder.pylint;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.python.pydev.builder.PyDevBuilderVisitor;
import org.python.pydev.builder.PydevMarkerUtils;
import org.python.pydev.builder.PydevMarkerUtils.MarkerInfo;
import org.python.pydev.core.ProjectMisconfiguredException;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;
import org.python.pydev.runners.SimplePythonRunner;
import org.python.pydev.ui.UIConstants;

/**
 * 
 * Check lint.py for options.
 * 
 * @author Fabio Zadrozny
 */
public class PyLintVisitor extends PyDevBuilderVisitor {

    /* (non-Javadoc)
     * @see org.python.pydev.builder.PyDevBuilderVisitor#visitResource(org.eclipse.core.resources.IResource)
     */
    public static final String PYLINT_PROBLEM_MARKER = "org.python.pydev.pylintproblemmarker";

    public static final List<PyLintThread> pyLintThreads = new ArrayList<PyLintThread>();
    
    private static MessageConsole fConsole;
    
    /**
     * This class runs as a thread to get the markers, and only stops the IDE when the markers are being added.
     * 
     * @author Fabio Zadrozny
     */
    public static class PyLintThread extends Thread{
        
        IResource resource; 
        IDocument document; 
        IPath location;

        List<Object[]> markers = new ArrayList<Object[]>();
        
        public PyLintThread(IResource resource, IDocument document, IPath location){
            setName("PyLint thread");
            this.resource = resource;
            this.document = document;
            this.location = location;
        }
        
        /**
         * @return
         */
        private boolean canPassPyLint() {
            if(pyLintThreads.size() < PyLintPrefPage.getMaxPyLintDelta()){
                pyLintThreads.add(this);
                return true;
            }
            return false;
        }

        /**
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                if(canPassPyLint()){
                    
                    IOConsoleOutputStream out=null;
                    try {
                        MessageConsole console = getConsole();
                        if(console != null){
                            out = console.newOutputStream();
                        }
                    } catch (MalformedURLException e3) {
                        throw new RuntimeException(e3);
                    }

                    passPyLint(resource, out);
                    
                    new Job("Adding markers"){
                    
                        protected IStatus run(IProgressMonitor monitor) {
                            
                            ArrayList<MarkerInfo> lst = new ArrayList<PydevMarkerUtils.MarkerInfo>();
    
                            for (Iterator<Object[]> iter = markers.iterator(); iter.hasNext();) {
                                Object[] el = iter.next();
                                
                                String tok   = (String) el[0];
                                int priority = ((Integer)el[1]).intValue();
                                String id    = (String) el[2];
                                int line     = ((Integer)el[3]).intValue();
                                
                                lst.add(new PydevMarkerUtils.MarkerInfo(document, "ID:" + id + " " + tok,
                                        PYLINT_PROBLEM_MARKER, priority, false, false, line, 0, line, 0, null));
                            }
                            
                            PydevMarkerUtils.replaceMarkers(lst, resource, PYLINT_PROBLEM_MARKER, monitor);
    
                            return PydevPlugin.makeStatus(Status.OK, "", null);
                        }
                    }.schedule();
                }
                
            } catch (final Exception e) {
                new Job("Error reporting"){
                    protected IStatus run(IProgressMonitor monitor) {
                        PydevPlugin.log(e);
                        return PydevPlugin.makeStatus(Status.OK, "", null);
                    }
                }.schedule();
            }finally{
                try {
                    pyLintThreads.remove(this);
                } catch (Exception e) {
                    PydevPlugin.log(e);
                }
            }
        }

        private MessageConsole getConsole() throws MalformedURLException {
            if(PyLintPrefPage.useConsole()){
                if (fConsole == null){
                    fConsole = new MessageConsole("", PydevPlugin.getImageCache().getDescriptor(UIConstants.PY_ICON));
                    ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{fConsole});
                }
            }else{
                return null;
            }
            return fConsole;
        }


        /**
         * @param tok
         * @param type
         * @param priority
         * @param id
         * @param line
         */
        private void addToMarkers(String tok, int priority, String id, int line) {
            markers.add(new Object[]{tok, priority, id, line} );
        }
        
        /**
         * @param resource
         * @param out 
         * @param document
         * @param location
         * @throws CoreException
         * @throws ProjectMisconfiguredException 
         */
        private void passPyLint(IResource resource, IOConsoleOutputStream out) throws CoreException, ProjectMisconfiguredException {
            File script = new File(PyLintPrefPage.getPyLintLocation());
            File arg = new File(location.toOSString());

            ArrayList<String> list = new ArrayList<String>();
            list.add("--include-ids=y");
            
            //user args
            String userArgs = StringUtils.replaceNewLines(PyLintPrefPage.getPylintArgs(), " ");
            StringTokenizer tokenizer2 = new StringTokenizer(userArgs);
            while(tokenizer2.hasMoreTokens()){
                list.add(tokenizer2.nextToken());
            }
            list.add(REF.getFileAbsolutePath(arg));
            
            
            IProject project = resource.getProject();
            
            String scriptToExe = REF.getFileAbsolutePath(script);
            String[] paramsToExe = list.toArray(new String[0]);
            write("Pylint: Executing command line:'", out, scriptToExe, paramsToExe, "'");
            
            PythonNature nature = PythonNature.getPythonNature(project);
            if(nature == null){
                PydevPlugin.log(new RuntimeException("PyLint ERROR: Nature not configured for: "+project));
                return;
            }
            
            Tuple<String, String> outTup = new SimplePythonRunner().runAndGetOutputFromPythonScript(
                    nature.getProjectInterpreter().getExecutableOrJar(), scriptToExe, paramsToExe, arg.getParentFile(), project);
            
            write("Pylint: The stdout of the command line is: "+outTup.o1, out);
            write("Pylint: The stderr of the command line is: "+outTup.o2, out);
            
            String output = outTup.o1;

            StringTokenizer tokenizer = new StringTokenizer(output, "\r\n");
            
            boolean useW = PyLintPrefPage.useWarnings();
            boolean useE = PyLintPrefPage.useErrors();
            boolean useF = PyLintPrefPage.useFatal();
            boolean useC = PyLintPrefPage.useCodingStandard();
            boolean useR = PyLintPrefPage.useRefactorTips();
            
            //Set up local values for severity
            int wSeverity = PyLintPrefPage.wSeverity();
            int eSeverity = PyLintPrefPage.eSeverity();
            int fSeverity = PyLintPrefPage.fSeverity();
            int cSeverity = PyLintPrefPage.cSeverity();
            int rSeverity = PyLintPrefPage.rSeverity();
            
            //System.out.println(output);
            if(output.indexOf("Traceback (most recent call last):") != -1){
                PydevPlugin.log(new RuntimeException("PyLint ERROR: \n"+output));
                return;
            }
            if(outTup.o2.indexOf("Traceback (most recent call last):") != -1){
                PydevPlugin.log(new RuntimeException("PyLint ERROR: \n"+outTup.o2));
                return;
            }
            while(tokenizer.hasMoreTokens()){
                String tok = tokenizer.nextToken();
                
                try {
                    boolean found=false;
                    int priority = 0;
                    
                    //W0611:  3: Unused import finalize
                    //F0001:  0: Unable to load module test.test2 (list index out of range)
                    //C0321: 25:fdfd: More than one statement on a single line
                    int indexOfDoublePoints = tok.indexOf(":");
                    if(indexOfDoublePoints != -1){
                        
                        if(tok.startsWith("C")&& useC){
                            found=true;
                            //priority = IMarker.SEVERITY_WARNING;
                            priority = cSeverity;
                        }
                        else if(tok.startsWith("R")  && useR ){
                            found=true;
                            //priority = IMarker.SEVERITY_WARNING;
                            priority = rSeverity;
                        }
                        else if(tok.startsWith("W")  && useW ){
                            found=true;
                            //priority = IMarker.SEVERITY_WARNING;
                            priority = wSeverity;
                        }
                        else if(tok.startsWith("E") && useE ){
                            found=true;
                            //priority = IMarker.SEVERITY_ERROR;
                            priority = eSeverity;
                        }
                        else if(tok.startsWith("F") && useF ){
                            found=true;
                            //priority = IMarker.SEVERITY_ERROR;
                            priority = fSeverity;
                        }else{
                            continue;
                        }
                        
                    }else{
                        continue;
                    }
                    
                    try {
                        if(found){
                            String id = tok.substring(0, tok.indexOf(":")).trim();
                            
                            int i = tok.indexOf(":");
                            if(i == -1)
                                continue;
                            
                            tok = tok.substring(i+1);

                            i = tok.indexOf(":");
                            if(i == -1)
                                continue;
                            
                            int line = Integer.parseInt(tok.substring(0, i).trim() );
                            
                            IRegion region = null;
                            try {
                                region = document.getLineInformation(line - 1);
                            } catch (Exception e) {
                                region = document.getLineInformation(line);
                            }
                            String lineContents = document.get(region.getOffset(), region.getLength());
                            
                            int pos = -1;
                            if( ( pos = lineContents.indexOf("IGNORE:") ) != -1){
                                String lintW = lineContents.substring(pos+"IGNORE:".length());
                                if (lintW.startsWith(id)){
                                    continue;
                                }
                            }
                            
                            i = tok.indexOf(":");
                            if(i == -1)
                                continue;

                            tok = tok.substring(i+1);
                            addToMarkers(tok, priority, id, line-1);
                        }
                    } catch (RuntimeException e2) {
                        PydevPlugin.log(e2);
                    }
                } catch (Exception e1) {
                    PydevPlugin.log(e1);
                }
            }
        }


    }
    
    public void visitChangedResource(IResource resource, IDocument document, IProgressMonitor monitor) {
        if(document == null){
            return;
        }
        if(PyLintPrefPage.usePyLint() == false){
            try {
                resource.deleteMarkers(PYLINT_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
            } catch (CoreException e3) {
                PydevPlugin.log(e3);
            }
            return;
        }
        
        IProject project = resource.getProject();
        PythonNature pythonNature = PythonNature.getPythonNature(project);
        try {
            //pylint can only be used for jython projects
            if (!pythonNature.isPython()) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        if (project != null && resource instanceof IFile) {

            IFile file = (IFile) resource;
            IPath location = file.getRawLocation();
            if(location != null){
                PyLintThread thread = new PyLintThread(resource, document, location);
                thread.start();
            }
        }
    }
    
    public static void write(String cmdLineToExe, IOConsoleOutputStream out, Object ... args) {
        try {
            if(fConsole != null && out != null){
                synchronized(fConsole){
                    if(args != null){
                        for (Object arg : args) {
                            if(arg instanceof String){
                                cmdLineToExe += " "+arg;
                            }else if(arg instanceof String[]){
                                String[] strings = (String[]) arg;
                                for (String string : strings) {
                                    cmdLineToExe += " "+string;
                                }
                            }
                        }
                    }
                    out.write(cmdLineToExe);
                }
            }
        } catch (IOException e) {
            PydevPlugin.log(e);
        }
    }

    /**
     * @see org.python.pydev.builder.PyDevBuilderVisitor#visitRemovedResource(org.eclipse.core.resources.IResource, org.eclipse.jface.text.IDocument)
     */
    public void visitRemovedResource(IResource resource, IDocument document, IProgressMonitor monitor) {
    }

    /**
     * @see org.python.pydev.builder.PyDevBuilderVisitor#maxResourcesToVisit()
     */
    public int maxResourcesToVisit() {
        int i = PyLintPrefPage.getMaxPyLintDelta();
        if (i < 0){
            i = 0;
        }
        return i;
    }
}
