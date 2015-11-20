/* ---------------------------------------------------------------------------------------------------------------
• LP_RhythmTreeLeaf
--------------------------------------------------------------------------------------------------------------- */
+ LP_RhythmTreeLeaf {
	//!!! TODO: automatic generation of correct number of tab stops
	isFirstLeafIn { |spanner|
		^spanner.components.first == this;
	}
	isLastLeafIn { |spanner|
		^spanner.components.last == this;
	}
	formatStr1 { |str, inStr|
		// don't include space if inStr includes non-alphanumeric characters
		str = str ++ if (inStr.findRegexp("[\\[\\]\\<\\>\\(\\)\\!]").notEmpty) { inStr } { " " ++ inStr };
		^str;
	}
	formatStr2 { |str, inStr|
		str = str ++ "\n";
		if (this.depth.notNil) { (this.depth + 2).do { str = str ++ "\t" } };
		str = str ++ inStr;
		^str;
	}
	formatStr3 { |str, inStr|
		if (this.depth.notNil) { (this.depth + 2).do { str = str ++ "\t" } };
		str = str ++ inStr;
		str = str ++ "\n";
		^str;
	}
	formatStr4 { |str, inStr|
		if (this.depth.notNil) { (this.depth + 2).do { inStr = "\t" ++ inStr } };
		str = "\n" ++ inStr ++ str;
		^str;
	}
	formatStr { |token|
		var str="";
		/* --------------------------------------------------------------------------------
		once overrides for this leaf
		-------------------------------------------------------------------------------- */
		overrides.do { |assoc|
			str = str ++ "\n\t\\once \\override" + assoc.key + "=" + assoc.value.asString ++ "\n";
		};
		/* --------------------------------------------------------------------------------
		assign any grob override settings
		!!! can this be delegated to LP_Markup/LP_Indicator/LP_Spanner ??
		-------------------------------------------------------------------------------- */
		markups.do { |markup|
			if (markup.overrides.notNil) {
				markup.overrides.do { |override| str = this.formatStr2(str, override.lpStartStr) };
			};
		};

		indicators.do { |indicator|
			if (indicator.overrides.notNil) {
				indicator.overrides.do { |override| str = this.formatStr2(str, override.lpStartStr) };
			};
		};

		spanners.do { |spanner|
			if (spanner.overrides.notNil && { this.isFirstLeafIn(spanner) }) {
				spanner.overrides.do { |override| str = this.formatStr3(str, override.lpStartStr) };
			};
		};
		/* --------------------------------------------------------------------------------
		indicators that must be inserted before component string (clefs, tempo, etc.)
		-------------------------------------------------------------------------------- */
		indicators.do { |indicator|
			if (indicator.isKindOf(LP_StaffIndicator)) { str = this.formatStr4(str, indicator.lpStr) };
		};
		/* --------------------------------------------------------------------------------
		leaf string (ie. note/rest/chord token)
		-------------------------------------------------------------------------------- */
		str = this.formatStr2(str, token);

		/* --------------------------------------------------------------------------------
		indicator and spanner strings
		override end strings
		-------------------------------------------------------------------------------- */
		markups.do { |markup| str = str + markup.lpStr(indent: this.depth + 2) };

		indicators.do { |indicator|
			if (indicator.isKindOf(LP_StaffIndicator).not) {
				str = this.formatStr1(str, indicator.lpStr);

				// revert any grob override settings
				//!!! TODO: avoid duplicate strings
				if (indicator.overrides.notNil) {
					indicator.overrides.do { |override| str = this.formatStr1(str, override.lpEndStr) };
				};
			};
		};

		spanners.do { |spanner|
			// LP_ComplexSpanners (glissando and tie): lpStr attached to all leaves but last
			if (spanner.isKindOf(LP_ComplexSpanner)) {
				if (this.isLastLeafIn(spanner).not) { str = this.formatStr1(str, spanner.lpStr) };
			} {
				if (this.isFirstLeafIn(spanner)) { str = this.formatStr1(str, spanner.lpStartStr) };
				if (this.isLastLeafIn(spanner)) { str = this.formatStr1(str, spanner.lpEndStr) };
			};
		};

		^str;
	}
}

+ LP_Note {
	lpStr {
		var str = "";
		str = str ++ this.formatStr(noteName ++ writtenDuration.lpStr);
		^str;
	}
}

+ LP_Chord {
	lpStr {
		var str = "";
		str = str ++ this.formatStr("<" ++ noteNames.reduce('+') ++ ">" ++ writtenDuration.lpStr);
		^str;
	}
}

+ LP_Rest {
	lpStr {
		var str = "";
		str = str ++ this.formatStr("r" ++ duration.lpStr);
		str = str.replace("~", ""); // temporary hack for removing tie token
		^str;
	}
}

+ LP_MultimeasureRest {
	lpStr {
		^this.formatStr("R" ++ duration.lpStr);
	}
}

+ LP_Skip {
	lpStr {
		^this.formatStr("s" ++ duration.lpStr);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_RhythmTreeContainer
--------------------------------------------------------------------------------------------------------------- */
+ LP_RhythmTreeContainer {
	formatStr {
		var str="";
		//if (this.isContainer) {
		(this.depth + 2).do { str = str ++ "\t" };
		if (isTuplet) {
			str = str ++ "\\tuplet " ++ tupletNumerator.asString ++ "/" ++ tupletDenominator.asString + "{";
		} {
			str = str ++ "{";
		};
		//};
		children.do { |child| str = str ++ child.lpStr };
		str = str ++ "\n";
		(this.depth + 2).do { str = str ++ "\t" };
		str = str ++ "}";
		^str;
	}
}

+ LP_Tuplet {
	lpStr {
		var str="";
		//str = str ++ "\\tuplet " ++ tupletNumerator.asString ++ "/" ++ tupletDenominator.asString + "{";
		str = str ++ this.formatStr;
		^str;
	}
}

+ LP_Measure {
	lpStr {
		var str;
		if (this.prevMeasure.isNil || { this.prevMeasure.timeSignature.pair != timeSignature.pair }) {
			str = "\t\t\\time " ++ timeSignature.lpStr ++ "\n";
		};
		commands.do { |command| str = str ++ "\n\t\t\\" ++ command.asString + "\n" };
		//str = str ++ "{";
		str = str ++ this.formatStr ++ "\n";
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Object
--------------------------------------------------------------------------------------------------------------- */
+ LP_Object {
	indicatorStr {
		var str="";
		//!!!attachments.do { |attachment| str = str ++ "\n\t\\" ++ attachment.lpStr };
		functions.do { |assoc| str = str ++ "\n\t\\" ++ assoc.key.asString + assoc.value.asString };
		sets.do { |assoc| str = str ++ "\n\t\\set" + assoc.key + "=" + assoc.value.asString };
		overrides.do { |assoc| str = str ++ "\n\t\\override" + assoc.key + "=" + assoc.value.asString };
		^str;
	}
}

+ LP_Score {
	lpStr {
		var str;
		str = "\n\\score {\n\t<<";
		str = str ++ this.indicatorStr;
		str = str ++ staves.collect { |staff| staff.lpStr }.reduce('++');
		str = str ++ "\t>>\n}\n";
		^str;
	}
}

+ LP_Staff {
	lpStr {
		var str;
		str = "\n\t\\new Staff {\n";
		str = str ++ this.indicatorStr;
		str = str ++ measures.collect { |measure| measure.lpStr }.reduce('++');
		str = str ++ "\t}\n";
		^str;
	}
}

+ LP_RhythmicStaff {
	lpStr {
		var str;
		str = "\t\\new RhythmicStaff {\n";
		str = str ++ this.indicatorStr;
		str = "\n" ++ str;
		str = str ++ measures.collect { |measure| measure.lpStr }.reduce('++');
		str = str ++ "\t}\n";
		^str;
	}
}

+ LP_Voice {
	lpStr {
		var str;
		str = "\t\\new Voice {\n";
		//!!! str = str ++ "\\voiceOne\n"; // for correct positioning of stems, etc.
		str = str ++ this.indicatorStr;
		str = "\n" ++ str;
		str = str ++ measures.collect { |measure| measure.lpStr }.reduce('++');
		str = str ++ "}\n";
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_TimeSignature
--------------------------------------------------------------------------------------------------------------- */
+ LP_TimeSignature {
	lpStr {
		^(numerator.asString ++ "/" ++ denominator.asString)
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Duration
--------------------------------------------------------------------------------------------------------------- */
+ LP_Duration {
	lpStr {
		^switch(this.numDots,
			0, { denominator.asString },
			1, { (denominator / 2).asInteger.asString ++ "." },
			2, { (denominator / 4).asInteger.asString ++ ".." }
		);
	}
}