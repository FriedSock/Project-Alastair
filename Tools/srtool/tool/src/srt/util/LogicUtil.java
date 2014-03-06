package srt.util;

import java.util.List;

import srt.ast.BinaryExpr;
import srt.ast.Expr;

public class LogicUtil {
	
	public static Expr conjoin(List<Expr> elements) {
		if (elements.isEmpty()) {
			return null;
		}
		
		Expr result = elements.get(0);
		
		for (int i = 1; i < elements.size(); i++) {
			BinaryExpr binEx = new BinaryExpr(BinaryExpr.LAND, elements.get(i), result);
			result = binEx;
		}
		
		return result;
	}
}
