package srt.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclList;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    private List<Stmt> decls = new ArrayList<>();
    private List<WhileStmt> whileLoops = new ArrayList<>();

	public HoudiniLoopExtractorVisitor() {
		super(true);
	}
	
	@Override
	public Object visit(Decl decl) {
		decls.add(decl);
		return decl;
	}

    @Override
    public Object visit(WhileStmt whileStmt) {
        whileLoops.add(whileStmt);        
        
        return super.visit(whileStmt);
    }
    
    public List<Stmt> getDeclarations() {
    	return decls;
    }

    public List<WhileStmt> getWhileLoops() {
    	return whileLoops;
    }
}
