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
				operator = "(bvxor %s %s)";

				break;
			case BinaryExpr.LSHIFT:
				break;
			case BinaryExpr.MOD:
				break;
			case BinaryExpr.MULTIPLY:
				break;
			case BinaryExpr.RSHIFT:
				break;
			case BinaryExpr.SUBTRACT:
				break;
				
			case BinaryExpr.LAND:
				break;
			case BinaryExpr.LOR:
				break;
			
			case BinaryExpr.GEQ:
				break;
			case BinaryExpr.GT:
				break;
			case BinaryExpr.LEQ:
				break;
			case BinaryExpr.LT:
				break;
			case BinaryExpr.NEQUAL:
				break;
			case BinaryExpr.EQUAL:
				operator = "(= %s %s)";
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
		return null;
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
			//TODO
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
