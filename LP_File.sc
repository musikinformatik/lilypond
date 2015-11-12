/* ---------------------------------------------------------------------------------------------------------------
LP_Config.bin;
LP_Config.dir;
LP_Config.version;
LP_Config.language;
--------------------------------------------------------------------------------------------------------------- */
LP_Config {
	classvar <>bin="/Applications/LilyPond.app/Contents/Resources/bin/lilypond", <>dir;
	classvar >version, <>language="english";
	*initClass {
		dir = Platform.userAppSupportDir ++ "/lilypond/";
		if (File.exists(dir).not) { unixCmd("mkdir" + dir.shellQuote) };
	}
	*version {
		^version ?? {
			var str;
			str = (LP_Config.bin + "--version").unixCmdGetStdOut;
			str.copyRange(*[str.findRegexp("\\s[0-9]")[0][0]+1, str.find("\n")-1]);
		};
	}
}

LP_File {
	var score, path;
	var <headerBlock, <layoutBlock, <paperBlock;
	var <scoreContextBlock;
	*new { |score|
		^super.new.init(score);
	}
	init { |argScore|
		score = argScore;
		headerBlock = LP_HeaderBlock();
		layoutBlock = LP_LayoutBlock();
		paperBlock = LP_PaperBlock();

		scoreContextBlock = LP_ContextBlock('Score');
	}
	initTempDefaults {
		//headerBlock.add('title', "Symphony No.1 etc.");
		headerBlock.add('tagline', "##f");

		//scoreContextBlock.override('Score.SpacingSpanner.strict-note-spacing', "##t");
		//scoreContextBlock.override('SpacingSpanner.common-shortest-duration', "#(ly:make-moment 1/2)");
		//scoreContextBlock.override('TupletNumber.text', "#tuplet-number::calc-fraction-text");
		scoreContextBlock.add('tupletFullLength', "##t");
		scoreContextBlock.override('TupletBracket.direction', "#UP"); // this.add('tupletUp');
		scoreContextBlock.addCommand('numericTimeSignature');

		layoutBlock.addContextBlock(scoreContextBlock);
		layoutBlock.add('indent', "#0");

		//paperBlock.add('page-count', "#2"); // force page count
		//paperBlock.add('system-count', "#5");
		paperBlock.add('top-margin', "#15");
		paperBlock.add('bottom-margin', "#15");
		paperBlock.add('left-margin', "#20");
		paperBlock.add('right-margin', "#20");
		paperBlock.add("system-system-spacing #'basic-distance".asSymbol, "#14");
		paperBlock.add('score-system-spacing',
			"#'((basic-distance . 30) (minimum-distance . 24) (padding . 1) (stretchability . 12))");
		paperBlock.add('ragged-bottom', "##t");

	}
	write { |argPath, openPDF=true|
		path = argPath ?? { String.nextDateStampedPathname(LP_Config.dir, "ly") };
		this.initTempDefaults; //!!! REMOVE ?

		File.use(path, "w", { |file|
			file.write("%" + Date.getDate.format("%Y-%m-%d %H:%M")).write("\n\n"); // date stamp
 			file.write("\\version" + LP_Config.version.asCompileString).write("\n\n");
			file.write("\\language" + LP_Config.language.asCompileString).write("\n\n");

			//!!! ----- TODO: INCLUDES
			//file.write("\\include \"microtonal.ily\"").write("\n\n");

			//!!! ----- TODO: PAPER DEFAULTS
			file.write("#(set-default-paper-size \"a4\" 'landscape)\n");
			//file.write("#(set-default-paper-size \"a4\" 'portrait)\n");
			file.write("#(set-global-staff-size 16)\n\n");

			file.write(headerBlock.lpStr).write("\n\n");
			file.write(layoutBlock.lpStr).write("\n\n");
			file.write(paperBlock.lpStr).write("\n\n");
			file.write(score.lpStr);
		});

		if (openPDF) { this.open };
	}
	open {
		var infile, outfile;
		infile = path.shellQuote;
		outfile = path.splitext[0].shellQuote;
		unixCmd(LP_Config.bin + "-o" + outfile + infile, action: { |result|
			if (result == 0) { unixCmd("open" + outfile ++ ".pdf") };
		});
	}
}


