/* ---------------------------------------------------------------------------------------------------------------
// play a LP_Score
a = LP_Staff([LP_Measure([4, 8], [LP_Tuplet(3, [1,1,1]), LP_Tuplet(2, [1,1,1,1,1])])]);
a.selectBy(LP_Event).mask([2, 3, -1, 2]);
a.selectBy(LP_PitchEvent).replaceNotes([61, 62, 63, 71]);
b = a.deepCopy;
b.selectBy(LP_PitchEvent).replaceNotes([63, 61, 62, 64] + 2);
c = a.deepCopy;
c.selectBy(LP_PitchEvent).replaceNotes([63, 61, 62, 64].reverse + 5);
x = LP_Score([a, b, c]);
LP_Player(x).playMIDI;
// LP_Player(x).play;

// play a LP_Measure, LP_Voice or LP_Staff
a = LP_Staff([LP_Measure([4, 8], [LP_Tuplet(3, [1,1,1]), LP_Tuplet(2, [1,1,1,1,1])])]);
a.selectBy(LP_Event).mask([2, 3, -1, 2]);
a.selectBy(LP_PitchEvent).replaceNotes([61, 62, 63]);
LP_Player(a).playMIDI;

LP_File(x).write("test1.ly");
--------------------------------------------------------------------------------------------------------------- */
LP_Player {
	var <eventList;
	*new { |music|
		^super.new.init(music);
	}
	init { |music|
		eventList = LP_EventList(music);
	}
	// array of patterns where each item = an independent voice
	//!!! move this to LP_EventList::asPattern method ?
	patterns {
		var durs, type, midinotes;
		^eventList.collect { |voice|
			# durs, type, midinotes = voice.flop;
			Pbind(\dur, Pseq(durs), \midinote, Pseq(midinotes));
		};
	}
	play {
		//!!! bind instrument and pan settings here
		//!!! also settable tempo ??
		Ppar(this.patterns).play;
	}
	playMIDI {
		var device="IAC Driver", port="IAC Bus 1", midiOut, voices;

		if (MIDIClient.initialized == false) { MIDIClient.init };
		midiOut = midiOut ?? { MIDIOut.newByName(device, port).latency_(0.05) };

		voices = this.patterns.collect { |pattern, i|
			Pbindf(pattern, \proto, (type: 'midi', midiout: midiOut, midicmd: 'noteOn', chan: i));
		};

		Ppar(voices).play;
	}
}
/* ---------------------------------------------------------------------------------------------------------------
• LP_EventList
TODO:
- make LP_Event class
- once LP_Event class is made, LP_EventList is updated to contain a list of LP_Events

a = LP_Measure([4, 8], [LP_Tuplet(3, [1,1,1]), LP_Tuplet(2, [1,1,1,1,1])]);
LP_EventList(a).printAll; "";

a = LP_Staff([LP_Measure([4, 8], [LP_Tuplet(3, [1,1,1]), LP_Tuplet(2, [1,1,1,1,1])])]);
LP_EventList(a).printAll; "";

a = LP_Staff([LP_Measure([4, 8], [LP_Tuplet(3, [1,1,1]), LP_Tuplet(2, [1,1,1,1,1])])]);
b = a.deepCopy;
a.selectBy(LP_PitchEvent).replaceNotes((61,61.5..80));
b.selectBy(LP_PitchEvent).replaceNotes((80,79..40));
c = LP_Score([a, b]);
LP_EventList(c).printAll; "";
--------------------------------------------------------------------------------------------------------------- */
LP_EventList {
	var scoreObject;
	*new { |scoreObject|
		^super.new.init(scoreObject);
	}
	init { |argScoreObject|
		scoreObject = argScoreObject;
		switch (scoreObject.class,
			LP_Measure, { ^[LP_MeasureEventList(scoreObject)] },
			LP_Staff, { ^[LP_StaffEventList(scoreObject)] },
			LP_Score, { ^scoreObject.staves.collect { |staff| LP_StaffEventList(staff) } }
		);
	}
}

LP_MeasureEventList {
	var measure;
	*new { |measure|
		^super.new.init(measure);
	}
	init { |argMeasure|
		var note;
		measure = argMeasure;
		^measure.selectBy(LP_Event).components.collect { |event, i|
			note = switch(event.type, LP_Note, { event.note }, LP_Chord, { event.notes }, LP_Rest, \rest);
			[event.beatDuration, event.type, note];
		};
	}
}

LP_StaffEventList : LP_MeasureEventList {}