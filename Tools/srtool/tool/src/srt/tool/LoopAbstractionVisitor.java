package srt.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.LogicUtil;

public class LoopAbstractionVisitor extends DefaultVisitor {
	
	Stack<Set<DeclRef>> modsets = new Stack<Set<DeclRef>>();

	public LoopAbstractionVisitor() {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		List<Stmt> statementList = new ArrayList<Stmt>();
		
		InvariantList invariants = whileStmt.getInvariantList();
		List<Expr> invariantExpressions = new ArrayList<>();
		for (Invariant invariant : invariants.getInvariants()) {
			invariantExpressions.add(invariant.getExpr());
		}
		
		boolean noInvariants = invariantExpressions.isEmpty();
		
		Expr invariantConjunction = new IntLiteral(1);
		AssertStmt invariantAssertion = null;
		if (!noInvariants) {
			invariantConjunction = LogicUtil.conjoin(invariantExpressions);
			invariantAssertion = new AssertStmt(invariantConjunction);
			statementList.add(invariantAssertion);
		}
		
		Set<DeclRef> currentModset = new HashSet<DeclRef>();
		modsets.add(currentModset);
		Object visitedBody = super.visit(whileStmt.getBody());
		modsets.pop();
		if (!modsets.isEmpty()) {
			modsets.peek().addAll(currentModset);
		}
		
		for (DeclRef declarationReference : currentModset) {
			statementList.add(new HavocStmt(declarationReference));
		}
		
		Stmt[] thenBranchStatements;
		if (!noInvariants) {
			statementList.add(new AssumeStmt(invariantConjunction));
			thenBranchStatements = new Stmt[]{(Stmt) visitedBody, invariantAssertion, new AssumeStmt(new IntLiteral(0))};
		} else {
			thenBranchStatements = new Stmt[]{(Stmt) visitedBody, new AssumeStmt(new IntLiteral(0))};
		}
		
		BlockStmt thenBranch = new BlockStmt(thenBranchStatements);

		statementList.add(new IfStmt(whileStmt.getCondition(), thenBranch, new BlockStmt(new Stmt[]{})));

		
		return new BlockStmt(statementList);
	}
	
	@Override
	public Object visit(AssignStmt assignStmt) {
		if (!modsets.isEmpty()) {
			modsets.peek().add(assignStmt.getLhs());
		}
		
		return super.visit(assignStmt);
	}

}