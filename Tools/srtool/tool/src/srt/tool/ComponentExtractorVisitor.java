package srt.tool;

import java.util.HashSet;
import java.util.Set;

import srt.ast.Decl;
import srt.ast.IntLiteral;
import srt.ast.visitor.impl.DefaultVisitor;

public class ComponentExtractorVisitor extends DefaultVisitor {
	
	private Set<String> variableNames = new HashSet<>();
	private Set<Integer> intLiterals = new HashSet<>();

	public ComponentExtractorVisitor() {
		super(false);
	}

	@Override
	public Object visit(Decl decl) {
		variableNames.add(decl.getName());
		return decl;
	}
	
	@Override
	public Object visit(IntLiteral intLiteral) {
		intLiterals.add(intLiteral.getValue());
		return intLiteral;
	}
	
	public Set<String> getVariableNames() {
		return variableNames;
	}
	
	public Set<Integer> getIntLiterals() {
		return intLiterals;
	}
}
