package srt.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import srt.ast.IntLiteral;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.util.LogicUtil;

public class PredicationVisitor extends DefaultVisitor {
	
	private int noPredicates = 0;
	private int noHavocs = 0;
	private int noGlobals = 0;
	private Stack<Expr> nestedConds = new Stack<>();
	private boolean firstAssignment = true;

	public PredicationVisitor() {
		super(true);
		nestedConds.push(new DeclRef("$G0"));
	}
	
	@Override
	public Object visit(IfStmt ifStmt) {
		BlockStmt thenBranch = handleBranch(ifStmt.getCondition(), ifStmt.getThenStmt());
		if (((StmtList) ifStmt.getElseStmt().getChildrenCopy().get(0)).getStatements().isEmpty()) {
			return thenBranch;
		}
		BlockStmt elseBranch = handleBranch(new UnaryExpr(UnaryExpr.LNOT, ifStmt.getCondition()), ifStmt.getElseStmt());
		
		List<Stmt> flattenedBranches = new ArrayList<>();
        flattenedBranches.addAll(thenBranch.getStmtList().getStatements());
        flattenedBranches.addAll(elseBranch.getStmtList().getStatements());
		
		return new BlockStmt(flattenedBranches);
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		Expr lhs = getPredicate();
		UnaryExpr rhs = new UnaryExpr(UnaryExpr.LNOT, assertStmt.getCondition());
		Expr expr = new UnaryExpr(UnaryExpr.LNOT, new BinaryExpr(BinaryExpr.LAND, lhs, rhs));
		
		return super.visit(new AssertStmt(expr, assertStmt.getName()));
	}

	@Override
	public Object visit(AssignStmt assignment) {
		//We do not want to predicate the initial assignment of the Global predicate.
		if (firstAssignment) {
			firstAssignment = false;
			return assignment;
		}

		Expr newRHS;
		if (assignment.getLhs().getName().startsWith("$G")) {
			int noGlobals = Integer.parseInt(assignment.getLhs().getName().substring(2)) - 1;
			newRHS = new TernaryExpr(getPredicate(), assignment.getRhs(), new DeclRef("$G" + noGlobals));
		} else {
			newRHS = new TernaryExpr(getPredicate(), assignment.getRhs(), assignment.getLhs());
		}
		Stmt newAssign = new AssignStmt(assignment.getLhs(), newRHS);
		return newAssign;
	}

	@Override
	public Object visit(AssumeStmt assumeStmt) {
		String oldGlobal = "$G" + noGlobals;
		Expr assumeExpr = new BinaryExpr(BinaryExpr.LAND, new DeclRef(oldGlobal), assumeStmt.getCondition());
		Stmt assignment = (Stmt) visit(new AssignStmt(new DeclRef("$G"+(noGlobals+1)), assumeExpr));

		noGlobals++;
		nestedConds.set(0, new DeclRef("$G" + noGlobals));
		return assignment;
	}

	@Override
	public Object visit(HavocStmt havocStmt) {
		String havocId = "h$" + noHavocs++;
		Decl declaration = new Decl(havocId, "int");
		Expr rhs = new TernaryExpr(getPredicate(), new DeclRef(havocId), havocStmt.getVariable());
		Stmt assignStmt = new AssignStmt(havocStmt.getVariable(), rhs);
		
		List<Stmt> statementList = new ArrayList<>();
		statementList.add(declaration);
		statementList.add(assignStmt);
		BlockStmt block = new BlockStmt(statementList);
		return super.visit(block);
	}
	
	@Override
	public Object visit(Program program) {
		BlockStmt block = program.getBlockStmt();
		Decl declaration = new Decl("$G0", "int");
		Stmt assignment = new AssignStmt(new DeclRef("$G0"), new IntLiteral(1));
		List<Stmt> statementList = Arrays.asList(new Stmt[]{declaration, assignment});
		List<Stmt> newStatementList = new ArrayList<>();
		newStatementList.addAll(statementList);
		newStatementList.addAll(block.getStmtList().getStatements());

		BlockStmt newBlock = new BlockStmt(newStatementList);
		Program newProgram = new Program(program.getFunctionName(), program.getDeclList(), newBlock, program.getNodeInfo());
		return super.visit(newProgram);
	}
	
	
	private BlockStmt handleBranch(Expr condition, Stmt body) {
		String name = "$P" + noPredicates++;
		Decl decl = new Decl(name, "int");
		
		DeclRef lhs = new DeclRef(name);
		List<Expr> conditions = new ArrayList<>();
		conditions.addAll(nestedConds);
		conditions.add(condition);
		Expr rhs = LogicUtil.conjoin(conditions);
		AssignStmt newAssign = new AssignStmt(lhs, rhs);
		
		nestedConds.push(lhs);
		Stmt newBody = (Stmt) visit(body);
		nestedConds.pop();
		
		return new BlockStmt(new Stmt[]{decl, newAssign, newBody});
	}
	
	private Expr getPredicate() {
		return new BinaryExpr(BinaryExpr.LAND, nestedConds.peek(), nestedConds.firstElement());
	}

}