package srt.tool;

import java.util.Map;
import java.util.Set;

import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniReassemblerVisitor extends DefaultVisitor {
    
    private Map<Program, InvariantList> whileLoops;

	public HoudiniReassemblerVisitor(Map<Program, InvariantList> wL) {
		super(true);
		whileLoops = wL;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
	    Set<Program> loops = whileLoops.keySet();
	    Stmt newLoop = null;
	    for (Program p : loops) {
	        Stmt loop = p.getBlockStmt();
	        // TODO ensure/make equals() works
	        if (whileStmt.equals(loop)) {
	            newLoop = loop;
	            break;
	        }
	    }
	    
	    return super.visit(newLoop); // recurse for inner loops, or cover this in the loop extractor?
	}

}
