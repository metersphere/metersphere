package io.metersphere.log.utils.diff.json;

abstract class Node implements Cloneable {

	// keep the original hash code since we'll be unsetting the parent leaf
	int hashCode, parentHashCode;

	Node parent;
	Leaf leaf;

	Node(Node parent) {
		this.parent = parent;
	}

	@Override
	protected Node clone() {
		try {
			return (Node) super.clone();
		} catch (Exception e) {
			return null;
		}
	}

	abstract int doHash(boolean indexed);
	
	abstract void rehash(Node newParent);

	@Override
	public int hashCode() {
		return doHash(false);
	}

	boolean isOrphan() {
		return hashCode != 0 && parent == null;
	}

	void orphan() {
		parent = null;
		if (leaf != null) {
			for (Leaf c : leaf.newStructure) {
				c.parent.orphan();
			}
			leaf.newStructure.clear();
		}
	}
}