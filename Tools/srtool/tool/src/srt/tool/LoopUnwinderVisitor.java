package srt.tool;

import java.util.ArrayList;
import java.util.List;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopUnwinderVisitor extends DefaultVisitor {

	private boolean unsound;
	private int defaultUnwindBound;

	public LoopUnwinderVisitor(boolean unsound,
			int defaultUnwindBound) {
		super(true);
		this.unsound = unsound;
		this.defaultUnwindBound = defaultUnwindBound;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		
		int bound = defaultUnwindBound;
		boolean defaultIsUsed = true;
		if (whileStmt.getBound() != null) {
			bound = whileStmt.getBound().getValue();
			defaultIsUsed = false;
		}

		Stmt loopBody = whileStmt.getBody();
		List<Invariant> invariants = whileStmt.getInvariantList().getInvariants();
		
		List<Stmt> invariantAssertList = new ArrayList<>();
		for(Invariant invariant : invariants) {
			AssertStmt assertStmt = new AssertStmt(invariant.getExpr());
			invariantAssertList.add(assertStmt);
		}

		Stmt endIfBranch;
		if (defaultIsUsed && !unsound) {
			endIfBranch = new BlockStmt(new Stmt[]{new AssertStmt(new IntLiteral(0)), new AssumeStmt(new IntLiteral(0))});
		} else {
			endIfBranch = new BlockStmt(new Stmt[]{new AssumeStmt(new IntLiteral(0))});
		}
			
		Stmt endElseBranch = new BlockStmt(new Stmt[0]);
		IfStmt ifStmt = new IfStmt(whileStmt.getCondition(), endIfBranch, endElseBranch);
				
		for (int i = 0; i < bound; i++) {
			List<Stmt> statementsList = new ArrayList<>();
			statementsList.addAll(((BlockStmt)loopBody).getStmtList().getStatements());
			statementsList.addAll(invariantAssertList);
			statementsList.add(ifStmt);
			
			Stmt ifBranch = new BlockStmt(statementsList);
			Stmt elseBranch = new BlockStmt(new Stmt[0]);
			ifStmt = new IfStmt(whileStmt.getCondition(), ifBranch, elseBranch);
		}
		
		List<Stmt> unwoundLoopStatementsList = new ArrayList<>();
		unwoundLoopStatementsList.addAll(invariantAssertList);
		unwoundLoopStatementsList.add(ifStmt);

		return new BlockStmt(unwoundLoopStatementsList);

	}

}
