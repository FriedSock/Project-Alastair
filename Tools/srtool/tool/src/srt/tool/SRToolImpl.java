package srt.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import srt.ast.BinaryExpr;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Node;
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
	
	private void print(Node node) {
		System.out.println(new PrinterVisitor().visit(node));
	}
	
	private Invariant toInvariant(Expr expr) {
		return new Invariant(true, expr);
	}

	public SRToolResult go() throws IOException, InterruptedException {
		
		if (clArgs.mode.equals(CLArgs.INVGEN)) {
			ComponentExtractorVisitor componentExtractor = new ComponentExtractorVisitor();
			componentExtractor.visit(program);
			
			Set<String> variableNames = componentExtractor.getVariableNames();
			Set<Integer> intLiterals = componentExtractor.getIntLiterals();
			
			intLiterals.add(0);  // 0 is good
			
			System.out.println("Variables:");
			for (String variable : variableNames) {
				System.out.println(variable);
			}
			
			System.out.println("Int literals:");
			for (Integer intLiteral : intLiterals) {
				System.out.println(intLiteral);
			}
			
			List<Invariant> commonInvariants = new ArrayList<>();
			
			// Generate all the things
			for (String variableA : variableNames) {
				DeclRef a = new DeclRef(variableA);
				
				for (String variableB : variableNames) {
					if (variableA.equals(variableB)) {
						continue;
					}
					
					DeclRef b = new DeclRef(variableB);
					
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, a, b)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.LEQ, a, b)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.EQUAL, a, b)));
				}
				
				for (Integer intLiteral : intLiterals) {
					IntLiteral n = new IntLiteral(intLiteral);
					
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.LT, a, n)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.LEQ, a, n)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.EQUAL, a, n)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.GEQ, a, n)));
					commonInvariants.add(toInvariant(new BinaryExpr(BinaryExpr.GT, a, n)));
				}
			}
			
			for (Invariant i : commonInvariants) {
				print(i.getExpr());
			}
			
			program = (Program) new AddCandidateInvariantsVisitor(commonInvariants).visit(program);
			
			System.out.println(new PrinterVisitor().visit(program));
			
			System.out.println("---");
		}

		if (clArgs.mode.equals(CLArgs.HOUDINI) || clArgs.mode.equals(CLArgs.INVGEN)) {
    		// Extract all loops
			HoudiniLoopExtractorVisitor loopExtractor = new HoudiniLoopExtractorVisitor();
			Program loopProgram = (Program) loopExtractor.visit(program);
			loopExtractor.setFirstPassFinished();
			
			if (!loopExtractor.noLoops()) {
				boolean atLeastOneCandidateFailed;
				List<Set<Integer>> preTrueCandidates = new ArrayList<>();
				List<Set<Integer>> postTrueCandidates = new ArrayList<>();
				int loopCount = loopExtractor.loopCount();
				for (int i = 0; i < loopCount; i++) {
					preTrueCandidates.add(new HashSet<Integer>());
					postTrueCandidates.add(new HashSet<Integer>());
				}
				
				do {
					// Reset what needs resetting
					loopExtractor.reset();
					atLeastOneCandidateFailed = false;
					preTrueCandidates = new ArrayList<>();
					postTrueCandidates = new ArrayList<>();
					for (int i = 0; i < loopCount; i++) {
						preTrueCandidates.add(new HashSet<Integer>());
						postTrueCandidates.add(new HashSet<Integer>());
					}
					
					Program newLoopProgram = (Program) new LoopAbstractionVisitor().visit(loopProgram);
					newLoopProgram = (Program) new PredicationVisitor().visit(newLoopProgram);
					newLoopProgram = (Program) new SSAVisitor().visit(newLoopProgram);

					String smtQuery = buildSMTQuery(newLoopProgram);
					String queryResult = solve(smtQuery);
					
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
							
							
							if (invariantName.startsWith("cand")) {
								if (matches[1].equals("true")) {
									atLeastOneCandidateFailed = true;
								} else if (matches[1].equals("false")) {
									String[] splitString = matches[0].split("-");
									int loopId = Integer.parseInt(splitString[1]);
									int invId = Integer.parseInt(splitString[2]);
									if(splitString[3].equals("pre")) {
										preTrueCandidates.get(loopId).add(invId);			
									} else if (splitString[3].equals("post")) {
										postTrueCandidates.get(loopId).add(invId);
									}

								}
							}
						}


						List<Set<Integer>> trueCandidates = new ArrayList<>();
						for(int i = 0; i < loopCount; i++) {
							postTrueCandidates.get(i).retainAll(preTrueCandidates.get(i));
							trueCandidates.add(postTrueCandidates.get(i));
						}
						loopExtractor.setCandidates(trueCandidates);
						loopProgram = (Program) loopExtractor.visit(program);
					}
	
				} while (atLeastOneCandidateFailed);

				program = (Program) new HoudiniReassemblerVisitor(loopExtractor.getAllInvariants()).visit(program);
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
