all: library


library:
	sbt clean compile publishLocal