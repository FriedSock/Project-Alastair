package srt.tool;

import java.io.IOException;

import srt.ast.Program;

public class SRToolImpl implements SRTool {
	private Program program;
	private CLArgs clArgs;

	public SRToolImpl(Program p, CLArgs clArgs) {
		this.program = p;
		this.clArgs = clArgs;
	}

	public SRToolResult go() throws IOException, InterruptedException {
		Runner runner = new Runner(program, clArgs);
		runner.run();
		return runner.getResult();
	}
}
