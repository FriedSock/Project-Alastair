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
import srt.ast.visitor.impl.PrinterVisitor;

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
        List<Invariant> invariantsToAdd = invariants != null ? invariants : whileStmt.getInvariantList().getInvariants();
        
    	List<Stmt> stmts = new ArrayList<>();
    	int inv = 0;
        int cand = 0;
        for (Invariant invariant : invariantsToAdd) {
        	if (invariant.isCandidate()) {
        		stmts.add(new AssertStmt(invariant.getExpr(), "cand-" + cand++ + "-pre"));
        	} else {
        		stmts.add(new AssertStmt(invariant.getExpr(), "inv-" + inv++ + "-pre"));	
        	}
        }
        
        inv = 0;
        cand = 0;
        List<Stmt> loopBodyStatements = new ArrayList<>();
        loopBodyStatements.add(whileStmt.getBody());
        for (Invariant invariant : invariantsToAdd) {
        	if (invariant.isCandidate()) {
        		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "cand-" + cand++ + "-post"));
        	} else {
        		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "inv-" + inv++ + "-post"));	
        	}
        }
    	
    	BlockStmt loopBody = new BlockStmt(loopBodyStatements);
    	WhileStmt loopStmt = new WhileStmt(whileStmt.getCondition(), whileStmt.getBound(), new InvariantList(invariantsToAdd), loopBody);
    	
        stmts.add(loopStmt);
        stmts.add(new AssumeStmt(new IntLiteral(0)));

        loop = loopStmt;
        
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
