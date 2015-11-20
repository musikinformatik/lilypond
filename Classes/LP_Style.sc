LP_Style {
	var <context;
	*new { |context|
		^super.new.init(context);
	}
	init { |argScore|
		context = argScore;
		this.set;
	}
}

NoteHeadsOnly : LP_Style {
	set {
		var lyObj;
		lyObj = context.lyObj;
		context.override(lyObj ++ ".BarLine.stencil", "##f");
		context.override(lyObj ++ ".TimeSignature.stencil", "##f");
		context.override(lyObj ++ ".Stem.stencil", "##f");
		context.override(lyObj ++ ".Tie.stencil", "##f");
		context.override(lyObj ++ ".Beam.stencil", "##f");
		context.override(lyObj ++ ".Flag.stencil", "##f");
		context.override(lyObj ++ ".Dots.stencil", "##f");
		context.override(lyObj ++ ".NoteHead.duration-log", "#4");
		context.override(lyObj ++ ".Rest.transparent", "##t");
		// only show first event in ties
		switch(context.class,
			LP_Score, { context.staves.do { |staff| this.hideTiedNotes(staff) } },
			LP_Staff, { this.hideTiedNotes(context) }
		);
	}
	hideTiedNotes { |staff|
		staff.selectBy(LP_PitchEvent).components.do { |sel|
			if (sel.isKindOf(LP_TieSelection)) {
				sel[1..].override('NoteHead.transparent', "##t").override('NoteHead.no-ledgers', "##t");
			};
		};
	}
}