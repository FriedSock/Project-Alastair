package srt.tool;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.visitor.impl.PrinterVisitor;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;

	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
	}

	public SRToolResult go() throws IOException, InterruptedException {

		if (clArgs.mode.equals(CLArgs.BMC)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else {
			program = (Program) new LoopAbstractionVisitor().visit(program);
		}
		
		if (clArgs.mode.equals(CLArgs.HOUDINI)) {
    		//extract all loops
    		Map<Program, InvariantList> whileLoops = (Map<Program, InvariantList>) new HoudiniLoopExtractorVisitor().visit(program);
    		
    		for (Entry<Program, InvariantList> singleLoop : whileLoops.entrySet()) {
                boolean houdiniFails = true;
                List<Invariant> currentInvariants = singleLoop.getValue().getInvariants();
        		
        		while (houdiniFails) {
        	        // Collect the constraint expressions and variable names.
        	        CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
        	        ccv.visit(singleLoop.getKey());
        	        
        	        SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
        	        builder.buildQuery();
        	        
        	        String smtQuery = builder.getQuery();
        	        
        	        ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
        	        String queryResult = "";
        	        try {
        	            queryResult = process.execute(smtQuery, clArgs.timeout);
        	        } catch (ProcessTimeoutException e) {
        	            if (clArgs.verbose) {
        	                System.out.println("Timeout!");
        	            }
        	            return SRToolResult.UNKNOWN;
        	        }
        	        
        	        if (queryResult.startsWith("unsat")) {
        	            houdiniFails = false;
        	        } else if (queryResult.startsWith("sat")) {
        	            // TODO find failing assertions from queryResult?
        	            
        	            // how do we find out what eg. "prop0" actually is so we can remove the invariant? 
        	            
        	            // TODO update currentInvariants accordingly
        	            
        	            // TODO alter the program (and whileLoops) to reflect currentInvariants
        	            
        	            
        	        }
        		}
    		}
    		
    		program = (Program) new HoudiniReassemblerVisitor(whileLoops).visit(program);
		}
		
		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);

		// Output the program as text after being transformed (for debugging).
		if (clArgs.verbose) {
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}

		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);
		
		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
		builder.buildQuery();
		
		String smtQuery = builder.getQuery();
		
		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}

		// Submit query to SMT solver.
		// You can use other solvers.
		// E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		String queryResult = "";
		try {
			queryResult = process.execute(smtQuery, clArgs.timeout);
		} catch (ProcessTimeoutException e) {
			if (clArgs.verbose) {
				System.out.println("Timeout!");
			}
			return SRToolResult.UNKNOWN;
		}

		// output query result for debugging
		if (clArgs.verbose) {
			System.out.println(queryResult);
		}

		if (queryResult.startsWith("unsat")) {
			return SRToolResult.CORRECT;
		}

		if (queryResult.startsWith("sat")) {
			return SRToolResult.INCORRECT;
		}
		// query result started with something other than "sat" or "unsat"
		return SRToolResult.UNKNOWN;
	}
}
