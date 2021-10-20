package io.metersphere.log.utils.diff.json;

class ObjNode extends Node {

	final String key;

	ObjNode(Node parent, String key) {
		super(parent);
		this.key = key;
	}
	
	@Override
	void rehash(Node newParent) {
		this.parent = newParent;
		this.parentHashCode = newParent.hashCode;
		int i = this.parentHashCode;

		i = i * 31 + ObjNode.class.hashCode();
		i = i * 31 + key.hashCode();
		hashCode = i;
	}
	

	@Override
	int doHash(boolean indexed) {

		// just pass through the arguments as is since
		// it's the arr node that alters them.
		int i = parent.doHash(indexed);

		i = i * 31 + ObjNode.class.hashCode();
		i = i * 31 + key.hashCode();
		return i;

	}

	@Override
	public String toString() {
		return key;
	}

}