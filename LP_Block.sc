/* ---------------------------------------------------------------------------------------------------------------
x = LP_Block('header');
x.add('title', "Symphony No. 1");
x.override('NoteHead.font-size', 12);
x.items;
x.lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_Block {
	var name, <items, <overrides, <commands, <contextBlocks;
	*new { |name|
		^super.new.init(name);
	}
	init { |argName|
		name = argName;
		items = OrderedIdentitySet[];
		overrides = OrderedIdentitySet[];
		commands = OrderedIdentitySet[];
		contextBlocks = OrderedIdentitySet[];
	}
	add { |key, element|
		items = items.add(key -> element);
	}
	override { |key, value|
		overrides = overrides.add(key -> value);
	}
	addCommand { |command|
		commands = commands.add(command);
	}
	addContextBlock { |contextBlock|
		contextBlocks = contextBlocks.add(contextBlock);
	}
	lpStr {
		var str;
		str = "\\" ++ name.asString + "{";
		items.do { |assoc| str = str ++ "\n\t" ++ assoc.key + "=" + assoc.value.asString };
		overrides.do { |assoc| str = str ++ "\n\t\\override" + assoc.key + "=" + assoc.value.asString };
		commands.do { |command| str = str ++ "\n\t\\" ++ command.asString };
		contextBlocks.do { |each| str = str ++ each.lpStr };
		str = str ++ "\n}";
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
x = LP_ContextBlock('Score');
x.add('title', "Symphony No. 1");
x.override('NoteHead.font-size', 12);
x.lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_ContextBlock : LP_Block {
	var contextName;
	*new { |contextName|
		^super.new('context').init(contextName);
	}
	init { |argContextName|
		contextName = argContextName;
	}
	lpStr {
		var str;
		str = "\n\t\\context {";
		str = str ++ "\n\t\t\\Score";
		items.do { |assoc| str = str ++ "\n\t\t" ++ assoc.key + "=" + assoc.value };
		overrides.do { |assoc| str = str ++ "\n\t\t\\override" + assoc.key + "=" + assoc.value.asString };
		commands.do { |command| str = str ++ "\n\t\t\\" ++ command.asString };
		str = str ++ "\n\t}";
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
LP_LayoutBlock().lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_LayoutBlock : LP_Block {
	*new {
		^super.new('layout');
	}
}

LP_PaperBlock : LP_Block {
	var <left_margin, <right_margin, <top_margin, <bottom_margin, <score_system_spacing;
	*new {
		^super.new('paper');
	}
	left_margin_ { |dimension|
		left_margin = dimension;
		this.add('left-margin', left_margin);
	}
	right_margin_ { |dimension|
		right_margin = dimension;
		this.add('right-margin', right_margin);
	}
	top_margin_ { |dimension|
		top_margin = dimension;
		this.add('top-margin', top_margin);
	}
	bottom_margin_ { |dimension|
		bottom_margin = dimension;
		this.add('bottom-margin', bottom_margin);
	}
	score_system_spacing_ { |spacingVector|
		score_system_spacing = spacingVector;
		this.add('score-system-spacing', score_system_spacing);
	}
}

LP_HeaderBlock : LP_Block {
	/*var <dedication, <title, <subtitle, <subsubtitle, <instrument, <poet, <composer;
	var <meter, <arranger, <tagline, <copyright;*/
	*new {
		^super.new('header');
	}
	/*add { |key, element|
		if (element.isString) { element = LP_Markup(element) };
		super.add(key, element);
	}
	dedication_ { |argDedication|
		dedication = argDedication;
		this.add('dedication', dedication);
	}
	title_ { |argTitle|
		title = argTitle;
		this.add('title', title);
	}
	subtitle_ { |argSubtitle|
		subtitle = argSubtitle;
		this.add('subtitle', subtitle);
	}
	subsubtitle_ { |argSubsubtitle|
		subsubtitle = argSubsubtitle;
		this.add('subsubtitle', subsubtitle);
	}
	instrument_ { |argInstrument|
		instrument = argInstrument;
		this.add('instrument', instrument);
	}
	poet_ { |argPoet|
		poet = argPoet;
		this.add('poet', poet);
	}
	composer_ { |argComposer|
		composer = argComposer;
		this.add('composer', composer);
	}
	meter_ { |argMeter|
		meter = argMeter;
		this.add('meter', meter);
	}
	arranger_ { |argArranger|
		arranger = argArranger;
		this.add('arranger', arranger);
	}
	tagline_ { |argTagline|
		tagline = argTagline;
		this.add('tagline', tagline);
	}
	copyright_ { |argCopyright|
		copyright = argCopyright;
		this.add('copyright', copyright);
	}*/
}

LP_Dimension {
	var <value, <unit;
	*new { |value, unit|
		^super.new.init(value, unit);
	}
	init { |argValue, argUnit|
		value = argValue;
		unit = argUnit;
	}
	lpStr {
		^(value.asString ++ "\\" ++ unit.asString);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
see: http://www.lilypond.org/doc/v2.19/Documentation/notation/flexible-vertical-spacing-paper-variables
LP_SpacingVector(12, 17, 48, 9).lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_SpacingVector {
	var <basic_distance, <minimum_distance, <padding, <stretchability;
	*new { |basic_distance=0, minimum_distance=0, padding=12, stretchability=0|
		^super.new.init(basic_distance, minimum_distance, padding, stretchability);
	}
	init { |argBasic_distance, argMinimum_distance, argPadding, argStretchability|
		basic_distance = argBasic_distance;
		minimum_distance = argMinimum_distance;
		padding = argPadding;
		stretchability = argStretchability;
	}
	lpStr {
		var str, keys;
		keys = #["basic-distance", "minimum-distance", "padding", "stretchability"];
		str = "#'(";
		[basic_distance, minimum_distance, padding, stretchability].do { |value, i|
			str = str ++ "(" ++ keys[i] + "." + value ++ ")";
		};
		str = str ++ ")";
		^str;
	}
}