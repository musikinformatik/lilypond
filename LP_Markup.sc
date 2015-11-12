/* ---------------------------------------------------------------------------------------------------------------
• LP_Markup
- TODO: should this be a subclass of LP_Indicator ??

|string, position, align, xoffset, yoffset|
--------------------------------------------------------------------------------------------------------------- */
LP_Markup : LP_Indicator {
	var decorators, <lyObj="Markup";
	lpStr {
		var str;
		decorators = [];
		str = this.posStr(position) ++ "\\markup {";
		if (decorators.notNil) { decorators.do { |decoratorStr| str = str + decoratorStr } };
		str = str + string.asCompileString + "}";
		^str;
	}
	bold {
		decorators = decorators.add("\\bold");
	}
	italic {
		decorators = decorators.add("\\italic");
	}
	caps {
		decorators = decorators.add("\\caps");
	}
	//!!! remove -- go with a single override method
	font_name { |name|
		decorators = decorators.add("\\override #'(font-name ." + name.asCompileString + ")");
	}
	fontsize { |size|
		decorators = decorators.add("\\fontsize #" ++ size.asString);
	}
	box {
		decorators = decorators.add("\\box");
	}
	pad_around { |padding|
		decorators = decorators.add("\\pad-around #" ++ padding.asString);
	}
	with_color { |color|
		decorators = decorators.add("\\with-color #" ++ color.asString);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_MarkupCommand
see: http://abjad.mbrsi.org/api/tools/markuptools/MarkupCommand.html?highlight=markupcommand

LP_MarkupCommand('draw-circle', 2.5, 0.1, false);
--------------------------------------------------------------------------------------------------------------- */
LP_MarkupCommand {
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_MusicGlyph
see: http://abjad.mbrsi.org/appendices/version_history/version_2_11.html?highlight=musicglyph
[This is a subclass of markuptools.MarkupCommand, and can therefore be used anywhere MarkupCommand can appear. It guarantees correct quoting around the glyph name (which is easy to forget, or not always clear how to do for new users), and also checks that the glyph name is recognized in LilyPond.]

LP_MusicGlyph('accidentals.sharp').lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_MusicGlyph : LP_MarkupCommand {
	var glyphName;
	*new { |glyphName|
		^super.new.init(glyphName);
	}
	init { |argGlyphName|
		glyphName = argGlyphName;
	}
	lpStr {
		^"\\musicglyph #" ++ glyphName.asString.quote;
	}
}