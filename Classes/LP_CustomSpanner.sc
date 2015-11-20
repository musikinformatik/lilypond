/* ---------------------------------------------------------------------------------------------------------------
• LP_CustomSpanners override grob properties internally
• they do not have corresponding Lilypond objects

a = LP_Measure([4, 8], [1, 3, LP_Tuplet(7, [1, 2, 1, 2, 2]), 3], { rrand(60, 72) } ! 8);
b = LP_Staff(a!2);
b[(0..12)].attach(LP_ArrowText("tasto", "pont."));
LP_File(LP_Score([b])).write("test1.ly");
--------------------------------------------------------------------------------------------------------------- */
LP_CustomSpanner {
	var spanner;
}

LP_ArrowText : LP_CustomSpanner {
	var <lyObj="TextSpanner";
	//!!! should arguments be LP_Markups rather than strings ??
	*new { |leftText, rightText|
		^super.new.init(leftText, rightText);
	}
	init { |leftText, rightText|
		spanner = LP_TextSpanner();
		spanner.override(\style, "'line".asSymbol); //!!! an object is needed for quoting scheme objects
		spanner.override(\bound_details, \right, \arrow, true);
		spanner.override(\arrow_length, 1);
		spanner.override(\arrow_width, 0.25);
		if (leftText.notNil) { spanner.override(\bound_details, \left, \text, (leftText + "").asString) };
		if (rightText.notNil) { spanner.override(\bound_details, \right, \text, ("" + rightText).asString) };
		//spanner.override(\bound_details, \right, \padding, 0.6); //!!! padding should be relative to string length ?
		spanner.override(\bound_details, \left, \stencil_align_dir_y, 'center');
		spanner.override(\bound_details, \right, \stencil_align_dir_y, 'center');
		^spanner;
	}
}