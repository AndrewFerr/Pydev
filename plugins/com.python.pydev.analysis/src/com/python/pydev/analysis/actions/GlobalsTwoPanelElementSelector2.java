
package com.python.pydev.analysis.actions;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import com.python.pydev.analysis.AnalysisPlugin;
import com.python.pydev.analysis.additionalinfo.AbstractAdditionalInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.AdditionalProjectInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.IInfo;
import com.python.pydev.analysis.additionalinfo.InfoFactory;

/**
 * Let us choose from a list of IInfo (and the related additional info)
 */
public class GlobalsTwoPanelElementSelector2 extends FilteredItemsSelectionDialog{

    private static final String DIALOG_SETTINGS = "com.python.pydev.analysis.actions.GlobalsTwoPanelElementSelector2"; //$NON-NLS-1$

    private static final String WORKINGS_SET_SETTINGS = "WorkingSet"; //$NON-NLS-1$

    private WorkingSetFilterActionGroup workingSetFilterActionGroup;

    private CustomWorkingSetFilter workingSetFilter = new CustomWorkingSetFilter();

    private String title;

    private List<AbstractAdditionalInterpreterInfo> additionalInfo;

    private String selectedText;

    public GlobalsTwoPanelElementSelector2(Shell shell, boolean multi, String selectedText) {
        super(shell, multi);
        this.selectedText = selectedText;

        setSelectionHistory(new InfoSelectionHistory());

        setTitle("Pydev: Globals Browser");

        NameIInfoLabelProvider resourceItemLabelProvider = new NameIInfoLabelProvider(true);

        ModuleIInfoLabelProvider resourceItemDetailsLabelProvider = new ModuleIInfoLabelProvider();

        setListLabelProvider(resourceItemLabelProvider);
        setDetailsLabelProvider(resourceItemDetailsLabelProvider);
    }

    public void setTitle(String title){
        super.setTitle(title);
        this.title = title;
    }

    /**
     * Used to add the working set to the title.
     */
    private void setSubtitle(String text){
        if(text == null || text.length() == 0){
            getShell().setText(title);
        }else{
            getShell().setText(title + " - " + text); //$NON-NLS-1$
        }
    }

    protected IDialogSettings getDialogSettings(){
        IDialogSettings settings = AnalysisPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

        if(settings == null){
            settings = AnalysisPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    protected void storeDialog(IDialogSettings settings){
        super.storeDialog(settings);

        XMLMemento memento = XMLMemento.createWriteRoot("workingSet"); //$NON-NLS-1$
        workingSetFilterActionGroup.saveState(memento);
        workingSetFilterActionGroup.dispose();
        StringWriter writer = new StringWriter();
        try{
            memento.save(writer);
            settings.put(WORKINGS_SET_SETTINGS, writer.getBuffer().toString());
        }catch(IOException e){
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, AnalysisPlugin.getPluginID(), IStatus.ERROR, "", e)); //$NON-NLS-1$
            // don't do anything. Simply don't store the settings
        }
    }

    protected void restoreDialog(IDialogSettings settings){
        super.restoreDialog(settings);

        String setting = settings.get(WORKINGS_SET_SETTINGS);
        if(setting != null){
            try{
                IMemento memento = XMLMemento.createReadRoot(new StringReader(setting));
                workingSetFilterActionGroup.restoreState(memento);
            }catch(WorkbenchException e){
                StatusManager.getManager().handle(
                        new Status(IStatus.ERROR, AnalysisPlugin.getPluginID(), IStatus.ERROR, "", e)); //$NON-NLS-1$
                // don't do anything. Simply don't restore the settings
            }
        }

        addListFilter(workingSetFilter);

        applyFilter();
    }

    /**
     * We need to add the action for the working set.
     */
    protected void fillViewMenu(IMenuManager menuManager){
        super.fillViewMenu(menuManager);

        workingSetFilterActionGroup = new WorkingSetFilterActionGroup(getShell(), new IPropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event){
                String property = event.getProperty();

                if(WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)){

                    IWorkingSet workingSet = (IWorkingSet) event.getNewValue();

                    if(workingSet != null && !(workingSet.isAggregateWorkingSet() && workingSet.isEmpty())){
                        workingSetFilter.setWorkingSet(workingSet);
                        setSubtitle(workingSet.getLabel());
                    }else{
                        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                        if(window != null){
                            IWorkbenchPage page = window.getActivePage();
                            workingSet = page.getAggregateWorkingSet();

                            if(workingSet.isAggregateWorkingSet() && workingSet.isEmpty()){
                                workingSet = null;
                            }
                        }

                        workingSetFilter.setWorkingSet(workingSet);
                        setSubtitle(null);
                    }

                    scheduleRefresh();
                }
            }
        });

        menuManager.add(new Separator());
        workingSetFilterActionGroup.fillContextMenu(menuManager);
    }

    protected Control createExtendedContentArea(Composite parent){
        return null;
    }


    public Object[] getResult(){
        Object[] result = super.getResult();

        if(result == null)
            return null;

        List<AdditionalInfoAndIInfo> resultToReturn = new ArrayList<AdditionalInfoAndIInfo>();

        for(int i = 0; i < result.length; i++){
            if(result[i] instanceof AdditionalInfoAndIInfo){
                resultToReturn.add((AdditionalInfoAndIInfo) result[i]);
            }
        }

        return resultToReturn.toArray(new AdditionalInfoAndIInfo[resultToReturn.size()]);
    }

    /**
     * Overridden to set the initial pattern (if null we have an exception, so, it must at least be empty)
     */
    public int open(){
        if(getInitialPattern() == null){
            setInitialPattern(selectedText==null?"": selectedText);
        }else{
            setInitialPattern("");
        }
        return super.open();
    }

    public String getElementName(Object item){
        AdditionalInfoAndIInfo info = (AdditionalInfoAndIInfo) item;
        return info.info.getName();
    }

    protected IStatus validateItem(Object item){
        return Status.OK_STATUS;
    }

    protected ItemsFilter createFilter(){
        return new InfoFilter();
    }

    /**
     * Sets the elements we should work on (must be set before open())
     */
    public void setElements(List<AbstractAdditionalInterpreterInfo> additionalInfo){
        this.additionalInfo = additionalInfo;
    }


    protected Comparator<AdditionalInfoAndIInfo> getItemsComparator(){
        return new Comparator<AdditionalInfoAndIInfo>(){

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Comparator#compare(java.lang.Object,
             *      java.lang.Object)
             */
            public int compare(AdditionalInfoAndIInfo resource1, AdditionalInfoAndIInfo resource2){
                Collator collator = Collator.getInstance();
                String s1 = resource1.info.getName();
                String s2 = resource2.info.getName();
                int comparability = collator.compare(s1, s2);
                //same name
                if(comparability == 0){
                    String p1 = resource1.info.getDeclaringModuleName();
                    String p2 = resource2.info.getDeclaringModuleName();
                    if(p1 == null && p2 == null){
                        return 0;
                    }
                    if(p1 != null && p2 == null){
                        return -1;
                    }
                    if(p1 == null && p2 != null){
                        return 1;
                    }
                    return p1.compareTo(p2);
                }

                return comparability;
            }
        };
    }

    /**
     * This is the place where we put all the info in the content provider. Note that here we must add
     * ALL the info -- later, we'll filter it based on the active working set.
     */
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException{
        if(itemsFilter instanceof InfoFilter){
            if (progressMonitor != null){
                progressMonitor.beginTask("Searching...",this.additionalInfo.size());
            }
            
            for(AbstractAdditionalInterpreterInfo additionalInfo:this.additionalInfo){
                if(progressMonitor != null){
                    if (progressMonitor.isCanceled()){
                        return;
                    }else{
                        progressMonitor.worked(1);
                    }
                }
                Collection<IInfo> allTokens = new HashSet<IInfo>(additionalInfo.getAllTokens()); //no duplicates
                for(IInfo iInfo:allTokens){
                    contentProvider.add(new AdditionalInfoAndIInfo(additionalInfo, iInfo), itemsFilter);
                }
            }
        }
        
        
        if(progressMonitor != null){
            progressMonitor.done();
        }

    }


    /**
     * Viewer filter which filters resources due to current working set
     */
    private class CustomWorkingSetFilter extends ViewerFilter{
        
        private ResourceWorkingSetFilter resourceWorkingSetFilter = new ResourceWorkingSetFilter();

        public void setWorkingSet(IWorkingSet workingSet){
            resourceWorkingSetFilter.setWorkingSet(workingSet);
        }
        

        public boolean select(Viewer viewer, Object parentElement, Object element){
            if(element instanceof AdditionalInfoAndIInfo){
                AdditionalInfoAndIInfo info = (AdditionalInfoAndIInfo) element;
                if(info.additionalInfo instanceof AdditionalProjectInterpreterInfo){
                    AdditionalProjectInterpreterInfo projectInterpreterInfo = (AdditionalProjectInterpreterInfo) info.additionalInfo;
                    return resourceWorkingSetFilter.select(viewer, parentElement, projectInterpreterInfo.getProject());
                }
            }
            return resourceWorkingSetFilter.select(viewer, parentElement, element);
        }
    }


    /**
     * Filters the info based on the pattern (considers each dot as a new scope in the pattern.)
     */
    protected class InfoFilter extends ItemsFilter{

        public InfoFilter() {
            super();
        }

        
        /**
         * Must have a valid name.
         */
        public boolean isConsistentItem(Object item){
            if(!(item instanceof AdditionalInfoAndIInfo)){
                return false;
            }
            AdditionalInfoAndIInfo iInfo = (AdditionalInfoAndIInfo) item;
            if(iInfo.info.getName() != null){
                return true;
            }
            return false;
        }
        
        /**
         * We must override it so that the results are properly updating according to the scopes in the pattern
         * (if we only returned false it'd also work, but it'd need to traverse all the items at each step).
         */
        public boolean isSubFilter(ItemsFilter filter){
            if(!(filter instanceof InfoFilter)){
                return false;
            }

            return MatchHelper.isSubFilter(this.patternMatcher.getPattern(), ((InfoFilter) filter).patternMatcher.getPattern());
        }

        /**
         * Override so that we consider scopes.
         */
        public boolean equalsFilter(ItemsFilter filter){
            if(!(filter instanceof InfoFilter)){
                return false;
            }
            return MatchHelper.equalsFilter(this.patternMatcher.getPattern(), ((InfoFilter) filter).patternMatcher.getPattern());
        }


        /**
         * Overridden to consider each dot as a new scope in the pattern (and match according to modules)
         */
        public boolean matchItem(Object item){
            if(!(item instanceof AdditionalInfoAndIInfo)){
                return false;
            }
            AdditionalInfoAndIInfo info = (AdditionalInfoAndIInfo) item;
            return MatchHelper.matchItem(patternMatcher, info.info);
        }

    }
    
    /**
     * Used to store/restore the selections.
     */
    private class InfoSelectionHistory extends SelectionHistory{

        protected Object restoreItemFromMemento(IMemento element){
            InfoFactory infoFactory = new InfoFactory();
            AdditionalInfoAndIInfo resource = (AdditionalInfoAndIInfo) infoFactory.createElement(element);
            return resource;
        }

        protected void storeItemToMemento(Object item, IMemento element){
            AdditionalInfoAndIInfo resource = (AdditionalInfoAndIInfo) item;
            InfoFactory infoFactory = new InfoFactory(resource);
            infoFactory.saveState(element);
        }

    }


}
