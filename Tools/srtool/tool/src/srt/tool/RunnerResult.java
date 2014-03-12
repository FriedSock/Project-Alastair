package srt.tool;

import srt.tool.SRTool.SRToolResult;

public class RunnerResult {
	
	public final SRToolResult result;
	public final CLArgs clArgs;

	public RunnerResult(SRToolResult result, CLArgs clArgs) {
		this.result = result;
		this.clArgs = clArgs;
	}
	
}
