package srt.ast;

public class DeclRef extends Expr {
	private String name;

	public DeclRef(String name) {
		this(name, null);
	}
	
	public DeclRef(String name, NodeInfo nodeInfo) {
		super(nodeInfo);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DeclRef)) {
			return false;
		}
		
		DeclRef other = (DeclRef) o;
		return name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
