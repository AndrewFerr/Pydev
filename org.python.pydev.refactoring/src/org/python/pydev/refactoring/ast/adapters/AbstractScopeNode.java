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

	public AbstractScopeNode(ModuleAdapter module, AbstractScopeNode<?> parent, T node) {
		super(module, parent, node);
		this.usedVariables = null;
		this.assignedVariables = null;
		this.classes = null;
		this.functions = null;
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
