all: library


library:
	sbt clean compile publishLocal

doc:
	sbt doc
	cp -r target/scala-2.13/api/* docs/
