package srt.tool;

import java.util.List;
import java.util.Map;
import java.util.Set;

import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniReassemblerVisitor extends DefaultVisitor {
    
    private List<Invariant> newInvariants;
    private boolean firstLoop = true;

	public HoudiniReassemblerVisitor(List<Invariant> newInvariants) {
		super(true);
		this.newInvariants = newInvariants;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		if (firstLoop) {
			whileStmt.getInvariantList().setInvariants(newInvariants);
			firstLoop = false;
		}
		return super.visit(whileStmt);
	}

}
