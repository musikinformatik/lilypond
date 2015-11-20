/* ---------------------------------------------------------------------------------------------------------------
• LP_Duration

LP_Duration(1, 4).pair;
LP_Duration(0.25).pair; // initialize from number
LP_Duration(pi).pair; // irrational durations ??

LP_Duration(4, 4).lpStr;
LP_Duration(3, 8).lpStr;
LP_Duration(7, 16).lpStr;
LP_Duration(17, 16).lpStr; //!!! tied -- is stringified only after LP_TieContainer is created -- this needs fixing

// fuse: add durations, or tie fused components and then rewrite to meter ??
LP_Duration(3, 8).add(LP_Duration(3, 8)).pair
[LP_Duration(3, 8), LP_Duration(1, 4), LP_Duration(7, 16)].reduce('add').pair;

LP_Duration(3, 8).div(6).pair;
LP_Duration(3, 8).div(10).pair; //!!!
LP_Duration(3, 8).mul(2).pair;
LP_Duration(3, 8).mul(1.5).pair; //!!!

LP_Duration(3, 8).split([1, 2]).collect { |e| e.pair };
LP_Duration(3, 8).split([2, 4]).collect { |e| e.pair };
LP_Duration(3, 8).split([1, 1, 1, 1]).collect { |e| e.pair };

LP_Duration(0, 16).isAssignable;
LP_Duration(5, 16).isAssignable;
LP_Duration(4, 16).isAssignable;
LP_Duration(4, 12).isAssignable;
 ---------------------------------------------------------------------------------------------------------------*/
LP_Duration {
	classvar <base, <dot1, <dot2, <dotted, <nonPartitioned, <partitioned;
	var <numerator, <denominator, <pair;

	//!!! remove and update to use isAssignable
	*initClass {
		base = Array.geom(8, 1, 2); // base values: no dots
		dot1 = Array.geom(6, 3, 2); // 1 dot
		dot2 = Array.geom(5, 7, 2); // 2 dots
		dotted = [dot1, dot2].reduce(\union).sort;
		nonPartitioned = [base, dot1, dot2].reduce(\union).sort;
		partitioned = (1..128).symmetricDifference(nonPartitioned);
	}
	*new { |numerator, denominator|
		^if (denominator.isNil) {
			this.new(*numerator.asFraction); // allow initialization with a Number
		} {
			super.new.init(numerator, denominator);
		};
	}
	init { |argNumerator, argDenominator|
		var fraction;
		fraction = [argNumerator, argDenominator];
		# numerator, denominator = (fraction / fraction.reduce(\gcd)).asInteger; // simplify fraction
		pair = [numerator, denominator];
	}
	isAssignable {
		var index, bool;
		index = numerator.asBinaryString.indexOf($1);
		if (index.isNil) { ^false };
		bool = numerator.asBinaryString[index..].contains("01").not;
		bool = bool && denominator.isPowerOfTwo;
		^bool;
	}
	asDuration {
		^this;
	}
	numDots {
		^case
		{ base.includes(numerator) } { 0 }
		{ dot1.includes(numerator) } { 1 }
		{ dot2.includes(numerator) } { 2 }
		{ partitioned.includes(numerator) } { nil };
	}
	isDotted {
		^dotted.includes(numerator);
	}
	// duration in beats, where 1/4 = 1, 1/8 = 0.5, etc.
	beatDuration {
		^pair.reduce('/') * 4;
	}
	// returns a pair (or triple) of durations
	//!!! TODO: use recursion/back-tracking: if a pair is not found, look for a triple; if a triple is not found, etc..
	*partitionNum { |num|
		var out;
		if (nonPartitioned.includes(num)) { ^num };
		out = all {:[a, b],
			a <- nonPartitioned, b <- nonPartitioned,
			[a, b].sum == num,
			:while (a < b) && (b < num);
		};
		if (out.isNil) {
			out = all {:[a, b, c],
				a <- nonPartitioned, b <- nonPartitioned, c <- nonPartitioned,
				[a, b, c].sum == num,
				:while (a < b) && (b < c) && (c < num);
			}
		};
		//^out; //!!! TODO partitions (select best preference for rhythmic context)
		//^out[0].collect { |num| LP_Duration(num, denominator) };
		^out[0];
	}
	add { |duration|
		var numer, denom;
		denom = lcm(this.denominator, duration.denominator);
		numer = [this, duration].collect { |each| each.numerator * (denom / each.denominator) }.sum.asInteger;
		^LP_Duration(numer, denom);
	}
	//!!! TODO: deal with fractional number arguments
	mul { |number|
		^LP_Duration(numerator * number, denominator);
	}
	// div returns nil if duration is not divisible by number (i.e. denominator would not be power of two)
	//!!! TODO: LogicalTies for durs of 5, 9, 11, etc..
	div { |number|
		var tempPair;
		tempPair = case
		{ number == 1 } { [numerator, denominator] }
		{ numerator % number == 0 || { number % numerator == 0 } } {
			if ((denominator * (number/numerator)).asInteger.isPowerOfTwo) {
				[(numerator / number), denominator] * (number/numerator);
			};
		}
		{ (denominator * number).isPowerOfTwo } { [numerator, denominator * number] };
		^if (tempPair.notNil) { LP_Duration(*tempPair.asInteger) };
	}
	ratio { |duration|
		var denom;
		denom = [this, duration].collect { |each| each.denominator }.maxItem;
		^[this, duration].collect { |each| each.numerator * (denom / each.denominator) }.asInteger;
	}
	// split returns nil if duration is not divisible by sum of ratios (i.e. denominator would not be power of two)
	//!!! TODO: handling of nonAssignable durations
	split { |ratios, tieSplitLeaves=false|
		var sum = ratios.sum;
		^if (this.div(sum).notNil) { ratios.collect { |mul| this.mul(mul).div(sum) } } { nil };
	}
}

LP_Offset : LP_Duration {
}

//!!! this should be a LP_Indicator
LP_TimeSignature : LP_Duration {
	var <duration;
	init { |argNumerator, argDenominator|
		# numerator, denominator = [argNumerator, argDenominator];
		pair = [numerator, denominator];
		duration = LP_Duration(numerator, denominator);
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_Timespan
• TODO
- can be initialised by LP_Offsets, LP_Durations, numerator/denominator pairs, or Integers
 ---------------------------------------------------------------------------------------------------------------*/
LP_Timespan {
	var <startOffset, <endOffset;
	*new { |startOffset, endOffset|
		^super.new.init(startOffset, endOffset);
	}
	init { |argStartOffset, argEndOffset|
		startOffset = argStartOffset;
		endOffset = argEndOffset;
	}
	duration {
		^(startOffset - endOffset);
	}
	scale {
		// TODO: perhaps mul ?
	}
	translate {
		// TODO: rename add ?
	}
	union { |aLP_Timespan|
		// TODO: rename combine ?
	}
	sect { |aLP_Timespan|
		// TODO: rename mask ?
	}
	symmetricDifference { |aLP_Timespan|
		// TODO: rename ?
	}
	append { |aLP_Timespan|
		// TODO: rename concat / ++ ?
	}
	prepend { |aLP_Timespan|
		// TODO
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_AnnotatedTimespan
- any arbitrary objects can be an annotation
- see Oberholtzer: 80
x = LP_AnnotatedTimespan(1, 2, (durs: [2, 4, 5], notes: [61, 62, 63], dynamic: 'ppp'));
x.startOffset;
x.endOffset;
x.annotation;
---------------------------------------------------------------------------------------------------------------*/
LP_AnnotatedTimespan : LP_Timespan {
	var <annotation;
	*new { |startOffset, endOffset, annotation|
		^super.new(startOffset, endOffset).init1(annotation);
	}
	init1 { |argAnnotation|
		annotation = argAnnotation;
	}
}
