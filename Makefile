HARNESS := ./scripts/harness.sh

.PHONY: doctor verify backend-test backend-unit backend-context backend-run deps-up deps-down docs-serve frontend-install frontend-run frontend-test

doctor:
	$(HARNESS) doctor

verify:
	$(HARNESS) verify

backend-test:
	$(HARNESS) backend:test

backend-unit:
	$(HARNESS) backend:unit

backend-context:
	$(HARNESS) backend:context

backend-run:
	$(HARNESS) backend:run

deps-up:
	$(HARNESS) deps:up

deps-down:
	$(HARNESS) deps:down

docs-serve:
	$(HARNESS) docs:serve

frontend-install:
	$(HARNESS) frontend:install

frontend-run:
	$(HARNESS) frontend:run

frontend-test:
	$(HARNESS) frontend:test
