package srt.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import srt.ast.BlockStmt;
import srt.ast.DeclList;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    private DeclList declList = null;
    private List<WhileStmt> whileLoops = new ArrayList<>();

	public HoudiniLoopExtractorVisitor() {
		super(true);
	}

    @Override
    public Object visit(Program program) {
        declList = program.getDeclList();
        
        super.visit(program);
        
        return program;
    }

    @Override
    public Object visit(WhileStmt whileStmt) {
        whileLoops.add(whileStmt);
        
        return super.visit(whileStmt);
    }
    
    public DeclList getDeclarations() {
    	return declList;
    }

    public List<WhileStmt> getWhileLoops() {
    	return whileLoops;
    }
}
