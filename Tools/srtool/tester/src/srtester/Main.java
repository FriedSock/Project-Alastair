package srtester;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Main {
	
	public static final String UNKNOWN = "unknown";
	public static final String CORRECT = "correct";
	public static final String INCORRECT = "incorrect";
	
	private static PrintStream nullPrintStream = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});
	
	private static void getTestsRecursive(File dir, Set<Test> res) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				getTestsRecursive(file, res);
				continue;
			}
			if (!file.getName().endsWith(".sc"))
				continue;
			
			String filePath = file.getPath();
			boolean goodTest = false;
			
			if(filePath.contains("_good"))
			{
				goodTest = true;
			}
			else if(!filePath.contains("_bad"))
			{
				throw new IllegalArgumentException("Test " + filePath + " does not contain '_good' or '_bad'.");
			}
			
			Test test = new Test(file, goodTest);
			res.add(test);
		}
	}

	private static String join(List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append("\""+s+"\"");
			sb.append(" ");
		}
		return sb.toString();
	}

	// e.g. tester tests/loopfree 1 -mode bmc -unsound
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		if(argsList.size() < 2)
		{
			System.err.println("Need at least 2 arguments: directoryOfTests justReportFailures.");
			System.exit(1);
		}
		System.out.println();
		System.out.println();
		System.out.println("Starting tests: tester/tester_run.sh " + join(argsList));
		
		File dir = new File(argsList.remove(0));
		Set<Test> tests = new TreeSet<Test>();
		getTestsRecursive(dir, tests);
		
		String onlyReportFailures = argsList.remove(0);
		
		PrintStream out = System.out;
		PrintStream failureOut = nullPrintStream;
		
		if(!onlyReportFailures.equals("0"))
		{
			// only report failures
			
			failureOut = out; 
			out = nullPrintStream;
		}
		
		final long startTime = System.nanoTime();
		final long numTests = tests.size();
		long testsPassed = 0;
		long testsFailed = 0;
		
		long points = 0;
		long maxPoints = 0;
		
		for(Test test : tests)
		{
			
			List<String> command = new ArrayList<String>();
			command.add("tool/srt_run.sh");
			command.add(test.file.getPath());
			command.addAll(argsList);
			
			out.println("-------------------------");
			out.println(join(command));
			
			ProcessExec process = new ProcessExec(command.toArray(new String[0]));
			String result = UNKNOWN;
			try {
				process.execute("", 60);
				String stdout = process.stdout.trim();
				if(stdout.endsWith(INCORRECT))
				{
					result = INCORRECT;
				}
				else if(stdout.endsWith(CORRECT))
				{
					result = CORRECT;
				}
			} catch (ProcessTimeoutException e) {
				out.println("Timeout!");
			}
			
			out.println("Tool result:     " + result);
			out.println("Expected result: " + test.getExpectedResult());
			
			if(!result.equals(test.getExpectedResult()))
			{
				failureOut.println("--FAILURE:-------------");
				failureOut.println(join(command));
				failureOut.println("Tool result:     " + result);
				failureOut.println("Expected result: " + test.getExpectedResult());
			}
			
			int pointsAdded = 0;
			
			if(result.equals(UNKNOWN))
			{
				testsFailed++;
			}
			else if(result.equals(INCORRECT))
			{
				if(result.equals(test.getExpectedResult()))
				{
					pointsAdded = 1;
					testsPassed++;
				}
				else
				{
					pointsAdded = -4;
					testsFailed++;
				}
			}
			else if(result.equals(CORRECT))
			{
				if(result.equals(test.getExpectedResult()))
				{
					pointsAdded = 2;
					testsPassed++;
				}
				else
				{
					pointsAdded = -8;
					testsPassed++;
				}
			}
			
			points += pointsAdded;
			out.println("Points added: " + pointsAdded);
			
			if(test.goodTest)
			{
				maxPoints += 2;
			}
			else
			{
				maxPoints += 1;
			}
		}
		
		final long endTime = System.nanoTime();
		
		out.println("-------------------------------");
		out.println("-------------------------------");
		out.println("# passed:       " + testsPassed);
		out.println("# tests:        " + numTests);
		if(numTests != testsPassed + testsFailed)
		{
			failureOut.println("WARNING: Number of tests does not match!");
		}
		out.println("---------");
		out.println("# points:       " + points);
		out.println("(max possible): " + maxPoints);
		out.println("---------");
		out.println("time:       " + ((double)(endTime - startTime)) / 1000000000.0 + " seconds");
		
	}
}
