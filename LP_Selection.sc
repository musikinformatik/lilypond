//!!! TODO
LP_PitchEvent {}

//!!! TODO
LP_Event {}

//!!! TODO: rest-delimited LP_PitchEvent clumps; rename: LP_PitchEventGroup, LP_PitchEventChunk or similar ??
LP_Run {}

//!!! TODO
LP_SelectionInventory {
	var <selections;
	*new { |selections|
		^super.new.init(selections);
	}
	init { |argSelections|
		selections = argSelections;
	}
	at { |index|
		^selections[index];
	}
	// use doesNotUnderstand to perform LP_Selection methods on a collection of LP_Selections
	doesNotUnderstand { |selector ... args|
		selections.do { |selection| selection.perform(selector, *args) };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Selection
--------------------------------------------------------------------------------------------------------------- */
LP_Selection {
	var <components;
	*new { |components|
		^super.new.init(components);
	}
	init { |argComponents|
		components = argComponents;
		components.do { |component| this.addDependant(component.root) }; // root is each LP_Measure in the selection
	}
	//!!! set of dependants needs to be updated for each change to the content of components
	//!!! this avoids orphaned dependancy couplings
	at { |indices|
		if (indices.isNumber) { indices = [indices] };
		components = components.atAll(indices);
	}
	copySeries { |first, second, last|
		components = components.copySeries(first, second, last);
	}
	copyRange { |start, end|
		components = components.copyRange(start, end);
	}
	selectBy { |class|
		components = if (class.isArray) {
			components.select { |node| class.includes(node.class) }; //!!! BROKEN
			//components.postln;
		} {
			switch(class,
				LP_Leaf, {
					components = components.select { |node| node.isKindOf(LP_Leaf) }.asArray;
				},
				LP_Note, {
					components = components.select { |node| node.isKindOf(LP_Note) }.asArray;
				},
				LP_Chord, {
					components = components.select { |node| node.isKindOf(LP_Chord) }.asArray;
				},
				LP_TieContainer, {
					components = components.select { |node| node.isKindOf(LP_TieContainer) }.asArray;
				},
				LP_Rest, {
					components = components.select { |node| node.isKindOf(LP_Rest) }.asArray;
				},
				LP_Tuplet, {
					components = components.select { |node| node.isKindOf(LP_Tuplet) }.asArray;
				},
				LP_Measure, {
					components = components.select { |node| node.isKindOf(LP_Measure) }.asArray;
				},
				LP_Event, {
					var elems;
					elems = this.selectBy(LP_Leaf).components;
					elems = elems.separate { |leaf| leaf.isTiedToNext.not };
					elems = elems.collect { |elem|
						if (elem[0].isTiedToNext) { LP_TieSelection(elem) } { elem };
					}.flat;
					components = elems;
				},
				LP_PitchEvent, {
					var elems;
					elems = this.selectBy(LP_Event).components.reject { |item| item.type == LP_Rest };
					components = elems;
				},
				/* !! TODO
				a = LP_Measure([5, 16], [1, 2, 1, 4, -2, 3, 4, -1, 4, -3], [62, 63, 64, 65]);
				a.selectBy(LP_Run).selections.do { |each| each.components.postln };
				*/
				LP_Run, {
					var elems;
					elems = this.selectBy(LP_Event).components;
					elems = elems.separate { |a, b| a.isKindOf(LP_Rest) || b.isKindOf(LP_Rest) };
					elems = elems.reject { |elem| elem[0].isKindOf(LP_Rest) };
					elems = elems.collect { |elem| LP_ContiguousSelection(elem) };
					^LP_SelectionInventory(elems);
				}
			);
		};
	}
	attach { |attachment|
		if (attachment.isKindOf(LP_Spanner)) {
			if (attachment.isKindOf(LP_Tie)) {
				components.drop(-1).do { |each| each.isTiedToNext_(true) };
				components[1..].do { |each| each.isTiedToPrev_(true) };
			};
			components.do { |each| each.attach(attachment) };
			attachment.components_(components);
		} {
			components.do { |component| component.attach(attachment) };
		};
	}
	override { |key, value|
		components.do { |component| component.override(key, value) };
	}
	first {
		^components.first;
	}
	last {
		^components.last;
	}
	size {
		^components.size;
	}
	/* -----------------------------------------------------------------------------------------------------------
	• properties
	----------------------------------------------------------------------------------------------------------- */
	duration {
		^components.collect { |component| component.duration }.reduce('add');
	}
	preProlatedDuration {
		^components.collect { |component| component.preProlatedDuration }.sum;
	}
	beatDuration {
		^components.collect { |component| component.beatDuration }.sum;
	}
	/* -----------------------------------------------------------------------------------------------------------
	• componentsAreInSameParent
	!!! TODO: this is broken for LP_TieSelections (which may have more than one parent)
	b = LP_Staff(LP_Measure([5, 16], [1, 5, LP_Tuplet(2, [2, 1]), 1, 1, 1]) ! 2);
	b.selectBy(LP_Event).components.do { |e| e.indexInLogicalVoice.post; " ".post; e.postln }
	b.selectBy(LP_Event)[0..].componentsAreInSameParent;
	b.selectBy(LP_Event)[[1, 2]].componentsAreInSameParent;
	----------------------------------------------------------------------------------------------------------- */
	componentsAreInSameParent {
		components.doAdjacentPairs { |a, b| if (a.parent != b.parent) { ^false } };
		^true;
	}
	componentsAreInSameLogicalVoice {
		//!!! TODO
	}
	componentsAreSimultaneous {
		//!!! TODO
	}
	componentsAreInSameMeasure {
		var array;
		array = components.collect { |component| component.parents.select { |elem| elem.isKindOf(LP_Measure) } };
		^array;
	}
	componentsAreContiguous {
		//!!! TODO
	}
	isTieContainer {
		^(this.componentsAreInSameParent && components[0].parent.isKindOf(LP_TieContainer));
	}
	/* -----------------------------------------------------------------------------------------------------------
	• clumpByParentage: returns array where sub-arrays are defined by distinct parentage
	//!!! TODO: this is broken for LP_TieSelections (which have no single parent)
	b = LP_Measure([5, 16], [1, LP_Tuplet(2, [5, 1]), 1, 1, 1]);
	b.selectBy(LP_Leaf).clumpByParentage.printAll; "";
	----------------------------------------------------------------------------------------------------------- */
	clumpByParentage {
		^components.separate { |a, b| a.parent != b.parent };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_ContiguousSelection
--------------------------------------------------------------------------------------------------------------- */
LP_ContiguousSelection : LP_Selection {
	attach { |attachment|
		if (attachment.isKindOf(LP_Tie)) {
			components.drop(-1).do { |each| each.isTiedToNext_(true) };
			components[1..].do { |each| each.isTiedToPrev_(true) };
		};
		components.do { |each| each.attach(attachment) };
		attachment.components_(components);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_TieSelection
--------------------------------------------------------------------------------------------------------------- */
LP_TieSelection : LP_ContiguousSelection {
	*new { |components|
		^super.new(components);
	}
	isTied {
		^true;
	}
	isTiedToNext {
		^components.last.isTiedToNext;
	}
	isTiedToNext_ { |bool|
		components.last.isTiedToNext_(bool);
	}
	attach { |attachment|
		if (attachment.isKindOf(LP_Spanner)) {
			components.do { |component| component.attach(attachment) };
			attachment.components_(components);
		} {
			components[0].attach(attachment);
		};
	}
	clone {
		^components[0].clone;
	}
	shallowClone {
		^components[0].shallowClone;
	}
	cloneAttachmentsFrom { |leaf|
		components[0].cloneAttachmentsFrom(leaf);
	}

	// fake node-like behaviour
	parent {
		if (this.componentsAreInSameParent) { ^components[0].parent } {
			error("LP_TieSelection spans components with multiple parents.");
		}
	}
	parents {
		if (this.componentsAreInSameParent) { ^components[0].parents } {
			error("LP_TieSelection spans components with multiple parents.");
		}
	}
	root {
		if (this.componentsAreInSameParent) { ^components[0].root } {
			error("LP_TieSelection spans components with multiple parents.");
		}
	}
	preProlatedDuration_ { |dur|
		components[0].preProlatedDuration_(dur);
		if (this.size > 1) { LP_Selection(components[1..]).remove };
	}
	note_ { |note|
		this.replaceNotes(note ! this.size);
	}
	note {
		^components[0].note;
	}
	notes_ { |notes|
		components.do { |component| component.notes_(notes) };
	}
	dynamic {
		^components[0].dynamic;
	}
	detachTie {
		if (this.size == 1) { ^components[0].detachTie };
	}
	// for use in LP_Player
	type {
		^components[0].class;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• TODO: LP_SliceSelection
--------------------------------------------------------------------------------------------------------------- */
LP_SliceSelection {
}
/* ---------------------------------------------------------------------------------------------------------------
• TODO: LP_VerticalMoment
--------------------------------------------------------------------------------------------------------------- */
LP_VerticalMoment {
}
