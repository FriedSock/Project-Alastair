package srt.tool;

import java.util.Collection;
import java.util.Stack;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class PredicationVisitor extends DefaultVisitor {
	
	private int noPredicates = 0;
	private Stack<Expr> nestedConds = new Stack<>();

	public PredicationVisitor() {
		super(true);
	}
	
	@Override
	public Object visit(IfStmt ifStmt) {
		Stmt thenBranch = handleBranch(ifStmt.getCondition(), ifStmt.getThenStmt());
		if (((StmtList) ifStmt.getElseStmt().getChildrenCopy().get(0)).getStatements().isEmpty()) {
			return new BlockStmt(new Stmt[]{thenBranch});
		}
		Stmt elseBranch = handleBranch(new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition()), ifStmt.getElseStmt());
		
		return new BlockStmt(new Stmt[]{thenBranch, elseBranch});
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		if (nestedConds.isEmpty()) {
			return super.visit(assertStmt);
		}
		
		Expr lhs = nestedConds.peek(); 
		UnaryExpr rhs = new UnaryExpr(UnaryExpr.LNOT, assertStmt.getCondition());
		Expr expr = new UnaryExpr(UnaryExpr.LNOT, new BinaryExpr(BinaryExpr.LAND, lhs, rhs));
		
		return super.visit(new AssertStmt(expr));
	}

	@Override
	public Object visit(AssignStmt assignment) {
		if (nestedConds.isEmpty()) {
			return super.visit(assignment);
		}
		
		Expr newRHS = new TernaryExpr(nestedConds.peek(), assignment.getRhs(), assignment.getLhs());
		Stmt newAssign = new AssignStmt(assignment.getLhs(), newRHS); 
		return newAssign;
	}

	@Override
	public Object visit(AssumeStmt assumeStmt) {
		return super.visit(assumeStmt);
	}

	@Override
	public Object visit(HavocStmt havocStmt) {
		return super.visit(havocStmt);
	}
	
	private Stmt handleBranch(Expr condition, Stmt body) {
		String name = "$P" + noPredicates++;
		Decl decl = new Decl(name, "int");
		
		DeclRef lhs = new DeclRef(name);
		Expr rhs = joinWithAnd(nestedConds, condition);
		AssignStmt newAssign = new AssignStmt(lhs, rhs);
		
		nestedConds.push(lhs);
		Stmt newBody = (Stmt) visit(body);
		nestedConds.pop();
		
		return new BlockStmt(new Stmt[]{decl, newAssign, newBody});
	}
	
	private Expr joinWithAnd(Collection<Expr> elements, Expr finalElem) {
		Expr result = finalElem;
		
		for (Expr elem : elements) {
			BinaryExpr binEx = new BinaryExpr(BinaryExpr.LAND, elem, result);
			result = binEx;
		}
		
		return result;
	}

}