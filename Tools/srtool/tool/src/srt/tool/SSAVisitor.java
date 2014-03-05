package srt.tool;

import java.util.HashMap;
import java.util.Map;

import srt.ast.AssignStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.visitor.impl.DefaultVisitor;

public class SSAVisitor extends DefaultVisitor {
	
	private Map<String, Integer> noUsages = new HashMap<>();

	public SSAVisitor() {
		super(true);
	}

	@Override
	public Object visit(Decl decl) {
		noUsages.put(decl.getName(), 0);
	    String newName = decl.getName() + "$0";
		Decl newDecl = new Decl(newName, decl.getType(), decl.getNodeInfo());
		return super.visit(newDecl);
	}

	@Override
	public Object visit(DeclRef declRef) {
		if (declRef.getName().contains("$")) {
			return super.visit(declRef);
		}
	    String newName = declRef.getName() + "$" + noUsages.get(declRef.getName());
	    DeclRef newDeclRef = new DeclRef(newName, declRef.getNodeInfo());
		return super.visit(newDeclRef);
	}

	@Override
	public Object visit(AssignStmt assignment) {
		DeclRef lhs = assignment.getLhs();
	    String newName = lhs.getName() + "$" + (noUsages.get(lhs.getName()) + 1);
		AssignStmt newAssign = new AssignStmt(new DeclRef(newName, lhs.getNodeInfo()), assignment.getRhs());
		Object o = super.visit(newAssign);
		noUsages.put(lhs.getName(), noUsages.get(lhs.getName()) + 1);
		return o;
	}

}
