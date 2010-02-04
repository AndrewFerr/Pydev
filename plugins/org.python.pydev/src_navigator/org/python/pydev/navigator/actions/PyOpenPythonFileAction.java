/*
 * Created on Oct 17, 2006
 * @author Fabio
 */
package org.python.pydev.navigator.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.python.pydev.editor.actions.PyOpenAction;
import org.python.pydev.editor.model.ItemPointer;
import org.python.pydev.editor.model.Location;
import org.python.pydev.navigator.PythonpathTreeNode;
import org.python.pydev.navigator.PythonpathZipChildTreeNode;
import org.python.pydev.navigator.elements.PythonNode;
import org.python.pydev.outline.ParsedItem;
import org.python.pydev.parser.visitors.NodeUtils;
import org.python.pydev.plugin.PydevPlugin;

/**
 * This action will try to open a given file or node in the pydev editor (if a file or node is selected).
 * 
 * If a container is selected, it will expand or collapse it.
 */
public class PyOpenPythonFileAction extends Action {

    protected final List<IFile> filesSelected = new ArrayList<IFile>();

    protected final List<PythonNode> nodesSelected = new ArrayList<PythonNode>();

    protected final List<Object> containersSelected = new ArrayList<Object>(); // IContainer or IWrappedResource or PythonpathTreeNode(with folder file)
    
    protected final List<PythonpathTreeNode> pythonPathFilesSelected = new ArrayList<PythonpathTreeNode>(); 
    
    protected final List<PythonpathZipChildTreeNode> pythonPathZipFilesSelected = new ArrayList<PythonpathZipChildTreeNode>(); 

    protected final ISelectionProvider provider;

    public PyOpenPythonFileAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        this.setText("Open With Pydev");
        this.provider = selectionProvider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public synchronized void run() {
        // clear them
        fillSelections();

        if (filesSelected.size() > 0) {
            openFiles(filesSelected);

        } else if (nodesSelected.size() > 0) {
		    PythonNode node = nodesSelected.iterator().next();
		    ParsedItem actualObject = node.getActualObject();
		    new PyOpenAction().run(new ItemPointer(node.getPythonFile().getActualObject(), NodeUtils.getNameTokFromNode(actualObject.getAstThis().node)));
		    
        } else if (pythonPathZipFilesSelected.size() > 0) {
        	openFiles(pythonPathZipFilesSelected.toArray(new PythonpathZipChildTreeNode[pythonPathZipFilesSelected.size()]));
        	
		} else if (pythonPathFilesSelected.size() > 0) {
			openFiles(pythonPathFilesSelected.toArray(new PythonpathTreeNode[pythonPathFilesSelected.size()]));

		} else if (containersSelected.size() > 0) {
		    if (this.provider instanceof TreeViewer) {
		        TreeViewer viewer = (TreeViewer) this.provider;
		        for (Object container : containersSelected) {
		            if (viewer.isExpandable(container)) {
		                viewer.setExpandedState(container, !viewer.getExpandedState(container));
		            }
		        }
		    } else {
		        PydevPlugin.log("Expecting the provider to be a TreeViewer -- it is:" + this.provider.getClass());
		    }
		}
    }
    
    protected void openFiles(PythonpathZipChildTreeNode[] pythonPathZipFilesSelected) {
    	PyOpenAction pyOpenAction = new PyOpenAction();
    	for(PythonpathZipChildTreeNode n:pythonPathZipFilesSelected){
    		pyOpenAction.run(new ItemPointer(n.zipStructure.file, new Location(), new Location(), null, n.zipPath));
    	}
    }

	protected void openFiles(PythonpathTreeNode[] pythonPathFilesSelected) {
		PyOpenAction pyOpenAction = new PyOpenAction();
		for(PythonpathTreeNode n:pythonPathFilesSelected){
			pyOpenAction.run(new ItemPointer(n.file));
		}
	}

    /**
     * Opens the given files with the Pydev editor. 
     * 
     * @param filesSelected the files to be opened with the pydev editor.
     */
    protected void openFiles(List<IFile> filesSelected) {
        for (IFile f : filesSelected) {
            new PyOpenAction().run(new ItemPointer(f));
        }
    }

    /**
     * This method will get the given selection and fill the related attributes to match the selection correctly
     * (files, nodes and containers).
     */
    protected synchronized void fillSelections() {
        filesSelected.clear();
        nodesSelected.clear();
        containersSelected.clear();
        pythonPathFilesSelected.clear();
        pythonPathZipFilesSelected.clear();

        ISelection selection = provider.getSelection();
        if (!selection.isEmpty()) {
            IStructuredSelection sSelection = (IStructuredSelection) selection;
            Iterator iterator = sSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();

                if (element instanceof PythonNode) {
                    nodesSelected.add((PythonNode) element);

                } else if (element instanceof PythonpathZipChildTreeNode) {
					PythonpathZipChildTreeNode node = (PythonpathZipChildTreeNode) element;
					if(node.isDir){
						containersSelected.add(node);
					}else{
						pythonPathZipFilesSelected.add(node);
					}
					
                	
                } else if (element instanceof PythonpathTreeNode) {
                	PythonpathTreeNode node = (PythonpathTreeNode) element;
                	if(node.file.isFile()){
                		pythonPathFilesSelected.add(node);
                	}else{
                		containersSelected.add(node);
                	}
                	
                } else if (element instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable) element;
                    IFile file = (IFile) adaptable.getAdapter(IFile.class);
                    if (file != null) {
                        filesSelected.add(file);

                    } else {
                        IContainer container = (IContainer) adaptable.getAdapter(IContainer.class);
                        if (container != null) {
                            containersSelected.add(element);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return whether the current selection enables this action (not considering selected containers).
     */
    public boolean isEnabledForSelectionWithoutContainers() {
        fillSelections();
        if(filesSelected.size() > 0 || nodesSelected.size() > 0 
        		|| pythonPathFilesSelected.size() > 0 || pythonPathZipFilesSelected.size() > 0){
            return true;
        }
        return false;
    }

}
