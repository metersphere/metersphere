package io.metersphere.log.utils.diff.json;

class ArrNode extends Node {

	int index;

	ArrNode(Node parent, int index) {
		super(parent);
		this.index = index;
	}

	@Override
	void rehash(Node newParent) {
		this.parent = newParent;
		this.parentHashCode = newParent.hashCode;
		int i = this.parentHashCode;

		i = i * 31 + ArrNode.class.hashCode();
		hashCode = i;
	}

	@Override
	int doHash(boolean indexed) {

		// this must either be the first node in which case passing
		// false to lastArrNode must be correct, or it isn't
		// in which case passing false is also correct.
		int i = parent.doHash(indexed);

		i = i * 31 + ArrNode.class.hashCode();
		if (indexed) {
			int adjusted = index;
			i = i * 31 + adjusted;
		}
		return i;

	}

	@Override
	public String toString() {
		return "" + index;
	}
}