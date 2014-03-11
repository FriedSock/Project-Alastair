package srt.tool;

import java.util.ArrayList;
import java.util.List;

import srt.ast.BlockStmt;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class AddCandidateInvariantsVisitor extends DefaultVisitor {
	
	private List<Invariant> commonInvariants;

	public AddCandidateInvariantsVisitor(List<Invariant> commonInvariants) {
		super(true);
		this.commonInvariants = commonInvariants;
	}

	public Object visit(WhileStmt whileStmt) {
		List<Invariant> invariants = new ArrayList<>();
		invariants.addAll(whileStmt.getInvariantList().getInvariants());
		invariants.addAll(commonInvariants);
		
		return new WhileStmt(whileStmt.getCondition(), whileStmt.getBound(),
				new InvariantList(invariants), (BlockStmt) super.visit(whileStmt.getBody()));
	}
}
