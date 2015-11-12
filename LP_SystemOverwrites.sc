+ SequenceableCollection {
	intervals {
		var array;
		array = [];
		this.doAdjacentPairs { |a, b| array = array.add(b - a) };
		^array;
	}
	offsets {
		var array;
		array = ([0] ++ this.integrate);
		^array;
	}
	removeDuplicates {
		this.do { |each| if (this.occurrencesOf(each) > 1) { this.remove(each) } };
		^this;
	}
}

+ String {
	// generate incremental pathnames with date stamp (eg: "150316.0.ly", "150316.1.ly")
	*nextDateStampedPathname { |path, fileExt|
		var paths, dayStamp, index, outPath;
		path = path ?? { Platform.userHomeDir ++ "/" };
		if (path.last != $/) { path = path ++ "/" };
		paths = (path ++ "*").pathMatch;
		dayStamp = Date.getDate.dayStamp;
		index = paths.select { |each| each.contains(dayStamp) }.size;
		outPath = path ++ dayStamp ++ "." ++ index;
		if (fileExt.notNil) { outPath = outPath ++ "." ++ fileExt };
		^outPath;
	}
}