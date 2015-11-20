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
	var music, path;
	var <headerBlock, <layoutBlock, <paperBlock, <scoreContextBlock;
	var <defaultPaperSize=#['a4', 'landscape'], <>globalStaffSize=16;
	*new { |music|
		^super.new.init(music);
	}
	init { |argMusic|
		music = argMusic;
		headerBlock = LP_HeaderBlock();
		layoutBlock = LP_LayoutBlock();
		paperBlock = LP_PaperBlock();
		scoreContextBlock = LP_ContextBlock('Score');
	}
	defaultPaperSize_ { |paper='a4', orientation='landscape'|
		defaultPaperSize = [paper, orientation];
	}
	initDefaults {
		/*scoreContextBlock.override('Score.SpacingSpanner.strict-note-spacing', "##t");
		scoreContextBlock.override('SpacingSpanner.common-shortest-duration', "#(ly:make-moment 1/2)");
		scoreContextBlock.override('TupletNumber.text', "#tuplet-number::calc-fraction-text");
		scoreContextBlock.override('TupletBracket.direction', "#UP"); // this.add('tupletUp');*/
		headerBlock.tagline_(false);
		scoreContextBlock.add('tupletFullLength', "##t");
		scoreContextBlock.addCommand('numericTimeSignature');
		layoutBlock.addContextBlock(scoreContextBlock);
		paperBlock.margin_(20, 20, 20, 20);
		paperBlock.indent_(0);
		paperBlock.systemSystemSpacing_(0, 0, 10, 0);
		paperBlock.scoreSystemSpacing_(20, 20, 10, 0);
	}
	write { |argPath, openPDF=true|
		var paper, orientation;

		path = argPath ?? { String.nextDateStampedPathname(LP_Config.dir, "ly") };
		# paper, orientation = defaultPaperSize.collect { |each| each.asString };
		this.initDefaults;

		File.use(path, "w", { |file|
			file.write("%" + Date.getDate.format("%Y-%m-%d %H:%M")).write("\n\n"); // date stamp
 			file.write("\\version" + LP_Config.version.asCompileString).write("\n");
			file.write("\\language" + LP_Config.language.asCompileString).write("\n\n");

			//!!! TODO: INCLUDES
			//file.write("\\include \"microtonal.ily\"").write("\n\n");

			file.write("#(set-default-paper-size" + paper.quote + "'" ++ orientation ++ ")").write("\n");
			file.write("#(set-global-staff-size" + globalStaffSize.asString ++ ")").write("\n\n");

			file.write(headerBlock.lpStr).write("\n\n");
			file.write(layoutBlock.lpStr).write("\n\n");
			file.write(paperBlock.lpStr).write("\n");
			if (music.isArray) { music.do { |each| file.write(each.lpStr) } } { file.write(music.lpStr) };
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


