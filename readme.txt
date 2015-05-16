# Bugs Language

Bugs Language assignment for CIT 594 (Spring 2015)



This program acts as a compiler and interpreter for Bugs programs. The Bugs language was invented by Dave Matuszek, professor of CIT 594. The purpose of the Bugs language is to create patterns by moving small "bugs" around the screen of a user interface, which this project does. (For a much more detailed explanation of the Bugs language, see the links at the bottom of this document.)

This program prompts the user to load in a Bugs program and then parses it to create an execution tree for each of the bugs specified in the program, as well as an optional "All Bugs" portion. The program then interprets these trees sequentially so that each bug's thread may only execute one movement command per turn. Each of the movement commands is visualized through the user interface.

Sample Bugs programs are available in the Sample Bugs Programs directory.

Reference Links

Bugs Language description:
http://www.cis.upenn.edu/~matuszek/cit594-2015/Assignments/bugs-language.html

Bugs EBNF Grammar:
http://www.cis.upenn.edu/~matuszek/cit594-2015/Assignments/bugs-grammar-v2.html