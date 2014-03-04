package srtester;

import java.io.File;

public class Test implements Comparable<Test> {

	public File file = null;
	public boolean goodTest = false;
	public Test(File file, boolean goodTest) {
		this.file = file;
		this.goodTest = goodTest;
	}
	
	public String getExpectedResult()
	{
		return goodTest ? Main.CORRECT : Main.INCORRECT;
	}

	@Override
	public int compareTo(Test o) {
		return file.compareTo(o.file);
	}
	
}
