package srt.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
			
			if (loop != null) {
				List<Invariant> invariants = loop.getInvariantList().getInvariants();
				List<Invariant> validInvariants = new ArrayList<>();
				List<Invariant> candidateInvariants = new ArrayList<>();

				for (Invariant invariant : invariants) {
					if (invariant.isCandidate()) {
						candidateInvariants.add(invariant);
					} else {
						validInvariants.add(invariant);
					}
				}
				
				

				TreeSet<Integer> invalidInvariants = new TreeSet<>();
				do {
					invalidInvariants.clear();
					
					//loop.getInvariantList().setInvariants(candidateInvariants);

					Program newLoopProgram = (Program) new LoopAbstractionVisitor().visit(loopProgram);

					newLoopProgram = (Program) new PredicationVisitor().visit(newLoopProgram);
					newLoopProgram = (Program) new SSAVisitor().visit(newLoopProgram);

					//System.out.println(v.visit(newLoopProgram));
					
					//System.out.println("CANDIDATES:");
					for(Invariant inv : candidateInvariants) {
						//System.out.println(v.visit(inv.getExpr()));
					}

					String smtQuery = buildSMTQuery(newLoopProgram);
					String queryResult = solve(smtQuery);
					//System.out.println(smtQuery);
					//System.out.println(queryResult);
					if (queryResult == null) {
						return SRToolResult.UNKNOWN;
					}

					if (!queryResult.startsWith("unsat")) {
						Pattern p = Pattern.compile("([\\w-]+ \\w+)");
						Matcher m = p.matcher(queryResult);
						while (m.find()) { // find next match
							String match = m.group();
							String[] matches = match.split(" ");
							String invariantName = matches[0];
							if (matches[1].equals("true") && invariantName.startsWith("cand")) {
								int invalidInvariant = Integer.parseInt(invariantName.split("-")[1]);
								invalidInvariants.add(invalidInvariant);
							}
						}

						for (int invariantIndex : invalidInvariants.descendingSet()) {
							candidateInvariants.remove(invariantIndex);
						}
					}
					//System.out.println(candidateInvariants);
					//System.out.println(invalidInvariants);

					List<Invariant> newInvariants = new ArrayList<>();
					newInvariants.addAll(validInvariants);
					newInvariants.addAll(candidateInvariants);
					loopExtractor.setInvariants(newInvariants);
					loopProgram = (Program) loopExtractor.visit(program);
					
					for(Invariant inv : newInvariants) {
						//System.out.println(v.visit(inv.getExpr()));
					}
					
					//System.out.println(new PrinterVisitor().visit(loopProgram));
				} while (!invalidInvariants.isEmpty());

				validInvariants.addAll(candidateInvariants);

				for (Invariant i : validInvariants) {
					//System.out.println(new PrinterVisitor().visit(i.getExpr()));
				}

				program = (Program) new HoudiniReassemblerVisitor(validInvariants).visit(program);
			}
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
