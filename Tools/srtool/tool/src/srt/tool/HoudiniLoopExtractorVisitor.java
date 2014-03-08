package srt.tool;

import java.util.HashMap;
import java.util.Map;

import srt.ast.BlockStmt;
import srt.ast.DeclList;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    private String functionName = null;
    private DeclList declList = null;
    private Map<Program, InvariantList> whileLoops = new HashMap<Program, InvariantList>();

	public HoudiniLoopExtractorVisitor() {
		super(true);
	}

    @Override
    public Object visit(Program program) {
        functionName = program.getFunctionName();
        declList = program.getDeclList();
        
        super.visit(program); // step through program, adding (declList + while loops) to whileLoops
        
        return whileLoops;
    }

    @Override
    public Object visit(WhileStmt whileStmt) {
        BlockStmt blockStmt = new BlockStmt(new Stmt[]{whileStmt});
        Program p = new Program(functionName, declList, blockStmt);
        whileLoops.put(p, whileStmt.getInvariantList());
        
        return super.visit(whileStmt); // recurse for inner loops, or cover this in the reassembler?
    }

}
