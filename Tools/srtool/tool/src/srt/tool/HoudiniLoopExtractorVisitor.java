package srt.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclList;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    //private List<Stmt> decls = new ArrayList<>();
    //private List<WhileStmt> whileLoops = new ArrayList<>();
    private WhileStmt loop = null;
    
    private List<Invariant> invariants = null;

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
        
    	List<Stmt> stmts = new ArrayList<>();
        WhileStmt loopStmt = (WhileStmt) super.visit(whileStmt);
        loop = loopStmt;
        
        int i = 0;
        List<Invariant> invariantsToAdd = invariants != null ? invariants : loopStmt.getInvariantList().getInvariants();
        for (Invariant invariant : invariantsToAdd) {
        	stmts.add(new AssertStmt(invariant.getExpr(), "inv-" + i++ + "-pre"));
        }
        stmts.add(loopStmt);
        i = 0;
        for (Invariant invariant : invariantsToAdd) {
        	stmts.add(new AssertStmt(invariant.getExpr(), "inv-" + i++ + "-post"));
        }
        stmts.add(new AssumeStmt(new IntLiteral(0)));
        return new BlockStmt(stmts);
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
    
    public void setInvariants(List<Invariant> invariants) {
    	this.invariants = invariants;
    }
}
