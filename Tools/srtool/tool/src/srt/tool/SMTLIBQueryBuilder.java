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
		StringBuilder finalAssertion = new StringBuilder();
		StringBuilder propList = new StringBuilder();
		finalAssertion.append("(assert \n");
		int i = 0;
		for (AssertStmt asrtStmt : constraints.propertyNodes) {
			String expr = exprToSmt.visit(asrtStmt.getCondition());
			query.append("(define-fun prop" + i + " () Bool (not (tobool " + expr + ")))\n");
			finalAssertion.append("(or prop" + i + "\n");
			propList.append(" prop" + i++);
			closingBrackets.append(")");
		}
		finalAssertion.append(closingBrackets.toString() + ")\n");
		query.append(finalAssertion);

		query.append("(check-sat)\n");
		query.append("(get-value (" + propList + "))\n\n");
		queryString = query.toString();
	}

	public String getQuery() {
		return queryString;
	}

}
