package srt.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import srt.ast.BlockStmt;
import srt.ast.Decl;
import srt.ast.DeclList;
import srt.ast.Invariant;
import srt.ast.InvariantList;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.WhileStmt;
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
		
		PrinterVisitor v = new PrinterVisitor();

		if (clArgs.mode.equals(CLArgs.HOUDINI)) {
    		// Extract all loops
			HoudiniLoopExtractorVisitor loopExtractor = new HoudiniLoopExtractorVisitor();
			Program loopProgram = (Program) loopExtractor.visit(program);
			WhileStmt loop = loopExtractor.getLoop();
			
			List<Invariant> invariants = loop.getInvariantList().getInvariants();
			List<Invariant> validInvariants = new ArrayList<>();
			
			for (Invariant invariant : invariants) {
				if (!invariant.isCandidate()) {
					validInvariants.add(invariant);
					continue;
				}

				List<Invariant> invariantList = new ArrayList<>();
				invariantList.add(invariant);
				loop.getInvariantList().setInvariants(invariantList);
				
				Program newLoopProgram = (Program) new LoopAbstractionVisitor().visit(loopProgram);
				newLoopProgram = (Program) new PredicationVisitor().visit(newLoopProgram);
				newLoopProgram = (Program) new SSAVisitor().visit(newLoopProgram);
				
				String smtQuery = buildSMTQuery(newLoopProgram);
				String queryResult = solve(smtQuery);
				if (queryResult == null) {
					return SRToolResult.UNKNOWN;
				}
				
				//System.out.println(queryResult);

				if (queryResult.startsWith("unsat")) {
					validInvariants.add(invariant);
				}
			}
			
			
			//List<WhileStmt> whileLoops = loopExtractor.getWhileLoops();
			//List<WhileStmt> newWhileLoops = new ArrayList<>();
			//List<Stmt> declarations = loopExtractor.getDeclarations();
			//StmtList declarationStatements = new StmtList(declarations);
			
			/*for (WhileStmt loop : whileLoops) {
				List<Invariant> invariants = loop.getInvariantList().getInvariants();
				List<Invariant> validInvariants = new ArrayList<>();
				
				for (Invariant invariant : invariants) {
					if (!invariant.isCandidate()) {
						validInvariants.add(invariant);
						continue;
					}
					
					System.out.println(v.visit(invariant.getExpr()));
					
					InvariantList newInvariants = new InvariantList(new Invariant[] {invariant});
					WhileStmt newLoop = new WhileStmt(loop.getCondition(), loop.getBound(), newInvariants, loop.getBody());
					Program loopProgram = new Program(program.getFunctionName(), program.getDeclList(),
							new BlockStmt(new Stmt[] {new BlockStmt(declarationStatements), newLoop}));
					loopProgram = (Program) new LoopAbstractionVisitor().visit(loopProgram);
					loopProgram = (Program) new PredicationVisitor().visit(loopProgram);
					loopProgram = (Program) new SSAVisitor().visit(loopProgram);
					
					System.out.println(new PrinterVisitor().visit(loopProgram));

					String smtQuery = buildSMTQuery(loopProgram);
					String queryResult = solve(smtQuery);
					if (queryResult == null) {
						return SRToolResult.UNKNOWN;
					}
					
					System.out.println(queryResult);

					if (queryResult.startsWith("unsat")) {
						validInvariants.add(invariant);
					}
				}
				
				newWhileLoops.add(new WhileStmt(loop.getCondition(), loop.getBound(), new InvariantList(validInvariants), loop.getBody()));
			}*/
    		
    		program = (Program) new HoudiniReassemblerVisitor(validInvariants).visit(program);
		}
		
		if (clArgs.mode.equals(CLArgs.BMC)) {
			program = (Program) new LoopUnwinderVisitor(clArgs.unsoundBmc,
					clArgs.unwindDepth).visit(program);
		} else {
			program = (Program) new LoopAbstractionVisitor().visit(program);
		}

		program = (Program) new PredicationVisitor().visit(program);
		program = (Program) new SSAVisitor().visit(program);

		// Output the program as text after being transformed (for debugging).
		if (clArgs.verbose) {
			String programText = new PrinterVisitor().visit(program);
			System.out.println(programText);
		}

		String smtQuery = buildSMTQuery(program);
		
		// Output the query for debugging
		if (clArgs.verbose) {
			System.out.println(smtQuery);
		}

		String queryResult = solve(smtQuery);
		if (queryResult == null) {
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
	
	private String buildSMTQuery(Program program)  {
		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);

		SMTLIBQueryBuilder builder = new SMTLIBQueryBuilder(ccv);
		builder.buildQuery();

		return builder.getQuery();
	}
	
	private String solve(String smtQuery) throws InterruptedException, IOException {
		// Submit query to SMT solver.
		// You can use other solvers.
		// E.g. The command for cvc4 is: "cvc4", "--lang", "smt2"
		ProcessExec process = new ProcessExec("z3", "-smt2", "-in");
		try {
			return process.execute(smtQuery, clArgs.timeout);
		} catch (ProcessTimeoutException e) {
			if (clArgs.verbose) {
				System.out.println("Timeout!");
			}
			return null;
		}
	}
}
