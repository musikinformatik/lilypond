/* ---------------------------------------------------------------------------------------------------------------
• LP_Block

- !!TODO: pass objects into storage containers (items, overrides, etc.), not their lpStrs
- !!TODO: storage containers must be IdentityDictionaries, to ensure against duplication of keys/settings
- !!TODO: add a method for inserting any arbitrary string into lpStr
--------------------------------------------------------------------------------------------------------------- */
LP_Block {
	var <>name, <items, <overrides, <commands, <contextBlocks;
	*new { |name|
		^super.new.init(name);
	}
	init { |argName|
		name = argName;
		items = OrderedIdentitySet[];
		//items = IdentityDictionary[]; //!! see note above
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
		contextBlocks.do { |each| str = str ++ each.lpStr(indent: 1) };
		str = str ++ "\n}";
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_ContextBlock
--------------------------------------------------------------------------------------------------------------- */
LP_ContextBlock : LP_Block {
	var contextName;
	*new { |contextName|
		^super.new('context').init(contextName);
	}
	init { |argContextName|
		contextName = argContextName;
	}
	lpStr { |indent=0|
		var str;
		str = "\n\\context {";
		str = str ++ "\n\t\\Score";
		items.do { |assoc| str = str ++ "\n\t" ++ assoc.key + "=" + assoc.value };
		overrides.do { |assoc| str = str ++ "\n\t\\override" + assoc.key + "=" + assoc.value.asString };
		commands.do { |command| str = str ++ "\n\t\\" ++ command.asString };
		str = str ++ "\n}";
		if (indent > 0) { str = str.replace("\n", "\n".catList("\t" ! indent)) };
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_LayoutBlock
--------------------------------------------------------------------------------------------------------------- */
LP_LayoutBlock : LP_Block {
	var <indent, <raggedRight, <raggedBottom, <raggedLast;
	*new {
		^super.new('layout');
	}
	indent_ { |dimension|
		indent = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('indent', indent.lpStr);
	}
	raggedRight_ { |bool|
		raggedRight = bool;
		this.add('ragged-right', raggedRight.lpStr);
	}
	raggedBottom_ { |bool|
		raggedBottom = bool;
		this.add('ragged-bottom', raggedBottom.lpStr);
	}
	raggedLast_ { |bool|
		raggedLast = bool;
		this.add('ragged-last', raggedLast.lpStr);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_PaperBlock
--------------------------------------------------------------------------------------------------------------- */
LP_PaperBlock : LP_Block {
	var <leftMargin, <rightMargin, <topMargin, <bottomMargin;
	var <systemSystemSpacing, <scoreSystemSpacing;
	var <systemCount, <pageCount;
	var <indent, <raggedRight, <raggedBottom, <raggedLast;
	*new {
		^super.new('paper');
	}
	leftMargin_ { |dimension|
		leftMargin = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('left-margin', leftMargin.lpStr);
	}
	rightMargin_ { |dimension|
		rightMargin = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('right-margin', rightMargin.lpStr);
	}
	topMargin_ { |dimension|
		topMargin = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('top-margin', topMargin.lpStr);
	}
	bottomMargin_ { |dimension|
		bottomMargin = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('bottom-margin', bottomMargin.lpStr);
	}
	margin_ { |left=10, top=10, right=10, bottom=10|
		this.leftMargin_(left).topMargin_(top).rightMargin_(right).bottomMargin_(bottom);
	}
	systemSystemSpacing_ { |basicDistance, minimumDistance, padding, stretchability|
		systemSystemSpacing = LP_SpacingVector(basicDistance, minimumDistance, padding, stretchability);
		this.add('system-system-spacing', systemSystemSpacing.lpStr);
	}
	scoreSystemSpacing_ { |basicDistance, minimumDistance, padding, stretchability|
		scoreSystemSpacing = LP_SpacingVector(basicDistance, minimumDistance, padding, stretchability);
		this.add('score-system-spacing', scoreSystemSpacing.lpStr);
	}
	systemCount_ { |num|
		systemCount = num;
		this.add('system-count', systemCount.asString);
	}
	pageCount_ { |num|
		pageCount = num;
		this.add('page-count', pageCount.asString);
	}
	indent_ { |dimension|
		indent = if (dimension.isNumber) { LP_Dimension(dimension) } { dimension };
		this.add('indent', indent.lpStr);
	}
	raggedRight_ { |bool|
		raggedRight = bool;
		this.add('ragged-right', raggedRight.lpStr);
	}
	raggedBottom_ { |bool|
		raggedBottom = bool;
		this.add('ragged-bottom', raggedBottom.lpStr);
	}
	raggedLast_ { |bool|
		raggedLast = bool;
		this.add('ragged-last', raggedLast.lpStr);
	}
	lpStr { |indent=0|
		var str;
		str = "\\" ++ name.asString + "{";
		items.do { |assoc| str = str ++ "\n\t" ++ assoc.key + "=" + assoc.value };
		str = str ++ "\n}";
		if (indent > 0) { str = str.replace("\n", "\n".catList("\t" ! indent)) };
		^str;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_HeaderBlock
--------------------------------------------------------------------------------------------------------------- */
LP_HeaderBlock : LP_Block {
	var <dedication, <title, <subtitle, <subsubtitle, <instrument, <poet, <composer;
	var <meter, <arranger, <tagline, <copyright;
	*new {
		^super.new('header');
	}
	add { |key, element|
		element = if (element.isKindOf(LP_Markup) || element.isKindOf(Boolean)) {
			element.lpStr(indent: 1);
		} { element.asString.quote };
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
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Dimension
LP_Dimension(2, 'in').lpStr;
LP_Dimension(2).lpStr;
- unit must be: \mm, \cm, \in, \pt
--------------------------------------------------------------------------------------------------------------- */
LP_Dimension {
	var <value, <unit;
	*new { |value, unit|
		^super.new.init(value, unit);
	}
	init { |argValue, argUnit|
		value = argValue.asString;
		if (argUnit.notNil) { unit = argUnit.asString };
	}
	lpStr {
		^if (unit.isNil) { value } { value ++ "\\" ++ unit };
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_SpacingVector
see: http://www.lilypond.org/doc/v2.19/Documentation/notation/flexible-vertical-spacing-paper-variables
LP_SpacingVector(12, 17, 48, 9).lpStr;
--------------------------------------------------------------------------------------------------------------- */
LP_SpacingVector {
	var <basicDistance, <minimumDistance, <padding, <stretchability;
	*new { |basicDistance=0, minimumDistance=0, padding=12, stretchability=0|
		^super.new.init(basicDistance, minimumDistance, padding, stretchability);
	}
	init { |argBasicDistance, argMinimumDistance, argPadding, argStretchability|
		basicDistance = argBasicDistance;
		minimumDistance = argMinimumDistance;
		padding = argPadding;
		stretchability = argStretchability;
	}
	lpStr {
		var str, keys;
		keys = #["basic-distance", "minimum-distance", "padding", "stretchability"];
		str = "#'(";
		[basicDistance, minimumDistance, padding, stretchability].do { |value, i|
			str = str + "(" ++ keys[i] + "." + value ++ ")";
		};
		str = str + ")";
		^str;
	}
}