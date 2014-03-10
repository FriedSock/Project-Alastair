package srt.tool;

import java.util.List;
import java.util.Map;
import java.util.Set;

import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniReassemblerVisitor extends DefaultVisitor {
    
    private List<WhileStmt> newWhileLoops;
    private int loopCounter = 0;

	public HoudiniReassemblerVisitor(List<WhileStmt> newWhileLoops) {
		super(true);
		this.newWhileLoops = newWhileLoops;
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		WhileStmt loop = newWhileLoops.get(loopCounter++);
		return super.visit(loop);
	}

}
