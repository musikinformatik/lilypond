/* ---------------------------------------------------------------------------------------------------------------
• LP_Parser
- TODO: write the parser!
--------------------------------------------------------------------------------------------------------------- */
LP_Parser {
}

+ LP_RhythmTreeContainer {
	lpStr {
		var str="";
		if (this.isContainer) {
			if (isTuplet) {
				str = str ++ "\n";
				(this.depth + 2).do { str = str ++ "\t" };
				str = str ++ "\\tuplet " ++ tupletNumerator.asString ++ "/" ++ tupletDenominator.asString + "{";
			} { str = str + "{" };
		};
		children.do { |child| str = str ++ child.lpStr };
		str = str ++ "\n";
		(this.depth + 2).do { str = str ++ "\t" };
		str = str ++ "}";
		^str;
	}
}

+ LP_RhythmTreeLeaf {
	//!!! TODO: automatic generation of correct number of tab stops
	isFirstLeafIn { |spanner|
		^spanner.components.first == this;
	}
	isLastLeafIn { |spanner|
		^spanner.components.last == this;
	}
	formatStr { |str, inStr|
		// don't include space if inStr includes non-alphanumeric characters
		str = str ++ if (inStr.findRegexp("[\\[\\]\\<\\>\\(\\)\\!]").notEmpty) { inStr } { " " ++ inStr };
		^str;
	}
	formatStr1 { |str, inStr|
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
	lpStr { |token|
		var str="";
		/* --------------------------------------------------------------------------------
		assign any grob override settings
		!!! can this be outsourced to LP_Indicator/LP_Spanner ??
		-------------------------------------------------------------------------------- */
		if (indicators.notNil) {
			indicators.do { |indicator|
				if (indicator.overrides.notNil) {
					indicator.overrides.do { |override| str = this.formatStr1(str, override.lpStartStr) };
				};

				// clefs, etc.
				if (indicator.isKindOf(LP_StaffIndicator)) {
					str = this.formatStr4(str, indicator.lpStr);
				};
			};
		};

		if (spanners.notNil) {
			spanners.do { |spanner|
				if (spanner.overrides.notNil && { this.isFirstLeafIn(spanner) }) {
					spanner.overrides.do { |override| str = this.formatStr3(str, override.lpStartStr) };
				};
			};
		};

		/* --------------------------------------------------------------------------------
		leaf string (ie. note/rest/chord token)
		-------------------------------------------------------------------------------- */
		str = this.formatStr1(str, token);

		/* --------------------------------------------------------------------------------
		indicator and spanner strings
		override end strings
		-------------------------------------------------------------------------------- */
		if (indicators.notNil) {
			indicators.do { |indicator|
				if (indicator.isKindOf(LP_StaffIndicator).not) {
					str = this.formatStr(str, indicator.lpStr);

					// revert any grob override settings
					//!!! TODO: avoid duplicate strings
					if (indicator.overrides.notNil) {
						indicator.overrides.do { |override| str = this.formatStr(str, override.lpEndStr) };
					};
				};
			};
		};

		if (spanners.notNil) {
			spanners.do { |spanner|
				// LP_ComplexSpanners (glissando and tie): lpStr attached to all leaves but last
				if (spanner.isKindOf(LP_ComplexSpanner)) {
					if (this.isLastLeafIn(spanner).not) { str = this.formatStr(str, spanner.lpStr) };
				} {
					if (this.isFirstLeafIn(spanner)) { str = this.formatStr(str, spanner.lpStartStr) };
					if (this.isLastLeafIn(spanner)) { str = this.formatStr(str, spanner.lpEndStr) };
				};
			};
		};

		^str;
	}
}

+ LP_TimeSignature {
	lpStr {
		^(numerator.asString ++ "/" ++ denominator.asString)
	}
}

+ LP_Measure {
	lpStr {
		var str;
		if (this.prevMeasure.isNil) { str = "\t\t\\time " ++ timeSignature.lpStr } {
			if (this.prevMeasure.notNil && { this.prevMeasure.timeSignature.pair != timeSignature.pair }) {
				str = "\t\t\\time " ++ timeSignature.lpStr;
			};
		};
		commands.do { |command| str = str ++ "\n\t\t\\" ++ command.asString + "\n" };
		str = str ++ super.lpStr ++ "\n";
		^str;
	}
}

+ LP_Note {
	lpStr {
		var str = "";
		overrides.do { |assoc|
			str = str ++ "\n\t\\once \\override" + assoc.key + "=" + assoc.value.asString ++ "\n";
		};
		str = str ++ super.lpStr(noteName ++ writtenDuration.lpStr);
		^str;
	}
}

+ LP_Chord {
	lpStr {
		var str = "";
		if (overrides.notNil) { overrides.do { |assoc|
			str = str ++ "\n\t\\once \\override" + assoc.key + "=" + assoc.value.asString ++ "\n";
		} };
		str = str ++ super.lpStr("<" ++ noteNames.reduce('+') ++ ">" ++ writtenDuration.lpStr);
		^str;
	}
}

+ LP_Rest {
	lpStr {
		var str = "";
		if (overrides.notNil) { overrides.do { |assoc|
			str = str ++ "\n\t\\once \\override" + assoc.key + "=" + assoc.value.asString ++ "\n";
		} };
		str = str ++ super.lpStr("r" ++ duration.lpStr);
		str = str.replace("~", ""); // temporary hack for removing tie token
		^str;
	}
}

+ LP_MultimeasureRest {
	lpStr {
		//^super.lpStr("R" ++ duration.lpStr);
		^RhythmTreeLeaf().lpStr("R" ++ duration.lpStr);
	}
}

+ LP_Skip {
	lpStr {
		//^super.lpStr("s" ++ duration.lpStr);
		^RhythmTreeLeaf().lpStr("s" ++ duration.lpStr);
	}
}

+ LP_Duration {
	lpStr {
		^switch(this.numDots,
			0, { denominator.asString },
			1, { (denominator / 2).asInteger.asString ++ "." },
			2, { (denominator / 4).asInteger.asString ++ ".." }
		);
	}
}

+ LP_Object {
	indicatorStr { |str|
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
		str = "\\score {\n\t<<";
		str = super.indicatorStr(str);
		str = str ++ staves.collect { |staff| staff.lpStr }.reduce('++');
		str = str ++ "\t>>\n}\n";
		^str;
	}
}

+ LP_Staff {
	lpStr {
		var str;
		str = "\n\t\\new Staff {\n";
		str = super.indicatorStr(str);
		str = str ++ measures.collect { |measure| measure.lpStr }.reduce('++');
		str = str ++ "\t}\n";
		^str;
	}
}

+ LP_RhythmicStaff {
	lpStr {
		var str;
		str = "\t\\new RhythmicStaff {\n";
		str = super.indicatorStr(str);
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
		str = super.indicatorStr(str);
		str = "\n" ++ str;
		str = str ++ measures.collect { |measure| measure.lpStr }.reduce('++');
		str = str ++ "}\n";
		^str;
	}
}