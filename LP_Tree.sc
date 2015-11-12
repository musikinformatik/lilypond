/* ---------------------------------------------------------------------------------------------------------------
• LP_Node
--------------------------------------------------------------------------------------------------------------- */
LP_Node {
	var <children, <parent;
	addParent { |argParent|
		parent = argParent;
	}
	parents {
		^if (parent.isNil) { [] } {
			var temp, parents;
			parents = [parent];
			temp = parent;
			while { temp = temp.parent; temp.notNil } { parents = [temp] ++ parents };
			parents;
		};
	}
	nodeIndex {
		^this.root.nodes.do { |node, i| if (this == node) { ^i } };
	}
	atNodeIndex { |index|
		^this.root.nodes[index];
	}
	root {
		^if (parent.isNil) { this } { this.parents[0] };
	}
	depth {
		^if (parent.isNil) { 0 } { this.parents.size };
	}
	isRoot {
		^parent.isNil;
	}
	isContainer {
		^children.notNil;
	}
	isLeaf {
		^children.isNil;
	}
	siblings {
		^if (this.isRoot) { nil } { this.parent.children };
	}
	do { |func|
		func.(this);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Container
--------------------------------------------------------------------------------------------------------------- */
LP_Container : LP_Node {
	*new { |children|
		^super.new.init(children);
	}
	init { |argChildren|
		children = argChildren;
		children.do { |child| child.addParent(this) };
	}
	nodes {
		var array = [];
		this.do { |node| array = array.add(node) };
		^array;
	}
	leaves {
		var array = [];
		this.do { |node| if (node.isLeaf) { array = array.add(node) } };
		^array;
	}
	do { |func|
		func.(this);
		children.do { |child| child.do(func) };
	}
	at { |index|
		^children[index];
	}
	removeAt { |index|
		children.removeAt(index);
	}
	remove { |node|
		children.remove(node);
	}
	removeAll { |nodes|
		nodes.do { |node| this.remove(node) };
	}

	append { |newNode|
		children = children.add(newNode);
		newNode.addParent(this);
	}
	appendAll { |newNodes|
		newNodes.do { |node| this.append(node) };
	}
	insert { |index, newNode|
		children = children.insert(index, newNode);
		newNode.addParent(this);
	}
	insertAll { |index, newNodes|
		newNodes.do { |node, i|
			children = children.insert(index + i, node);
			node.addParent(this);
		};
	}
	put { |index, newNode|
		children = children.put(index, newNode);
		newNode.addParent(this);
	}
	putAll { |index, newNodes|
		children.removeAt(index);
		newNodes.do { |node, i|
			children = children.insert(index + i, node);
			node.addParent(this);
		};
	}
	replace { |oldNode, newNode|
		var index;
		index = children.indexOf(oldNode);
		this.put(index, newNode);
	}
	replaceAll { |oldNode, newNodes|
		var index;
		index = children.indexOf(oldNode);
		this.putAll(index, newNodes);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Leaf
--------------------------------------------------------------------------------------------------------------- */
LP_Leaf : LP_Node {
	remove {
		parent.remove(this);
	}
	replace { |newNode|
		parent.replace(this, newNode);
	}
	replaceAll { |newNodes|
		parent.replaceAll(this, newNodes);
	}
}