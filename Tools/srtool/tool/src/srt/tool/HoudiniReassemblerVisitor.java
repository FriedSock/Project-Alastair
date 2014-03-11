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
    
    private List<List<Invariant>> newInvariantList;
    
    //Note: This needs to be reset *externally* every time the visitor does a pass.
    private int id;

	public HoudiniReassemblerVisitor(List<List<Invariant>> newInvariants) {
		super(true);
		this.newInvariantList = newInvariants;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		List<Invariant> newInvariants = newInvariantList.get(id); 
		whileStmt.getInvariantList().setInvariants(newInvariants);
		id++;
		return super.visit(whileStmt);
	}

}
