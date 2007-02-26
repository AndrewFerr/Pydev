package org.python.pydev.refactoring.ast.adapters;

import java.util.List;

import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.refactoring.ast.visitors.VisitorFactory;
import org.python.pydev.refactoring.ast.visitors.context.LocalFunctionDefVisitor;
import org.python.pydev.refactoring.ast.visitors.context.ScopeAssignedVisitor;
import org.python.pydev.refactoring.ast.visitors.position.IndentVisitor;

public class FunctionDefAdapter extends AbstractScopeNode<FunctionDef> {

	private FunctionArgAdapter arguments;

	private List<FunctionDefAdapter> functions;

	public FunctionDefAdapter(ModuleAdapter module, AbstractScopeNode<?> parent,
			FunctionDef node) {
		super(module, parent, node);
		this.arguments = new FunctionArgAdapter(getModule(), this,
				getASTNode().args);
		this.functions = null;
	}

	public FunctionArgAdapter getArguments() {
		return arguments;
	}

	public boolean isInit() {
		return nodeHelper.isInit(getASTNode());
	}

	public boolean isDefaultInit() {
		return isInit()
				&& (arguments.isEmptyArgument() || arguments.hasOnlySelf());
	}

	public String getSignature() {
		return arguments.getSignature();
	}

	public int getNodeBodyIndent() {
		FunctionDef functionNode = getASTNode();
		IndentVisitor visitor = VisitorFactory.createVisitor(
				IndentVisitor.class, functionNode.body[0]);

		return visitor.getIndent();
	}

	public List<FunctionDefAdapter> getFunctions() {
		if (this.functions == null) {
			LocalFunctionDefVisitor visitor = null;
			visitor = VisitorFactory.createContextVisitor(
					LocalFunctionDefVisitor.class, this.getASTNode(),
					getModule(), this);

			this.functions = visitor.getAll();
		}
		return this.functions;
	}

	public List<SimpleAdapter> getAssignedVariables() {
		ScopeAssignedVisitor visitor = VisitorFactory.createContextVisitor(
				ScopeAssignedVisitor.class, getASTNode(), this.getModule(),
				this);
		return visitor.getAll();
	}
}
