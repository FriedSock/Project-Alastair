package srt.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import srt.ast.AssertStmt;
import srt.ast.BlockStmt;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;
import srt.ast.visitor.impl.PrinterVisitor;

public class HoudiniLoopExtractorVisitor extends DefaultVisitor {
    
    private int id = 0;
    private List<List<Invariant>> candidateLoopInvariants = new ArrayList<>();
    private List<List<Invariant>> loopInvariants = new ArrayList<>();
    
    private boolean firstPass = true;
    //private List<Invariant> invariants = null;

	public HoudiniLoopExtractorVisitor() {
		super(true);
	}

    @Override
    public Object visit(WhileStmt whileStmt) {
    	if (firstPass) {
    		List<Invariant> candidates = new ArrayList<Invariant>();
    		List<Invariant> invariants = new ArrayList<Invariant>();
    		for(Invariant invariant : whileStmt.getInvariantList().getInvariants()) {
    			if (invariant.isCandidate()) {
            		candidates.add(invariant);
            	} else {
            		invariants.add(invariant);
            	}
    		}
    		loopInvariants.add(invariants);
        	candidateLoopInvariants.add(candidates);
    	}
    	
    	/* System.out.println("loopInvariants");
    	for (List<Invariant> l : loopInvariants) {
    		for (Invariant i : l) {
    			System.out.println(new PrinterVisitor().visit(i.getExpr()));
    		}
    	}

    	System.out.println("candidateLoopInvariants");
    	for (List<Invariant> l : candidateLoopInvariants) {
    		for (Invariant i : l) {
    			System.out.println(new PrinterVisitor().visit(i.getExpr()));
    		}
    	}*/
    	
    	// Set new id for next loop to be visited
    	int localId = id++;

    	List<Stmt> stmts = new ArrayList<>();
    	int inv = 0;
    	int cand = 0;
    	
    	for (Invariant invariant : loopInvariants.get(localId)) {
    		stmts.add(new AssertStmt(invariant.getExpr(), "inv-" + localId + "-" + inv++ + "-pre"));
    	}
    	for (Invariant invariant : candidateLoopInvariants.get(localId)) {
    		stmts.add(new AssertStmt(invariant.getExpr(), "cand-" + localId + "-" + cand++ + "-pre"));	
    	}
        
        //Extract nested loops
        BlockStmt body = (BlockStmt) super.visit(whileStmt.getBody());
        
        inv = 0;
        cand = 0;
        List<Stmt> loopBodyStatements = new ArrayList<>();
        loopBodyStatements.add(body);
    	for (Invariant invariant : loopInvariants.get(localId)) {
    		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "inv-" + localId + "-" + inv++ + "-post"));
    	}
    	for (Invariant invariant : candidateLoopInvariants.get(localId)) {
    		loopBodyStatements.add(new AssertStmt(invariant.getExpr(), "cand-" + localId + "-" + cand++ + "-post"));	
    	}
    	
    	List<Invariant> invariantsToAdd = new ArrayList<>();
    	invariantsToAdd.addAll(loopInvariants.get(localId));
    	invariantsToAdd.addAll(candidateLoopInvariants.get(localId));
    	
    	BlockStmt loopBody = new BlockStmt(loopBodyStatements);
    	WhileStmt loopStmt = new WhileStmt(whileStmt.getCondition(), whileStmt.getBound(), new InvariantList(invariantsToAdd), loopBody);

        stmts.add(loopStmt);
        //stmts.add(new AssumeStmt(new IntLiteral(0)));

        return new BlockStmt(stmts);
    }
    
    public void setFirstPassFinished() {
    	this.firstPass = false;
    }
    
    public List<Invariant> getCandidateInvariants() {
    	List<Invariant> invariants = new ArrayList<>();
    	for (List<Invariant> candidates : candidateLoopInvariants) {
    		invariants.addAll(candidates);
    	}
    	return invariants;
    }
    
    public void setCandidates(List<Set<Integer>> candidates) {
    	List<List<Invariant>> newCandidates = new ArrayList<>();
    	
    	int i = 0;
    	for (Set<Integer> validCandidateIndices : candidates) {
    		List<Invariant> currentCandidates = new ArrayList<Invariant>();
    		for (Integer validIndex : validCandidateIndices) {
    			currentCandidates.add(candidateLoopInvariants.get(i).get(validIndex));
    		}
    		newCandidates.add(currentCandidates);
    		i++;
    	}
    	candidateLoopInvariants = newCandidates;
    }
    
    public List<List<Invariant>> getAllInvariants() {
    	List<List<Invariant>> invariants = new ArrayList<>();
    	
    	for (int i = 0; i < loopInvariants.size(); i++) {
    		List<Invariant> currentInvariants = new ArrayList<>();
    		currentInvariants.addAll(loopInvariants.get(i));
    		currentInvariants.addAll(candidateLoopInvariants.get(i));
    		invariants.add(currentInvariants);
    	}
    	
    	return invariants;
    }
    
    public Boolean noLoops() {
    	return loopInvariants.isEmpty();
    }
    
    //Should only be called after the visitor has finished.
    public int loopCount () {
    	return id;
    }
	
	public void reset() {
		id = 0;
	}
}
