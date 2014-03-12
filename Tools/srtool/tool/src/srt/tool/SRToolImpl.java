package srt.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import srt.ast.Program;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;

	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
	}

	public SRToolResult go() throws IOException, InterruptedException {
		if (clArgs.mode.equals(CLArgs.COMP)) {
			
			List<Callable<RunnerResult>> tasks = new ArrayList<>();
			tasks.add(new Runner(program, createArgs(CLArgs.VERIFIER)));
			tasks.add(new Runner(program, createArgs(CLArgs.BMC, false)));
			tasks.add(new Runner(program, createArgs(CLArgs.HOUDINI)));
			tasks.add(new Runner(program, createArgs(CLArgs.INVGEN)));
			
			Executor ex = Executors.newFixedThreadPool(8); // 8 cores
			ExecutorCompletionService<RunnerResult> ecs = new ExecutorCompletionService<RunnerResult>(ex);
			for (Callable<RunnerResult> task : tasks) { 
			    ecs.submit(task);
			}
			
			int incorrect = 0;
			for (int i = 0; i < tasks.size(); i++) {
				try {
					RunnerResult taskResult = ecs.take().get();
					SRToolResult result = taskResult.result;
					
					if (result == SRToolResult.CORRECT) {
						return SRToolResult.CORRECT;
					} else if (result == SRToolResult.INCORRECT) {
						incorrect++;
						if (incorrect > 3) {
							return SRToolResult.INCORRECT;
						}
					}					
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
			return SRToolResult.UNKNOWN;
		}
		
		Runner runner = new Runner(program, clArgs);
		try {
			return runner.call().result;
		} catch (Exception e) {
			return SRToolResult.UNKNOWN;
		}
	}
	
	private CLArgs createArgs(String mode) {
		return createArgs(mode, false);
	}
	
	private CLArgs createArgs(String mode, boolean unsoundBMC) {
		try {
			CLArgs args = clArgs.clone();
			args.mode = mode;
			args.unsoundBmc = unsoundBMC;
			args.unwindDepth = 100;
			args.verbose = false;  // Override verbosity
			return args;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
