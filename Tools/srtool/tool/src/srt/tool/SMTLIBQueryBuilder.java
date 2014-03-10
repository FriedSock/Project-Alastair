package srt.tool;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;

public class SMTLIBQueryBuilder {

	private ExprToSmtlibVisitor exprConverter;
	private CollectConstraintsVisitor constraints;
	private String queryString = "";
	private int ass = 0;

	public SMTLIBQueryBuilder(CollectConstraintsVisitor ccv) {
		this.constraints = ccv;
		this.exprConverter = new ExprToSmtlibVisitor();
	}

	public void buildQuery() {
		StringBuilder query = new StringBuilder();
		
		query.append("(set-logic QF_BV)\n"
				+ "(define-fun tobv32 ((p Bool)) (_ BitVec 32) (ite p (_ bv1 32) (_ bv0 32)))\n"
	   	        + "(define-fun tobool ((p (_ BitVec 32))) (Bool) (not (= p (_ bv0 32))))\n");

		for (String variableName : constraints.variableNames) {
			query.append("(declare-fun " + variableName + " () (_ BitVec 32))\n");
		}
		
		for (AssignStmt assStmt : constraints.transitionNodes) {
			String name = assStmt.getLhs().getName();
			String rhs = exprConverter.visit(assStmt.getRhs());
			query.append("(assert (= " + name + " " + rhs + "))\n");
		}
		
		StringBuilder closingBrackets = new StringBuilder();
		StringBuilder finalAssertion = new StringBuilder();
		StringBuilder propList = new StringBuilder();
		
		if (!constraints.propertyNodes.isEmpty()) {
			finalAssertion.append("(assert \n");
		
			int i = 0;
			for (AssertStmt asrtStmt : constraints.propertyNodes) {
				String expr = exprConverter.visit(asrtStmt.getCondition());
				String name = asrtStmt.getName() != null ? asrtStmt.getName() : ("prop" + i++);
				query.append("(define-fun " + name + " () Bool (not (tobool " + expr + ")))\n");
				finalAssertion.append("(or " + name + "\n");
				propList.append(" " + name);
				closingBrackets.append(")");
			}
			finalAssertion.append(closingBrackets.toString() + ")\n");
			query.append(finalAssertion);
		}

		query.append("(check-sat)\n");
		query.append("(get-value (" + propList + "))\n\n");
		queryString = query.toString();
	}

	public String getQuery() {
		return queryString;
	}

}
