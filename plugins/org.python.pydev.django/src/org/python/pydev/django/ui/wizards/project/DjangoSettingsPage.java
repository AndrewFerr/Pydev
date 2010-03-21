package org.python.pydev.django.ui.wizards.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.python.pydev.core.ICallback0;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.ui.wizards.project.IWizardNewProjectNameAndLocationPage;

public class DjangoSettingsPage extends WizardPage {

	public static final String CPYTHON = "cpython";
	public static final String JYTHON = "jython";

	static final Map<String, List<String>> DB_ENGINES = new HashMap<String, List<String>>() {{
		put(CPYTHON, new ArrayList<String>() {{
			add("postgresql_psycopg2");
			add("sqlite3");
			add("mysql");
			add("oracle");
			add("other (just type in combo)");
		}});
		put(JYTHON, new ArrayList<String>() {{
			add("doj.backends.zxjdbc.postgresql");
			add("doj.backends.zxjdbc.sqlite3");
			add("doj.backends.zxjdbc.mysql");
			add("doj.backends.zxjdbc.oracle");
			add("other (just type in combo)");
		}});
	}};


	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    private Combo engineCombo;
    private Text nameText;
    private Text hostText;
    private Text portText;
    private Text userText;
    private Text passText;
	private ICallback0<IWizardNewProjectNameAndLocationPage> projectPageCallback;

    public DjangoSettingsPage(String pageName, ICallback0<IWizardNewProjectNameAndLocationPage> projectPage) {
        super(pageName);
        this.projectPageCallback = projectPage;
        setTitle("Django Settings");
        setDescription("Basic Django Settings");
    }


    private Label newLabel(Composite parent, String label) {
    	Label l = new Label(parent, SWT.NONE);
    	l.setText(label);
    	l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    	return l;
    }

    private Text newText(Composite parent) {
    	Text t = new Text(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = SIZING_TEXT_FIELD_WIDTH;
        t.setLayoutData(gd);
        return t;
    }

	
	public void createControl(Composite parent) {
        Composite topComp= new Composite(parent, SWT.NONE);
        GridLayout innerLayout= new GridLayout();
        innerLayout.numColumns= 1;
        innerLayout.marginHeight= 0;
        innerLayout.marginWidth= 0;
        topComp.setLayout(innerLayout);
        GridData gd= new GridData(GridData.FILL_BOTH);
        topComp.setLayoutData(gd);

        //Database Settings
        Group group = new Group(topComp, SWT.NONE);
        group.setText("Database settings");
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 8;
        layout.numColumns = 2;
        group.setLayout(layout);
        gd= new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);

        // Database Engine
        Label engineLabel = newLabel(group, "Database &Engine");

        engineCombo = new Combo(group, 0);
        final IWizardNewProjectNameAndLocationPage projectPage = projectPageCallback.call();
        
        
		String projectType = projectPage.getProjectType();
		List<String> engines = DB_ENGINES.get(
				projectType.startsWith("jython") ? DjangoSettingsPage.JYTHON : DjangoSettingsPage.CPYTHON);
		for (String engine : engines) {
			engineCombo.add(engine);
		}
		
        engineCombo.setText(engines.get(0));
        
        engineCombo.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				String selection = engineCombo.getText();
				if(selection.endsWith("sqlite3")){
			        String projectName = projectPage.getProjectName();
					nameText.setText(projectPage.getLocationPath().append(projectName).append("sqlite.db").toOSString());
				}
			}
			
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        
        gd= new GridData(GridData.FILL_HORIZONTAL);
        engineCombo.setLayoutData(gd);

        // Database Name
        Label nameLabel = newLabel(group, "Database &Name");
        nameText = newText(group);
        // Database Host
        Label hostLabel = newLabel(group, "Database &Host");
        hostText = newText(group);
        // Database Port
        Label portLabel = newLabel(group, "Database P&ort");
        portText = newText(group);

        // Database User
        Label userLabel = newLabel(group, "&Username");
        userText = newText(group);
        // Database Pass
        Label passLabel = newLabel(group, "&Password");
        passText = newText(group);
        passText.setEchoChar('*');
        setErrorMessage(null);
        setMessage(null);
        setControl(topComp);
	}

	public static class DjangoSettings {
		public String databaseEngine;
		public String databaseName;
		public String databaseHost;
		public String databasePort;
		public String databaseUser;
		public String databasePassword;

	}

	public DjangoSettings getSettings() {
		DjangoSettings s = new DjangoSettings();
		//make it suitable to be written
		s.databaseEngine = escapeSlashes(engineCombo.getText());
		s.databaseName = escapeSlashes(nameText.getText());
		s.databaseHost = escapeSlashes(hostText.getText());
		s.databasePort = escapeSlashes(portText.getText());
		s.databaseUser = escapeSlashes(userText.getText());
		s.databasePassword = escapeSlashes(passText.getText());
		return s;
	}


	private String escapeSlashes(String text) {
		return StringUtils.replaceAll(text, "\\", "\\\\\\\\");
	}
}
