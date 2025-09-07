.PHONY: all library documentation test clean

SHELL := /bin/bash

VIVADO_PATH ?= ~/../vivado/Vivado
VIVADO_VERSION ?= 2022.2
TARGET_BOARD ?= kv260
TARGET_DESIGN ?= ConfigPortTest


library:
	sbt clean compile publishLocal

documentation:
	sbt doc
	cp -r target/scala-2.13/api/* docs/

sbt:
	@bash -c '\
	source $(VIVADO_PATH)/$(VIVADO_VERSION)/settings64.sh; \
	sbt "runMain example.$(TARGET_BOARD).$(TARGET_DESIGN)Verilog";'

bitstream:
	@bash -c '\
	source $(VIVADO_PATH)/$(VIVADO_VERSION)/settings64.sh; \
	sbt "runMain example.$(TARGET_BOARD).$(TARGET_DESIGN)Verilog"; \
	vivado -mode batch -source vivado/$(TARGET_DESIGN).tcl;'

test:
	@bash -c '\
	echo "Running implementation (sbt -> vivado -> bitstream) regression tests."; \
	success=0; fails=0; \
	$(MAKE) --no-print-directory clean; \
	for version in "2022.2" "2023.2" "2024.1"; do \
		for board in "kv260" "kr260"; do \
			for design in "ConfigPortTest" "Shell"; do \
				if $(MAKE) bitstream TARGET_BOARD=$$board TARGET_DESIGN=$$design VIVADO_VERSION=$$version; then \
					success=$$((success+1)); \
				else \
					fails=$$((fails+1)); \
				fi; \
				$(MAKE) --no-print-directory clean; \
			done; \
		done; \
	done; \
	echo "--------------------------------"; \
	echo "✔ Passed: $$success"; \
	echo "✘ Failed: $$fails"; \
	if [ "$$fails" -eq 0 ]; then exit 0; else exit 1; fi'

clean:
	rm -fr ./hw/gen/*
	rm -fr ./vivado/*
	rm -f ./*.log ./*.jou ./*.bit
