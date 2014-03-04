package srt.tool;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.Expr;

public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
	private CollectConstraintsVisitor constraints;
	private String queryString = "";

	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		this.constraints = ccv;
		this.exprConverter = new ExprToSmtlibVisitor();
	}

	public void buildQuery() {
		StringBuilder query = new StringBuilder();
		ExprToSmtlibVisitor exprToSmt = new ExprToSmtlibVisitor();
		
		query.append("(set-logic QF_BV)\n"
				+ "(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n"
	   	        + "(define-fun tobool ((p (_ BitVec 32))) (Bool) (not (= p (_ bv0 32))))\n");

		for (String variableName : constraints.variableNames) {
			query.append("(declare-fun " + variableName + " () (_ BitVec 32))\n");
		}
		
		for (AssignStmt assStmt : constraints.transitionNodes) {
			String name = assStmt.getLhs().getName();
			String rhs = exprToSmt.visit(assStmt.getRhs());
			query.append("(assert (= " + name + " " + rhs + "))\n");
		}
		
		StringBuilder closingBrackets = new StringBuilder();
		query.append("(assert \n");
		for (AssertStmt asrtStmt : constraints.propertyNodes) {
			String expr = exprToSmt.visit(asrtStmt.getCondition());
			query.append("(or (not (tobool " + expr + "))\n");
			closingBrackets.append(")");
		}
		query.append(closingBrackets.toString() + ")\n");


		query.append("(check-sat)\n");
		queryString = query.toString();
	}

	public String getQuery() {
		return queryString;
	}

}
