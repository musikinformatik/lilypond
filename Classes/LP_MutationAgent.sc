/* ---------------------------------------------------------------------------------------------------------------
• LP_MutationAgent: a wrapper around the LP mutate / mutation methods
- !!TODO: write the LP_MutationAgent class !!
--------------------------------------------------------------------------------------------------------------- */
+ LP_Selection {
	remove {
		if (components[0].isKindOf(LP_TieSelection)) {
			var tieSelection = components[0];
			if (tieSelection.isTieContainer) { tieSelection.root.remove(tieSelection.parent) };
		} {
			components.do { |component| component.parent.remove(component) };
		};
		this.changed;
	}
	replace { |newComponents|
		if (newComponents.isArray.not) { newComponents = [newComponents] };
		if (components[0].isKindOf(LP_TieSelection)) {
			components[0].replace(newComponents); // recursive - will only work for LP_TieContainer
		} {
			components[0].parent.replaceAll(components[0], newComponents);
			components[1..].do { |component| component.remove };
		};
		this.changed;
	}
	put { |index, component|
		components[0].root.put(index, component);
		this.changed;
	}
	putAll { |index, newComponents|
		components[0].root.putAll(index, newComponents);
		this.changed;
	}
	partition { |ratio, notes|
		if (this.componentsAreInSameParent) {
			var newComponents, component, tuplet;
			component = components[0];
			tuplet = LP_Tuplet(this.preProlatedDuration, ratio.collect { |dur|
				if (dur.isPositive) { component.shallowClone.preProlatedDuration_(dur) } { LP_Rest(dur) };
			});
			if (notes.notNil) { tuplet.notes_(notes) };
			this.replace(tuplet);
		} { error("Can only partition components that share the same parent.") };
	}
	replaceNotes { |notes|
		var note;
		if (notes.isString) { notes = LP_StringParser(notes) };
		if (notes.size < components.size) { notes = notes.extend(components.size, notes.last) };
		if (notes.size > components.size) { notes = notes[..components.lastIndex] };

		components.do { |component, i|
			note = notes[i];
			//!!! make this recursive
			case
			{ component.isKindOf(LP_TieSelection) && note.isKindOf(Array) } {
				component.components.do { |leaf| leaf.parent.replace(leaf, leaf.asChord.notes_(note)) };
			}
			{ component.isKindOf(LP_TieSelection) } {
				component.components.do { |leaf| leaf.parent.replace(leaf, leaf.asNote.note_(note)) };

			}
			{ component.isKindOf(LP_Leaf) && note.isKindOf(Array) } {
				component.parent.replace(component, component.asChord.notes_(note));
			}
			{ component.isKindOf(LP_Leaf) } {
				component.parent.replace(component, component.asNote.note_(note));
			};
		};
	}
	transpose { |intervals|
		if (intervals.isKindOf(Number)) { intervals = [intervals] };
		if (intervals.size < this.size) { intervals = intervals.extend(this.size, intervals.last) };
		this.replaceNotes(components.collect { |each, i| each.note + intervals[i] });
	}
	/* -----------------------------------------------------------------------------------------------------------
	• mask
	TODO:
	- add option for rewriting to beat structure following mask

	!!BUG:
	- cannot mask when mask span includes both Chords and Notes (and Rests?)
	a = LP_Staff([LP_Measure([4, 8], [1, 1, 1, 1], [62,[60,65,70],[60,65,71],63])]);
	a.selectBy(LP_PitchEvent).mask([1, 1, 1, 1]);
	a.selectBy(LP_PitchEvent).replaceNotes([62,[60,65,70],63]);
	LP_File(LP_Score([a])).write("test1.ly");


	!!BUG:
	a = LP_Staff([LP_Measure([4, 8], [1, 1, -1, 1])]);
	a.selectBy(LP_PitchEvent).mask([1, 2, 1]);
	a.selectBy(LP_PitchEvent).replaceNotes([62,[60,65,70],63]);
	LP_File(LP_Score([a])).write("test1.ly");
	----------------------------------------------------------------------------------------------------------- */
	mask { |maskVals|
		var indices, selectionInventory;
		indices =  (0..(maskVals.abs.sum - 1))[..components.lastIndex].clumps(maskVals.abs);
		selectionInventory = this.selectBy(LP_Event)[indices];
		selectionInventory.components.do { |sel, i| LP_ContiguousSelection(sel).fuse(maskVals[i].isNegative) };
	}
	/* -----------------------------------------------------------------------------------------------------------
	• fuse
	TODO:
	- only execute if selection.componentsAreContiguous && selection.componentsAreInSameLogicalVoice ?
	- move method to LP_ContiguousSelection ?
	- add option for rewriting to beat structure following fuse
	----------------------------------------------------------------------------------------------------------- */
	fuse { |fuseAsRest=false|
		if (fuseAsRest.not) { this.replaceNotes(components[0].note ! this.size) }; //!!! BROKEN for LP_Chords

		components = if (this.componentsAreInSameParent) {
			[this.prFuseLeavesInSameParent(fuseAsRest)];
		} {
			this.clumpByParentage.collect { |clump|
				LP_ContiguousSelection(clump).prFuseLeavesInSameParent(fuseAsRest);
			};
		};

		if (components.size > 1 && { fuseAsRest.not }) { LP_Selection(components).attach(LP_Tie()) };
	}
	prFuseLeavesInSameParent { |fuseAsRest=false|
		var firstLeaf, localSelection, newComponent;

		firstLeaf = components[0];
		localSelection = LP_Selection(components);

		//!!! TODO: refactor - a hack for associating ties with partitioned durs at the end of measures
		if (localSelection.last.isLastLeafInMeasure
			&& { LP_Duration.partitioned.includes(localSelection.preProlatedDuration) }) {
			localSelection.attach(LP_Tie());
		};

		newComponent = if (fuseAsRest) {
			LP_Rest(this.preProlatedDuration);
		} { firstLeaf.preProlatedDuration_(this.preProlatedDuration) };
		firstLeaf.parent.replace(firstLeaf, newComponent);
		if (this.size > 1) { firstLeaf.parent.removeAll(components[1..]) };
		// extract any redundant tuplets
		//!!! TODO: make this recursive, i.e. extracting parents of parents where needed
		if (newComponent.parent.isKindOf(LP_Tuplet) && { newComponent.parent.children.size == 1 }) {
			newComponent.preProlatedDuration_(newComponent.parent.preProlatedDuration);
			newComponent.parent.parent.replace(newComponent.parent, newComponent);
		};
		this.changed;
		^newComponent;
	}
	extractRedundantTuplets {
		//!!! TODO
	}
	/* -----------------------------------------------------------------------------------------------------------
	• beamStructure_

	a = LP_Staff([LP_Measure([4, 4], 1!8)]);
	b = a.deepCopy;
	b.selectBy(LP_Leaf).beamStructure_([3,3,2]);
	LP_File(LP_Score([a, b])).write;

	a = LP_Staff(LP_Measure([4, 4], 1!8)!2);
	b = a.deepCopy;
	b.selectBy(LP_Leaf).beamStructure_([3,3,5,2,3]);
	LP_File(LP_Score([a, b])).write;

	a = LP_Staff(LP_Measure([4, 4], 1!8)!2);
	b = a.deepCopy;
	b.selectBy(LP_Leaf)[2..].beamStructure_([3,5,2,3]);
	LP_File(LP_Score([a, b])).write;
	----------------------------------------------------------------------------------------------------------- */
	beamStructure_ { |beamStructure|
		var selections, spanners;
		components.do { |leaf|
			spanners = leaf.spanners;
			if (spanners.notNil) { spanners.removeAllSuchThat { |spanner| spanner.isKindOf(LP_Beam) } };
		};
		beamStructure = beamStructure.offsets.min(this.size).as(OrderedIdentitySet).asArray.intervals;
		selections = (0..(beamStructure.sum - 1)).clumps(beamStructure);
		selections = selections.collect { |indices| LP_Selection(components[indices]) };
		selections.do { |each|  each.attach(LP_Beam()) };
	}
}
