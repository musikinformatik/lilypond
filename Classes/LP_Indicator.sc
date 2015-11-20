/* ---------------------------------------------------------------------------------------------------------------
TODO:
• line returns and tab indents for each new decorator
• add position arguments for all spanners and indicators
• difference between LP_Indicator and LP_Markup ??

TODO (from abjad):
LP_Accelerando
LP_Annotation
LP_Arpeggio
LP_Arrow
LP_Articulation
LP_BarLine
LP_BendAfter
LP_BowContactPoint
LP_BowMotionTechnique
LP_BowPressure
LP_BreathMark
LP_Clef
LP_ClefInventory
LP_ColorFingering
LP_Dynamic
LP_Fermata
LP_IndicatorExpression
LP_IsAtSoundingPitch
LP_IsUnpitched
LP_KeyCluster
LP_KeySignature
LP_LaissezVibrer
LP_LilyPondCommand
LP_LilyPondComment
LP_LineSegment
LP_MetricModulation
LP_PageBreak
LP_RehearsalMark
LP_Repeat
LP_Ritardando
LP_StaffChange
LP_StemTremolo
LP_StringContactPoint
LP_StringNumber
LP_SystemBreak
LP_Tempo
LP_TempoInventory
LP_TimeSignature
LP_TimeSignatureInventory
LP_Tremolo
LP_Tuning

position: \above, \below
x, y: Number, \left, \center, \right, \up, \down
--------------------------------------------------------------------------------------------------------------- */
LP_Indicator : LP_Object {
	var <string, position, x, y;
	var <>component;
	*new { |string, position, x, y|
		^super.new.init(string, position, x, y);
	}
	init { |argString, argPosition, argX, argY|
		string = argString;
		position = argPosition;
		x = argX;
		y = argY;
	}
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "-");
	}
	formatStr { |obj, indent=0|
		var str="";
		if (x.notNil) { str = str ++ "\\general-align #X #" ++ x.asString };
		if (y.notNil) { str = str ++ "\\general-align #Y #" ++ x.asString };
	}
}

LP_Dynamic : LP_Indicator {
	var <lyObj="Dynamic";
	lpStr {
		var xoffsetStr="", yoffsetStr="";
		//if (xoffset != 0) { xoffsetStr = "\\general-align #X #" ++ xoffset.asString };
		//if (yoffset != 0) { yoffsetStr = "\\general-align #Y #" ++ yoffset.asString };
		//^(str ++ "\\markup { \\dynamic \\center-align" + xoffsetStr + yoffsetStr + string + "}");
		//^(this.posStr(position) ++ "\\markup { \\dynamic \\center-align" + xoffsetStr + yoffsetStr + string + "}");
		^(this.posStr(position) ++ "\\markup { \\dynamic \\center-align" + string + "}");
	}
}

LP_Articulation : LP_Indicator {
	classvar <articulations;
	var <lyObj="Articulation";
	*initClass {
		articulations = #[
			'>', '^', '_', '!', '.', '-', '+', 'accent', 'espressivo', 'marcato', 'portato', 'staccatissimo',
			'staccato', 'tenuto', 'prall', 'prallup', 'pralldown', 'upprall', 'downprall', 'prallprall',
			'lineprall','prallmordent', 'mordent', 'upmordent','downmordent', 'trill', 'turn', 'reverseturn',
			'shortfermata', 'fermata', 'longfermata', 'verylongfermata', 'upbow', 'downbow', 'flageolet',
			'open', 'halfopen', 'lheel', 'rheel', 'ltoe', 'rtoe', 'snappizzicato', 'stopped', 'segno',
			'coda', 'varcoda', 'accentus', 'circulus', 'ictus', 'semicirculus', 'signumcongruentiae'
		];
	}
	*new { |string, position, align, xoffset, yoffset|
		if (articulations.includes(string.asSymbol)) {
			^super.new(string, position, align, xoffset, yoffset);
		} {
			^error("Articulation" + string.asString.quote + "not found.");
		};
	}
	lpStr {
		var str, xoffsetStr="", yoffsetStr="";
		str = this.posStr(position);
		if (string.asString[0].isAlpha) { str = str ++ "\\" };
		str = str ++ string.asString;
		^str;
	}
}

/* ---------------------------------------------------------------------------------------------------------------
• LP_StaffIndicator
- abtract class: all LP_StaffIndicators are placed before the components to which they are attached
--------------------------------------------------------------------------------------------------------------- */
LP_StaffIndicator : LP_Indicator {
	*new { |string|
		^super.new.init(string);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Clef
LP_Clef('bass').lpStr;
LP_Clef('treble^8').lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_Clef : LP_StaffIndicator {
	var <lyObj="Clef";
	lpStr {
		string = string.asString;
		// if name includes non-alpha characters (e.g. treble_8), enclose in quotes
		if (string.findRegexp("[^a-zA-Z]").notEmpty) { string = string.quote };
		^("\\clef" + string);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_MetronomeMark
LP_MetronomeMark(LP_Duration(1, 4), 120).lpStr;
LP_MetronomeMark(LP_Duration(1, 4), 60).tempo;
--------------------------------------------------------------------------------------------------------------- */
LP_MetronomeMark : LP_StaffIndicator {
	var <duration, <bpm;
	var <lyObj="MetronomeMark";
	*new { |duration, bpm|
		^super.new.init(duration, bpm)
	}
	init { |argDuration, argBpm|
		duration = argDuration;
		bpm = argBpm;
	}
	// SC tempo
	tempo {
		^((bpm / 60) * duration.beatDuration);
	}
	lpStr {
		^("\\tempo" + duration.lpStr + "=" + bpm.asString)
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_RehearsalMark (http://www.lilypond.org/doc/v2.17/Documentation/notation/bars#rehearsal-marks)
LP_RehearsalMark().lpStr;
LP_RehearsalMark("A").lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_RehearsalMark : LP_StaffIndicator {
	var <lyObj="RehearsalMark";
	lpStr {
		^if (string.isNil) { "\\mark \\default" } { "\\mark" + string.asString.quote };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_BarLine (http://www.lilypond.org/doc/v2.17/Documentation/notation/bars)
LP_BarLine("|.").lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_BarLine : LP_Indicator {
	var <lyObj="BarLine";
	lpStr {
		^("\\bar" + string.asString.quote );
	}
}