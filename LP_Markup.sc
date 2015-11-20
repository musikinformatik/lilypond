/* ---------------------------------------------------------------------------------------------------------------
• LP_Markup
- position: \above, \below
- x, y: Number, \left, \center, \right, \up, \down

x = LP_Markup("foobar", \above, \center, 2.5);
x.font_(Font("Helvetica", 12, bold: false, italic: true));
x.bold_(true);
x.color_(Color.grey);
x.lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_Markup : LP_Object {
	var <string, <position, <x, <y;
	var <decorators, <font, <>component;
	*new { |string, position, x, y|
		^super.new.init(string, position, x, y);
	}
	init { |argString, argPosition, argX, argY|
		string = argString;
		position = argPosition;
		x = argX;
		y = argY;
		decorators = OrderedIdentitySet[];
	}
	lpStr { |indent=0|
		var str="";
		// only add position str if LP_Markup is attached to a LP_Leaf
		if (component.isKindOf(LP_Leaf)) { str = "\n" ++ switch(position, \above, "^", \below, "_", nil, "-") };
		str = str ++ "\\markup {";
		if (x.notNil) { str = str ++ "\n\t\\general-align #X #" ++ x.asString.toUpper };
		if (y.notNil) { str = str ++ "\n\t\\general-align #Y #" ++ y.asString.toUpper };
		if (font.notNil) {
			str = str ++ "\n\t\\override #'(font-name ." + font.name.toLower.quote ++ ")";
			this.size_(font.size).bold_(font.bold).italic_(font.italic);
		};
		decorators.do { |decoratorStr| str = str ++ "\n\t" ++ decoratorStr };
		str = str ++ "\n\t" ++ string.quote ++ "\n}";
		if (indent > 0) { str = str.replace("\n", "\n".catList("\t" ! indent)) };
		^str;
	}
	font_ { |argFont|
		font = argFont;
	}
	size_ { |size|
		//if (font.notNil) { font.size_(size) };
		decorators = decorators.add("\\abs-fontsize #" ++ size.asString);
	}
	bold_ { |bool|
		//if (font.notNil) { font.bold_(bool) };
		if (bool) { decorators = decorators.add("\\bold") };
	}
	italic_ { |bool|
		//if (font.notNil) { font.italic_(bool) };
		decorators = decorators.add("\\italic");
	}
	box_ { |bool|
		if (bool) { decorators = decorators.add("\\box") };
	}
	padding_ { |padding|
		decorators = decorators.add("\\pad-around #" ++ padding.asString);
	}
	color_ { |color|
		var rgb = [color.red, color.green, color.blue];
		decorators = decorators.add("\\with-color #(rgb-color".scatList(rgb) ++")");
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