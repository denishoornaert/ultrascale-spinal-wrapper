.PHONY: all library documentation test clean

VIVADO_PATH ?= ~/../vivado/Vivado/
VIVADO_VERSION ?= 2022.2
TARGET_DESIGN ?= ConfigPortTest

all: library

library:
	sbt clean compile publishLocal

documentation:
	sbt doc
	cp -r target/scala-2.13/api/* docs/

bitstream:
	bash -c '\
		set -e; \
		. $(VIVADO_PATH)/$(VIVADO_VERSION)/settings64.sh; \
		sbt "runMain example.$(TARGET_DESIGN)Verilog"; \
		vivado -mode batch -source vivado/$(TARGET_DESIGN).tcl; \
	'

clean:
	rm -fr ./hw/gen/*
	rm -fr ./vivado/*
	rm -f ./*.log ./*.jou ./*.bit