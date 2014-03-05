package srt.tool;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class ExprToSmtlibVisitor extends DefaultVisitor {
	
	public ExprToSmtlibVisitor() {
		super(false);
	}

	@Override
	public String visit(BinaryExpr expr) {
		String operator = null;
		switch(expr.getOperator())
		{
			case BinaryExpr.ADD:
				operator = "(bvadd %s %s)";
				break;
			case BinaryExpr.BAND:
				operator = "(bvand %s %s)";
				break;
			case BinaryExpr.BOR:
				operator = "(bvor %s %s)";
				break;
			case BinaryExpr.BXOR:
				operator = "(bvxor %s %s)";
				break;
			case BinaryExpr.DIVIDE:
				operator = "(bvsdiv %s %s)";
				break;
			case BinaryExpr.LSHIFT:
				operator = "(bvshl %s %s)"; 
				break;
			case BinaryExpr.MOD:
				operator = "(bvsmod %s %s)";
				break;
			case BinaryExpr.MULTIPLY:
				operator = "(bvmul %s %s)";
				break;
			case BinaryExpr.RSHIFT:
				//TODO: test that this preserves sign.
				operator = "(bvashr %s %s)"; 
				break;
			case BinaryExpr.SUBTRACT:
				operator = "(bvsub %s %s)";
				break;
	
			case BinaryExpr.LAND:
				operator = "(and (tobool %s) (%s tobool))";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.LOR:
				operator = "(or (tobool %s) (tobool %s))";
				operator = "(tobv32 " + operator + ")";
				break;
				
			case BinaryExpr.GEQ:
				operator = "(bvsge %s %s)";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.GT:
				operator = "(bvsgt %s %s)";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.LEQ:
				operator = "(bvsle %s %s)";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.LT:
				operator = "(bvslt %s %s)";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.NEQUAL:
				operator = "(not (= %s %s))";
				operator = "(tobv32 " + operator + ")";
				break;
			case BinaryExpr.EQUAL:
				operator = "(= %s %s)";
				operator = "(tobv32 " + operator + ")";
				break;
			default:
				throw new IllegalArgumentException("Invalid binary operator");
		}
		
		return String.format(operator, visit(expr.getLhs()), visit(expr.getRhs()));
		
	}

	@Override
	public String visit(DeclRef declRef) {
		return declRef.getName();
	}

	@Override
	public String visit(IntLiteral intLiteral) {
		return "(_ bv" + intLiteral.getValue() + " 32)";
	}

	@Override
	public String visit(TernaryExpr ternaryExpr) {
		String cond = visit(ternaryExpr.getCondition());
		String thenBranch = visit(ternaryExpr.getTrueExpr());
		String elseBranch = visit(ternaryExpr.getFalseExpr());
		return "(ite (tobool " + cond + ") " + thenBranch + " " + elseBranch + ")";
	}

	@Override
	public String visit(UnaryExpr unaryExpr) {
		String operator = null;
		switch(unaryExpr.getOperator())
		{
		case UnaryExpr.UMINUS:
			operator = "(bvneg %s)";
			break;
		case UnaryExpr.UPLUS:
			operator = "%s";
			break;
		case UnaryExpr.LNOT:
			operator = "(tobv32 (not (tobool %s)))";
			break;
		case UnaryExpr.BNOT:
			operator = "(bvnot %s)";
			break;
		default:
			throw new IllegalArgumentException("Invalid binary operator");
		}
		
		return String.format(operator, visit(unaryExpr.getOperand()));
	}
	
	
	/* Overridden just to make return type String. 
	 * @see srt.ast.visitor.DefaultVisitor#visit(srt.ast.Expr)
	 */
	@Override
	public String visit(Expr expr) {
		return (String) super.visit(expr);
	}
	
	

}
