package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.Invariant;

public class InvariantGenerator {

	/*
	 * Generates invariants given variable names and integer literals found in the program.
	 * 
	 * May modify the arguments.
	 */
	public static List<Invariant> generate(Set<String> variableNames, Set<Integer> intLiterals) {
		intLiterals.add(0);  // 0 is good

		List<Invariant> invariants = new ArrayList<>();
		
		for (String variableA : variableNames) {
			DeclRef a = new DeclRef(variableA);
			
			for (String variableB : variableNames) {
				if (variableA.equals(variableB)) {
					continue;
				}
				
				DeclRef b = new DeclRef(variableB);
				
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, a, b)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LEQ, a, b)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.EQUAL, a, b)));
			}
			
			for (Integer intLiteral : intLiterals) {
				IntLiteral n = new IntLiteral(intLiteral);
				
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, a, n)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LEQ, a, n)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.EQUAL, a, n)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.GEQ, a, n)));
				invariants.add(toInvariant(new BinaryExpr(BinaryExpr.GT, a, n)));
			}
			
			invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, new DeclRef("i"), new IntLiteral(300))));
			invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, new DeclRef("j"), new IntLiteral(300))));
			invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, new DeclRef("k"), new IntLiteral(200))));
			invariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, new DeclRef("l"), new IntLiteral(200))));
		}
		
		return invariants;
	}
	
	private static Invariant toInvariant(Expr expr) {
		return new Invariant(true, expr);
	}
}
