/* 
 * Copyright (C) 2006, 2007  Dennis Hunziker, Ueli Kistler
 * Copyright (C) 2007  Reto Schuettel, Robin Stocker
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * 
 */

package org.python.pydev.refactoring.ast.adapters;

import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.refactoring.ast.visitors.VisitorFactory;
import org.python.pydev.refactoring.ast.visitors.context.ClassDefVisitor;
import org.python.pydev.refactoring.ast.visitors.context.LocalFunctionDefVisitor;
import org.python.pydev.refactoring.ast.visitors.context.ScopeAssignedVisitor;
import org.python.pydev.refactoring.ast.visitors.context.ScopeVariablesVisitor;

public abstract class AbstractScopeNode<T extends SimpleNode> extends AbstractNodeAdapter<T> {

	private List<SimpleAdapter> usedVariables;

	private List<SimpleAdapter> assignedVariables;

	private List<FunctionDefAdapter> functions;

	private List<IClassDefAdapter> classes;

	protected AbstractScopeNode() {
        
    }
	public AbstractScopeNode(ModuleAdapter module, AbstractScopeNode<? extends SimpleNode> parent, T node, String endLineDelim) {
		super(module, parent, node, endLineDelim);
	}

	public List<FunctionDefAdapter> getFunctions() {
		if (functions == null) {
			LocalFunctionDefVisitor visitor = VisitorFactory.createContextVisitor(LocalFunctionDefVisitor.class, getASTNode(), getModule(),
					this);
			functions = visitor.getAll();
		}

		return functions;
	}

	public List<IClassDefAdapter> getClasses() {
		if (this.classes == null) {
			ClassDefVisitor visitor = null;
			visitor = VisitorFactory.createContextVisitor(ClassDefVisitor.class, this.getASTNode(), getModule(), this);

			this.classes = visitor.getAll();
		}
		return this.classes;
	}

	public List<SimpleAdapter> getAssignedVariables() {
		if (assignedVariables == null) {
			ScopeAssignedVisitor visitor = VisitorFactory.createContextVisitor(ScopeAssignedVisitor.class, getASTNode(), this.getModule(),
					this);
			assignedVariables = visitor.getAll();
		}
		return assignedVariables;
	}

	public List<SimpleAdapter> getUsedVariables() {
		if (usedVariables == null) {
			ScopeVariablesVisitor visitor = VisitorFactory.createContextVisitor(ScopeVariablesVisitor.class, getASTNode(),
					this.getModule(), this);
			usedVariables = visitor.getAll();
		}
		return usedVariables;
	}

	public boolean alreadyUsedName(String newName) {
		for (SimpleAdapter adapter : this.getUsedVariables()) {
			if (adapter.getName().compareTo(newName) == 0) {
				return true;
			}
		}
		return false;
	}

}
