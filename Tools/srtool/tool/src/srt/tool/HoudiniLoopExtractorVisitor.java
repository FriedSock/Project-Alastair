package srt.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclList;
import srt.ast.IntLiteral;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    //private List<Stmt> decls = new ArrayList<>();
    //private List<WhileStmt> whileLoops = new ArrayList<>();
    private WhileStmt loop = null;

	public HoudiniLoopExtractorVisitor() {
		super(true);
	}
	
	/*@Override
	public Object visit(Decl decl) {
		decls.add(decl);
		return decl;
	}*/

    @Override
    public Object visit(WhileStmt whileStmt) {
        //whileLoops.add(whileStmt);
        
        WhileStmt loopStmt = (WhileStmt) super.visit(whileStmt);
        loop = loopStmt;
        return new BlockStmt(new Stmt[] {loopStmt, new AssumeStmt(new IntLiteral(0))});
    }
    
    /*public List<Stmt> getDeclarations() {
    	return decls;
    }*/

    /*public List<WhileStmt> getWhileLoops() {
    	return whileLoops;
    }*/
    
    public WhileStmt getLoop() {
    	return loop;
    }
}
