/* ---------------------------------------------------------------------------------------------------------------
• LP_RhythmTreeContainer
--------------------------------------------------------------------------------------------------------------- */
LP_RhythmTreeContainer : LP_Container {
	var <preProlatedDuration, <duration;
	var <isTuplet, <durations, <tupletNumerator, <tupletDenominator;
	*new { |duration, children|
		children = children.collect { |child|
			case
			{ child.isNumber && { child > 0 } } { LP_Note(60, child.asInteger).isTiedToPrev_(child.isFloat) }
			{ child.isNumber && { child < 0 } } { LP_Rest(child.asInteger) }
			{ child.isArray && { child[1].isArray } } { LP_Tuplet(*child) } // allow rtmtree syntax
			{ child }; // child is already wrapped in LP_Note, LP_Chord, LP_Rest or LP_Tuplet
		};
		^super.new(children).init2(duration);
	}
	init1 { |argDuration|
		duration = if (this.isRoot) { preProlatedDuration } { argDuration };
		this.prolateInnerDurations(duration, children);
		children.do { |child, i| child.init1(durations[i]) };
	}
	init2 { |duration|
		preProlatedDuration = duration ? 1;
		// second call to init1 allows for recalculation of durs after insertion of LP_TieContainers
		if (duration.isKindOf(LP_Duration)) { this.init1(duration).init1 };
	}
	// assign: durations, isTuplet, tupletNumerator, tupletDenominator
	prolateInnerDurations { |argDuration, children|
		var preProlatedDurations, baseDenominator, tupletRatio;

		preProlatedDurations = children.collect { |child| child.preProlatedDuration };
		tupletNumerator = preProlatedDurations.sum;
		tupletDenominator = argDuration.numerator;

		if (tupletNumerator == 1) {
			durations = [argDuration];
			isTuplet = false;
		} {
			if (tupletNumerator < tupletDenominator) {
				while { tupletNumerator <= tupletDenominator } {
					preProlatedDurations = preProlatedDurations * 2;
					tupletNumerator = tupletNumerator * 2;
				};
			} {
				while { tupletDenominator <= (tupletNumerator / 2) } {
					tupletDenominator = tupletDenominator * 2;
				};
			};
			baseDenominator = ((argDuration.denominator * tupletDenominator) / argDuration.numerator).asInteger;
			durations = preProlatedDurations.collect { |num| LP_Duration(num, baseDenominator) };
			isTuplet = (tupletNumerator != tupletDenominator);
		};

		tupletRatio = [tupletNumerator, tupletDenominator];
		# tupletNumerator, tupletDenominator = (tupletRatio / tupletRatio.reduce(\gcd)).asInteger;
	}
	// repeated call on init1 allows for the late addition of LP_TieContainers
	update {
		this.init1.init1;
	}
	// override dup: music trees must always creates a new instance of the copied object
	dup { |n=2|
		^Array.fill(n, { this.deepCopy });
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_RhythmTreeLeaf
--------------------------------------------------------------------------------------------------------------- */
LP_RhythmTreeLeaf : LP_Leaf {
	var <>preProlatedDuration, <duration, <writtenDuration, <>isTiedToNext=false, <>isTiedToPrev=false;
	var <indicators, <spanners, <markups, <overrides; //!!! move up to LP_Object and inherit
	init1 { |argDuration|
		this.duration_(argDuration);
		if (this.isTiedToPrev) { this.prevLeaf.isTiedToNext_(true) };
	}
	duration_ { |argDuration|
		duration = argDuration; //!!! remove
		writtenDuration = argDuration;
		if (duration.isAssignable.not && { this.root != this }) { this.replace(LP_TieContainer(this)) };
	}
	clone {
		^this.deepCopy;
	}
	shallowClone {
		^this.deepCopy.removeAttachments;
	}
	cloneAttachmentsFrom { |leaf|
		leaf.indicators.do { |attachment| if (attachment.notNil) { this.attach(attachment) } };
		leaf.markups.do { |attachment| if (attachment.notNil) { this.attach(attachment) } };
		leaf.spanners.do { |attachment| if (attachment.notNil) { this.attach(attachment) } };
		this.isTiedToNext_(leaf.isTiedToNext).isTiedToPrev_(leaf.isTiedToPrev);
	}
	//!!! move up to LP_Object and inherit
	removeAttachments {
		indicators = markups = spanners = nil;
	}
	//!!! move up to LP_Object and inherit
	attach { |attachment|
		if (attachment.isKindOf(LP_Indicator)) {
			indicators = indicators.add(attachment);
			//!!! if (attachment.isKindOf(LP_Dynamic)) { this.dynamic_(attachment.string.asSymbol) };
		} {
			// do not attach spanner if an instance of the same type is already attached
			if (spanners.detect { |elem| elem.isKindOf(attachment.class) }.isNil) {
				spanners = spanners.add(attachment);
			};
		};
	}
	//!!! move up to LP_Object and inherit
	detach { |attachment|
		var object;
		if (attachment.isKindOf(LP_Indicator)) {
			attachment = indicators.detect { |elem| elem == attachment };
			if (attachment.notNil) { indicators.remove(attachment) };
		} {
			attachment = spanners.detect { |elem| elem == attachment };
			if (attachment.notNil) { spanners.remove(attachment) };
		};
	}
	tie {
		^spanners.detect { |spanner| spanner.isKindOf(LP_Tie) };
	}
	isTied {
		^this.tie.notNil;
	}
	detachTie {
		if (this.isTied) { this.detach(this.tie) };
	}
	beatDuration {
		var parentageRatios;
		parentageRatios = this.parents.collect { |parent|
			if (parent.isTuplet) { (parent.tupletDenominator / parent.tupletNumerator) } { 1 };
		};
		^duration.beatDuration * parentageRatios.reduce('*');
	}
	// for use in LP_Player
	type {
		^this.class;
	}
	//!!! move up to LP_Object and inherit
	override { |key, value|
		if (overrides.isNil) { overrides = OrderedIdentitySet[] };
		overrides = overrides.add(key -> value);
	}

	isLastLeafInMeasure {
		^if (this.root.notNil) { this.root.leaves.last == this } { false };
	}
	nextLeaf {
		var siblings, index;
		siblings = this.root.leaves;
		index = siblings.indexOf(this);
		^if (index == siblings.lastIndex) {
			if (this.root.nextMeasure.notNil) { this.root.nextMeasure.leaves[0] } { nil };
		} { siblings[index + 1] };
	}
	prevLeaf {
		var siblings, index;
		siblings = this.root.leaves;
		index = siblings.indexOf(this);
		^if(index == 0) {
			if (this.root.prevMeasure.notNil) { this.root.prevMeasure.leaves.last } { nil };
		} { siblings[index - 1] };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_FixedDurationContainer (abstract)
--------------------------------------------------------------------------------------------------------------- */
LP_FixedDurationContainer : LP_RhythmTreeContainer {
	*new { |duration, children, notes|
		^super.new(duration, children).notes_(notes);
	}
	selectBy { |class|
		^LP_Selection(this.nodes).selectBy(class);
	}
	attach { |attachment|
		this.selectBy(LP_Leaf)[0].attach(attachment);
	}
	// the beat-wise offsets of events within the container (1 = crotchet)
	offsets {
		^this.selectBy(LP_Event).components.collect { |each| each.beatDuration }.offsets.drop(-1);
	}
	beatDuration {
		^duration.beatDuration;
	}
	notes_ { |notes|
		if (notes.notNil) { this.selectBy(LP_PitchEvent).replaceNotes(notes) };
	}
	notes {
		^this.selectBy(LP_PitchEvent).components.collect { |event| event.note };
	}
	/* -----------------------------------------------------------------------------------------------------------
	a = LP_Staff(LP_Measure([4, 8], [3, 1, 2, LP_Tuplet(2, 1!7)]) ! 2);
	a.selectBy(LP_PitchEvent).replaceNotes((61..120));
	b = a.deepCopy;
	b.selectBy(LP_Event).mask([5,2,4,5,4,3]);
	c = b.deepCopy;
	c.measures.do { |e, i| e.beatStructure_([[3, 1], [2, 1, 1]][i]) };
	c.measures.do { |e, i| e.beamStructure_([[1, 3], [2, 1, 2]][i]) };
	c.selectBy(LP_Event).components.collect { |e| [e, e.type, e.beatDuration.round(0.01)] }.printAll; "";
	LP_File(LP_Score([a, b, c])).write("test1.ly");
	----------------------------------------------------------------------------------------------------------- */
	beatStructure_ { |beatStructure, rewriteBeams=false|
		var eventOffsets, beatStructureOffsets, unionOffsets, clumps, partitions;
		var measureOffsets, tieClumps, indices, selections;
		var firstLeafInMeasure, lastLeafInMeasure;


		eventOffsets = children.collect { |child| child.preProlatedDuration }.offsets;
		eventOffsets = (eventOffsets * (lcm(beatStructure.sum, eventOffsets.last) / eventOffsets.last)).asInteger;

		beatStructureOffsets = (beatStructure.offsets * (eventOffsets.last / beatStructure.sum)).asInteger;
		unionOffsets = eventOffsets.union(beatStructureOffsets).sort;
		clumps = unionOffsets.indicesOf(eventOffsets).intervals;
		partitions = unionOffsets.intervals.clumps(clumps);

		measureOffsets = this.offsets;

		children.do { |child, i|
			if (child.isKindOf(LP_Tuplet).not) { LP_Selection([child]).partition(partitions[i]) };
		};

		// protect against rounding errors returning false positives
		tieClumps = this.offsets.separate { |a, b| measureOffsets.round(0.001).includes(b.round(0.001)) };
		tieClumps = tieClumps.collect { |each| each.size };

		indices = (0..(tieClumps.sum - 1)).clumps(tieClumps);
		selections = indices.collect { |ind| this.selectBy(LP_Event)[ind] };
		lastLeafInMeasure = this.selectBy(LP_Event).last;

		selections.do { |selection|
			selection.components.do { |each| if (each != lastLeafInMeasure) { each.detachTie } };
			if (selection.size > 1) { selection.attach(LP_Tie()) };
		};

		//!!! this is a hack
		if (this.prevMeasure.notNil && { this.prevMeasure.leaves.last.isTiedToNext }) {
			this.prevMeasure.leaves.last.detachTie;
			LP_Selection([this.prevMeasure.leaves.last, this.leaves.first]).attach(LP_Tie());
		};

		//!!!TODO: CONNECT BEAMS INSIDE TUPLETS
		/*if (rewriteBeams) {
			var beats, beamStructure;
			beats = patternOffsets.sect(unionOffsets).drop(-1);
			beamStructure = unionOffsets.drop(-1).separate { |a, b| beats.includes(b) };
			beamStructure = beamStructure.collect { |each| each.size };
			this.beamStructure_(beamStructure);
		};*/
	}
	//!!! TEMPORARY HACK
	extractRedundantTuplets {
		[children.postln, children[0], children[0].parent].postln;
		if (children[0].parent.isKindOf(LP_Tuplet) && { children[0].parent.children.size == 1 }) {
			children[0].preProlatedDuration_(children[0].parent.preProlatedDuration);
			this.replace(children[0].parent, children[0]);
		}
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Measure
TODO:
- parentVoice: not a good inst var name, but parent causes naming conflicts in the tree, where LP_Measure must
always be root (and not the parent LP_Staff or LP_Voice) - this needs to be improved
- update timeSignature_ method: should stretch or compress leaf durations
- add extend(duration) method: extends timeSignature by duration, does not stretch leaves
- adds rest (or note/chord) to beginning, end, or some other indexed part of measure without stretching leaves
--------------------------------------------------------------------------------------------------------------- */
LP_Measure : LP_FixedDurationContainer {
	var <timeSignature, commands;
	var <parentVoice;
	*new { |timeSignature, children, notes|
		if (timeSignature.isArray) { timeSignature = LP_TimeSignature(*timeSignature) };
		^super.new(timeSignature.duration, children).timeSignature_(timeSignature).notes_(notes);
	}
	timeSignature_ { |argTimeSignature|
		if (argTimeSignature.isArray) { argTimeSignature = LP_TimeSignature(*argTimeSignature) };
		timeSignature = argTimeSignature;
		duration = timeSignature.duration;
	}
	addCommand { |command|
		if (commands.isNil) { commands = OrderedIdentitySet[] };
		commands = commands.add(command);
	}
	//!!!
	addParent { |parent|
		parentVoice = parent;
	}
	// get the next measure in this voice
	nextMeasure {
		^if (parentVoice.notNil) {
			var node = parentVoice.measures.findNodeOfObj(this).next;
			if (node.notNil) { node.obj }
		};
	}
	// get the previous measure in this voice
	prevMeasure {
		^if (parentVoice.notNil) {
			var node = parentVoice.measures.findNodeOfObj(this).prev;
			if (node.notNil) { node.obj };
		};
	}
}

LP_Tuplet : LP_FixedDurationContainer {
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_TieContainer
- behaves like a leaf
--------------------------------------------------------------------------------------------------------------- */
LP_TieContainer : LP_FixedDurationContainer {
	*new { |leaf|
		var preProlatedDurations, children;
		preProlatedDurations = LP_Duration.partitionNum(leaf.preProlatedDuration);
		children = preProlatedDurations.collect { |dur| leaf.clone.preProlatedDuration_(dur) };
		LP_ContiguousSelection(children).attach(LP_Tie());
		^super.new(leaf.preProlatedDuration, children);
	}
	clone {
		^children[0].clone.preProlatedDuration_(preProlatedDuration);
	}
	shallowClone {
		^children[0].shallowClone.preProlatedDuration_(preProlatedDuration);
	}
	attach { |attachment|
		if (attachment.isKindOf(LP_Tie)) {
			children.last.attach(attachment);
		} { children.first.attach(attachment) };
	}
	last {
		^children.last;
	}
	isNote {
		^children[0].isKindOf(LP_Note);
	}
	isChord {
		^children[0].isKindOf(LP_Chord);
	}
	//!!! i.e. the notes contained within the chord, and not across a LP_TieSelection
	//!!! this needs to be refactored
	chordNotes_ { |notes|
		children.do { |child| child.notes_(notes) };
	}
	note_ { |note|
		children.do { |child| child.note_(note) };
	}
	note {
		^children[0].note;
	}
	dynamic_ { |dynamic|
		children.do { |child| child.dynamic_(dynamic) };
	}
	dynamic {
		^children[0].dynamic;
	}
	beatDuration {
		var parentageRatios;
		parentageRatios = this.parents.collect { |parent|
			if (parent.isTuplet) { (parent.tupletDenominator / parent.tupletNumerator) } { 1 };
		};
		^duration.beatDuration * parentageRatios.reduce('*');
	}
	// for use in LP_Player
	type {
		^children[0].class;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
LP_Note
--------------------------------------------------------------------------------------------------------------- */
LP_Note : LP_RhythmTreeLeaf {
	var <note, <noteName, <dynamic;
	var namedPitch;
	*new { |note=60, duration|
		^super.new.init(note, duration);
	}
	init { |argNote, argDuration|
		this.note_(argNote);
		if (argDuration.isKindOf(LP_Duration)) {
			duration = argDuration; //!!! update -- get rid of duration
			writtenDuration = argDuration;
		} { preProlatedDuration = argDuration.abs };
	}
	// midinote, LP noteName, or SC noteName
	note_ { |argNote|
		namedPitch = LP_NamedPitch(argNote);
		note = namedPitch.note;
		noteName = namedPitch.noteName;
	}
	noteName_ { |noteName|
		this.note_(noteName);
	}
	dynamic_ { |argDynamic|
		dynamic = argDynamic;
	}
	asChord {
		var chord;
		chord = LP_Chord([note], duration).cloneAttachmentsFrom(this);
		^chord;
	}
	asNote {
		^this;
	}
	//!!! TODO
	writtenDuration_ { // arg: a LP_Duration
	}
}
/* ---------------------------------------------------------------------------------------------------------------
LP_Chord
--------------------------------------------------------------------------------------------------------------- */
LP_Chord : LP_RhythmTreeLeaf {
	var <notes, <noteNames, <dynamic;
	var namedPitches;
	*new { |notes, duration|
		^super.new.init(notes, duration);
	}
	init { |argNotes, argDuration|
		this.notes_(argNotes);
		if (argDuration.isKindOf(LP_Duration)) {
			duration = argDuration; //!!! update -- get rid of duration
			writtenDuration = argDuration;
		} { preProlatedDuration = argDuration.abs };
	}
	// midinote, LP noteName, or SC noteName
	notes_ { |argNotes|
		namedPitches = argNotes.collect { |note| LP_NamedPitch(note) };
		notes = namedPitches.collect { |namedPitch| namedPitch.note };
		noteNames = namedPitches.collect { |namedPitch| namedPitch.noteName };
	}
	noteNames_ { |noteNames|
		this.notes_(noteNames);
	}

	/*dynamic_ { |argDynamic|
		dynamic = argDynamic;
	}*/
	asNote {
		var note;
		note = LP_Note(notes[0], duration).cloneAttachmentsFrom(this);
		^note;
	}
	asChord {
		^this;
	}
	//!!! should at method return notes or a LP_VerticalMoment ??
	at { |indices|
		if (indices.isNumber) { indices = [indices] };
		^notes.atAll(indices);
	}
	//!!! TODO: similar implementation to LP_Grob:override
	tweak { |index|
		this.notYetImplemented;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
LP_Rest
--------------------------------------------------------------------------------------------------------------- */
LP_Rest : LP_RhythmTreeLeaf {
	*new { |duration|
		^super.new.init(duration);
	}
	init { |argDuration|
		if (argDuration.isKindOf(LP_Duration)) {
			duration = argDuration;
		} { preProlatedDuration = argDuration.abs };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
LP_MultimeasureRest
--------------------------------------------------------------------------------------------------------------- */
LP_MultimeasureRest : LP_Rest {
}
/* ---------------------------------------------------------------------------------------------------------------
LP_Skip
--------------------------------------------------------------------------------------------------------------- */
LP_Skip : LP_Rest {
}
