/* ---------------------------------------------------------------------------------------------------------------
TODO
• order in which attachments are written to ly file should be determined internally by LP_File
• make all class names consistent with corresponding classes in Lilypond
• <> Hairpins (on single notes ??)
--------------------------------------------------------------------------------------------------------------- */
//!!! add vertical position arguments for all subclasses ??
LP_Spanner : LP_Object {
	var <>components;
	var position, xoffset, yoffset;
	*new { |position, xoffset, yoffset|
		^super.new.init(position, xoffset, yoffset);
	}
	init { |argPosition, argXoffset, argYoffset|
		position = argPosition;
		xoffset = argXoffset;
		yoffset = argYoffset;
	}
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "-");
	}
	/*
	lpStr { |component|
		^case
		{ components.first == component } { this.posStr(position) ++ this.startToken }
		{ components.last == component } { this.endToken }
		{ "" };
	}
	*/
	lpStartStr {
		^this.posStr(position) ++ this.startToken;
	}
	lpEndStr {
		^this.endToken;
	}
}

LP_Beam : LP_Spanner {
	var <startToken="[", <endToken="]", <lyObj="Beam";
}

LP_Crescendo : LP_Spanner {
	var <startToken="\\<", <endToken="\\!", <lyObj="Hairpin";
}

LP_Diminuendo : LP_Spanner {
	var <startToken="\\>", <endToken="\\!", <lyObj="Hairpin";
}

//!!! TODO
LP_GlissandoSkip : LP_Spanner {
	var <startToken="\\glissando \\glissandoSkipOn>", <endToken="\\glissandoSkipOff", <lyObj="Glissando";
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

LP_Hairpin : LP_Spanner {
	var descriptor, <startToken, <endToken, <lyObj="Hairpin";
	*new { |descriptor, position, xoffset, yoffset|
		^super.new.init1(descriptor, position, xoffset, yoffset);
	}
	init1 { |argDescriptor, argPosition, argXoffset, argYoffset|
		var startDynIndex, startDyn, endDynIndex, endDyn, hairPinIndex, hairpin;
		descriptor = argDescriptor;
		position = argPosition;
		xoffset = argXoffset;
		yoffset = argYoffset;
		# startDynIndex, startDyn, endDynIndex, endDyn = descriptor.asString.findRegexp("[a-z]+").flatten(1);
		# hairPinIndex, hairpin = descriptor.asString.findRegexp("[<>]").flatten(1);
		#startToken, endToken = case
		{ startDyn.isNil && endDyn.isNil } { ["\\" ++ hairpin, "\\!"] }
		{ endDyn.isNil && { startDynIndex < hairPinIndex } } { ["\\" ++ hairpin ++ "\\" ++ startDyn, "\\!"] }
		{ endDyn.isNil && { startDynIndex > hairPinIndex } } { ["\\" ++ hairpin, "\\" ++ startDyn] }
		{ ["\\" ++ hairpin ++ "\\" ++ startDyn, "\\" ++ endDyn] };
	}
}

LP_FlaredHairpin : LP_Hairpin {
	*new { |descriptor, position, xoffset, yoffset|
		^super.new(descriptor, position, xoffset, yoffset).override(\stencil, 'flared-hairpin');
	}
}

LP_ConstanteHairpin : LP_Hairpin {
	*new { |descriptor, position, xoffset, yoffset|
		^super.new(descriptor, position, xoffset, yoffset).override(\stencil, 'constante-hairpin');
	}
}

LP_CircledTipHairpin : LP_Hairpin {
	*new { |descriptor, position, xoffset, yoffset|
		^super.new(descriptor, position, xoffset, yoffset).override(\circled_tip, true);
	}
}

//!!! lyObj needed: Staff ??
LP_HiddenStaff : LP_Spanner {
	var <startToken="\\stopStaff", <endToken="\\startStaff";
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

//!!! startToken must precede first element - index is currently too great by one
//!!! new superclass needed
//!!! argument for octave offset must also be added
LP_OttavaBracket : LP_Spanner {
	var <startToken="\\ottava #1", <endToken="\\ottava #0", <lyObj="OttavaBracket";
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

//!!! add vertical position and asymmetry (eccentricity) arguments
// see: http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-moving-slur-positions-vertically)
// see: "http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-asymmetric-slurs"
// dotted and dashed slurs: "http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-changing-the-appearance-of-a-slur-from-solid-to-dotted-or-dashed"
LP_PhrasingSlur : LP_Spanner {
	var <startToken="\\(", <endToken="\\)", <lyObj="PhrasingSlur";
}

//!!! add vertical position and asymmetry (eccentricity) arguments
// see: http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-moving-slur-positions-vertically)
// see: "http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-asymmetric-slurs"
// dotted and dashed slurs: "http://www.lilypond.org/doc/v2.19/Documentation/snippets/expressive-marks#expressive-marks-changing-the-appearance-of-a-slur-from-solid-to-dotted-or-dashed"
LP_Slur : LP_Spanner {
	var <startToken="(", <endToken=")", <lyObj="Slur";
}

//!!! add style argument ??
//!!! \set Staff.pedalSustainStyle = #'mixed
//!!! \set Staff.pedalSustainStyle = #'bracket
//!!! also lyObj: SustainPedalLineSpanner
LP_SustainPedal : LP_Spanner {
	var <startToken="\\sustainOn", <endToken="\\sustainOff", <lyObj="SustainPedal";
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

//!!! also lyObj: SostenutoPedalLineSpanner
LP_SostenutoPedal : LP_Spanner {
	var <startToken="\\sostenutoOn", <endToken="\\sostenutoOff", <lyObj="SostenutoPedal";
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

/*!!! TODO
\stopStaff
\once \override Staff.StaffSymbol.line-count = 5
\startStaff
... MUSIC SELECTION ...
\stopStaff
\startStaff
*/
//!!! lyObj needed
LP_StaffLinesSpanner : LP_Spanner {
}

//!!! add overrides for LP_Markup text
LP_TextSpanner : LP_Spanner {
	var <startToken="\\startTextSpan", <endToken="\\stopTextSpan", <lyObj="TextSpanner";
	//!!! HACK
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

//!!! TODO: see http://abjad.mbrsi.org/api/tools/spannertools/TrillSpanner.html?highlight=trillspanner
LP_TrillSpanner : LP_Spanner {
	var <startToken="\\startTrillSpan", <endToken="\\stopTrillSpan", <lyObj="TrillSpanner";
	//!!! HACK
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}


//!!! LP_ComplexSpanner should be renamed for consistency with Abjad
//!!! rename LP_MultiSpanner ??
LP_ComplexSpanner : LP_Spanner {
	lpStr {
		^this.posStr(position) ++ this.token;
	}
	//!!! HACK
	posStr { |position|
		^switch(position, \above, "^", \below, "_", nil, "");
	}
}

LP_Glissando : LP_ComplexSpanner {
	var <token="\\glissando", <lyObj="Glissando";
}

//!!! TODO: must prevent attaching more than one LP_Tie to the same leaf
//!!! TODO: must also prevent adding more than one LP_Tie to the leaf's spanners collection (instance variable)
//!!! TODO: only allow tie if all leaves in the selection are non-rests and have the same note/s
LP_Tie : LP_ComplexSpanner {
	var <token="~", <lyObj="Tie";
}