/* ----------------------------------------------------------------------------------------------------------------------
• Make sure that the lilypond bin matches the bin class variable in LP_Config
---------------------------------------------------------------------------------------------------------------------- */
LP_Config.bin;
LP_Config.version; // this should find the version number of your lilypond bin if it's installed correctly
LP_Config.language;
/* ----------------------------------------------------------------------------------------------------------------------
• Output files (ly and pdf) are written to the directory stored in LP_Config.dir
---------------------------------------------------------------------------------------------------------------------- */
LP_Config.dir;
/* ----------------------------------------------------------------------------------------------------------------------
• Basic score hierarchy
- TODO: LP_Voice (allowing more than one voice per LP_Staff), LP_StaffGroup, etc.
---------------------------------------------------------------------------------------------------------------------- */
(
var measure, staff, score;
measure = LP_Measure([4, 4], [1], [60]);
staff = LP_Staff([measure]);
score = LP_Score([staff]);
LP_File(score).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Score components can be accessed by indexing into the tree
- TODO: components can be accessed by name
---------------------------------------------------------------------------------------------------------------------- */
(
~measure = LP_Measure([4, 4], [1], [60]);
~staff = LP_Staff([~measure]);
~score = LP_Score([~staff]);
)

~score[0];
~score[0][0];
~score[0][0][0];
~staff[0];
~staff[0][0];
~measure[0];
~measure[0].note;
~measure[0].noteName;
~measure[0].duration.pair;
/* ----------------------------------------------------------------------------------------------------------------------
• LP_Measures use rhythm tree syntax
---------------------------------------------------------------------------------------------------------------------- */
(
var measure;
measure = LP_Measure([4, 4], [1, 1, 1, 4]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
var measure = LP_Measure([4, 4], [1, -1, 1, 4]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Tuplets can be specified by 'pure' rhythm trees, or by use of LP_Tuplet (usually easier to read)
---------------------------------------------------------------------------------------------------------------------- */
(
var measure = LP_Measure([4, 4], [2, [4, [1, 1, 1, 1, 1]], 3]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
var measure = LP_Measure([4, 4], [2, LP_Tuplet(4, [1, 1, 1, 1, 1]), 3]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Notes can be specified by midi number, by lilypond note names, or by a lilypond string
---------------------------------------------------------------------------------------------------------------------- */
(
// midi numbers
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], [61, 62]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// array of lilypond note names
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], ["ds''", "df''"]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// array of midi note names ('C4' = middle-C)
var measure = LP_Measure([4, 4], 1!8, ['A0', 'C#1', 'Db2', 'C##3', 'E4', 'B4', 'Bbb5', 'C6']);
measure.attach(LP_Clef('bass'));
measure[4].attach(LP_Clef('treble'));
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// lilypond string
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], "ds'' d''");
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// midi note name string
var measure = LP_Measure([4, 4], 1!8, "A0 C#1 Db2 C##3 E4 B4 Bbb5 C6");
measure.attach(LP_Clef('bass'));
measure[4].attach(LP_Clef('treble'));
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Quarter-tones are supported
---------------------------------------------------------------------------------------------------------------------- */
(
var measure = LP_Measure([4, 4], [1, 1, 1, 1], [72, 72.5, 71.5, 70.5]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
var measure = LP_Measure([4, 4], [1, 1, 1, 1], "dqs'' dqf'' dtqs'' dtqf''");
LP_File(LP_Score([LP_Staff([measure])])).write;
)


(
var measure = LP_Measure([4, 4], [1, 1, 1, 1], "D+5 D-5 D#+5 Db-5");
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Chords are specified as arrays of midi numbers, or in lilypond chord syntax
---------------------------------------------------------------------------------------------------------------------- */
(
// midi numbers
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], [61, [60, 64, 67, 70]]);
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// lilypond string
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], "ef' <c' e' g' bf'>");
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// midi note name string
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], "Eb4 <C4 E4 G4 Bb4>");
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• Music can be selected by LP_Leaf, LP_Event, LP_Rest, or LP_PitchEvent
---------------------------------------------------------------------------------------------------------------------- */
(
var a, b, c, d;
a = LP_Staff([LP_Measure([4, 4], [1, -1, 1, 4.0], [61, [60, 64, 67, 70]])]);
b = a.deepCopy;
c = a.deepCopy;
d = a.deepCopy;
a.selectBy(LP_Leaf).attach(LP_Markup("L")); // select by LP_Leaf
b.selectBy(LP_Event).attach(LP_Markup("E")); // select by LP_Event
c.selectBy(LP_PitchEvent).attach(LP_Markup("PE")); // select by LP_PitchEvent
d.selectBy(LP_Rest).attach(LP_Markup("R")); // select by LP_Rest
LP_File(LP_Score([a, b, c, d])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• 'at' syntax can be used to retrieve components from a selection
---------------------------------------------------------------------------------------------------------------------- */
(
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], [61, [60, 64, 67, 70]]);
measure.selectBy(LP_Leaf)[2].attach(LP_Articulation('>'));
LP_File(LP_Score([LP_Staff([measure])])).write;
)

(
// NB: the above example is equivalent to accessing the leaf directly from its container
var measure = LP_Measure([4, 4], [1, -1, 1, 4.0], [61, [60, 64, 67, 70]]);
measure[2].attach(LP_Articulation('>'));
LP_File(LP_Score([LP_Staff([measure])])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• dynamics, articulations, markups and spanners can be flexibly added to the music
---------------------------------------------------------------------------------------------------------------------- */
(
var measureFunc, staff1, staff2;
measureFunc = { LP_Measure([5, 8], [1, -3, LP_Tuplet(7, [1, 2, 1, 2, 2]), -3], { rrand(60, 72) } ! 6) };
staff1 = LP_Staff(measureFunc ! 2);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_Event)[(2..6)].attach(LP_Slur(\below));
staff2.selectBy(LP_Event)[(2..4)].attach(LP_Diminuendo());
staff2.selectBy(LP_Event)[(10..13)].attach(LP_Glissando());
staff2.selectBy(LP_Event)[(5..11)].attach(LP_Hairpin('p<mp'));
staff2.selectBy(LP_Event).at([2,3]).attach(LP_Dynamic('sf'));
staff2.selectBy(LP_Event).at([0,5,10]).attach(LP_Articulation('flageolet'));
staff2.selectBy(LP_Event).at([2,3,11]).attach(LP_Articulation('^'));
staff2.selectBy(LP_Event)[13].attach(LP_Articulation('espressivo', \below));
staff2.selectBy(LP_Event)[14].attach(LP_Markup("foobar", \above));
LP_File(LP_Score([staff1, staff2])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• selections of music can be mutated
---------------------------------------------------------------------------------------------------------------------- */
(
// replaceNotes 1
var staff1, staff2;
staff1 = LP_Staff(LP_Measure([4, 4], 1!8) ! 4);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_PitchEvent).replaceNotes((60, 61 .. 100));
LP_File(LP_Score([staff1, staff2])).write;
)

(
// replaceNotes 2
var staff1, staff2;
staff1 = LP_Staff(LP_Measure([4, 4], 1!8) ! 4);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_PitchEvent).replaceNotes("A0 C#1 Db2 C##3 E4 B4 Bbb5 C6");
LP_File(LP_Score([staff1, staff2])).write;
)

(
// transpose
var staff1, staff2;
staff1 = LP_Staff(LP_Measure([4, 4], 1!8) ! 4);
staff2 = staff1.deepCopy;
staff2.attach(LP_Clef('bass'));
staff2.selectBy(LP_PitchEvent).transpose((0, -1 .. staff2.leaves.lastIndex.neg));
LP_File(LP_Score([staff1, staff2])).write;
)

(
// fuse
var staff1, staff2;
staff1 = LP_Staff(LP_Measure([4, 4], 1!8) ! 2);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_PitchEvent).replaceNotes((60, 61 .. staff2.leaves.lastIndex + 60));
staff2.selectBy(LP_PitchEvent)[3..11].fuse;
LP_File(LP_Score([staff1, staff2])).write;
)

(
// mask (nb: this is still a little buggy!)
var staff1, staff2;
staff1 = LP_Staff(LP_Measure([4, 4], 1!8) ! 4);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_PitchEvent).replaceNotes((60, 61 .. staff2.leaves.lastIndex + 60));
staff2.selectBy(LP_PitchEvent).mask([3,7,-9,2,1,1,5,-4]);
LP_File(LP_Score([staff1, staff2])).write;
)

(
// partition
var staff1, staff2;
staff1 = LP_Staff([LP_Measure([4, 4], 1!8)]);
staff2 = staff1.deepCopy;
staff2.selectBy(LP_PitchEvent)[2].partition([-2,3]);
LP_File(LP_Score([staff1, staff2])).write;
)
/* ----------------------------------------------------------------------------------------------------------------------
• lilypond commands (set, override, etc.) can be added to score objects
- TODO: all lilypond commands to be available in SC-native syntax
---------------------------------------------------------------------------------------------------------------------- */
(
// show tuplet ratios
var measures, score;
measures = { LP_Measure([4, 4], [1, -1, 1, 4].scramble, [61, 62, 63]) } ! 4;
score = LP_Score([LP_Staff(measures)]);
score.proportionalNotationDuration_(LP_Duration(1, 12));
score.override('Score.TupletNumber.text', "#tuplet-number::calc-fraction-text");
LP_File(score).write;
)

(
// only show noteheads
var measures, score;
measures = { LP_Measure([4, 4], [1, -1, 1, 4].scramble, [61, 62, 63]) } ! 4;
score = LP_Score([LP_Staff(measures)]);
score.proportionalNotationDuration_(LP_Duration(1, 12));
score.override('Score.BarLine.stencil', "##f");
score.override('Score.TimeSignature.stencil', "##f");
score.override('Score.Stem.stencil', "##f");
score.override('Score.Tie.stencil', "##f");
score.override('Score.Beam.stencil', "##f");
score.override('Score.Flag.stencil', "##f");
score.override('Score.Dots.stencil', "##f");
score.override('Score.TupletNumber.stencil', "##f");
score.override('Score.TupletBracket.stencil', "##f");
score.override('Score.NoteHead.duration-log', "#4");
score.override('Score.Rest.transparent', "##t");
LP_File(score).write;
)