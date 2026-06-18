#!/usr/bin/env python3
"""Verify Trust & Safety P0 contract guardrails across docs and app contracts.

This is intentionally lightweight: it catches drift in the agreed MVP contract while
backend/frontend implementation lanes evolve in parallel.
"""
from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]

CHECKS: list[tuple[str, str, str | re.Pattern[str]]] = [
    ("SPEC documents P0 Trust & Safety", "docs/SPEC.md", "### P0. Trust & Safety MVP"),
    ("SPEC keeps warning source as preflight API", "docs/SPEC.md", "POST /api/v1/item-policy/warnings"),
    ("SPEC forbids CANCELLED/REJECTED additions", "docs/SPEC.md", "`CANCELLED` 또는 `REJECTED` 상태를 추가하지 않는다"),
    ("SPEC preserves buyer-to-seller review MVP", "docs/SPEC.md", "완료 거래 구매자→판매자"),
    ("SPEC excludes description from report unique key", "docs/SPEC.md", "`description`은 unique key에 포함하지 않는다"),
    ("REFERENCE lists item policy preflight", "docs/REFERENCE.md", "POST | `/api/v1/item-policy/warnings`"),
    ("REFERENCE lists report API", "docs/REFERENCE.md", "POST /api/v1/reports"),
    ("REFERENCE lists block API", "docs/REFERENCE.md", "POST /api/v1/blocks/{memberId}"),
    ("REFERENCE documents CANCELED-only taxonomy", "docs/REFERENCE.md", "`CANCELLED` 또는 `REJECTED` 상태를 추가하지 않는다"),
    ("Frontend service calls policy preflight", "knock-frontend/services/index.ts", "'/item-policy/warnings'"),
    ("Frontend types restrict warning severity", "knock-frontend/types.ts", "export type ItemPolicyWarningSeverity = 'NONE' | 'WARNING';"),
    ("Backend exposes policy preflight", "knock-backend/core/core-api/src/main/java/com/knock/core/api/controller/v1/ItemPolicyController.java", "/api/v1/item-policy/warnings"),
    ("Backend policy MVP has no blocking severity", "knock-backend/core/core-api/src/main/java/com/knock/core/domain/itempolicy/ItemPolicyWarningService.java", "private static final String WARNING = \"WARNING\";"),
    ("REST Docs includes policy preflight", "knock-backend/core/core-api/src/docs/asciidoc/index.adoc", "api/v1/item-policy/warnings"),
    ("Frontend safe meetup checklist is rendered", "knock-frontend/components/ReservationSafetyModal.tsx", "Safe meetup checklist"),
    ("Frontend blocked interaction is not treated as login", "knock-frontend/pages/ItemDetail.tsx", "code !== BLOCKED_INTERACTION_CODE"),
    ("Frontend policy link has a route", "knock-frontend/App.tsx", "/docs/marketplace-item-policy"),
    ("Policy page documents safe trade guidance", "knock-frontend/pages/MarketplaceItemPolicy.tsx", "Safe meetup guidance"),
]

FORBIDDEN: list[tuple[str, str, re.Pattern[str]]] = [
    ("Docs must not introduce British CANCELLED status", "docs", re.compile(r"\bCANCELLED\b(?!` 또는 `REJECTED` 상태를 추가하지 않는다)")),
    ("Docs must not introduce REJECTED reservation status", "docs", re.compile(r"\bREJECTED\b(?!` 상태를 추가하지 않는다)")),
]


def read(path: str) -> str:
    target = ROOT / path
    if target.is_dir():
        return "\n".join(p.read_text(errors="replace") for p in sorted(target.rglob("*.md")))
    return target.read_text(errors="replace")


def main() -> int:
    failures: list[str] = []
    for label, path, needle in CHECKS:
        text = read(path)
        ok = bool(needle.search(text) if hasattr(needle, "search") else needle in text)
        print(f"{'PASS' if ok else 'FAIL'}: {label}")
        if not ok:
            failures.append(label)

    for label, path, pattern in FORBIDDEN:
        text = read(path)
        matches = pattern.findall(text)
        ok = not matches
        print(f"{'PASS' if ok else 'FAIL'}: {label}")
        if not ok:
            failures.append(f"{label}: {matches[:5]}")

    if failures:
        print("\nTrust & Safety contract verification failed:", file=sys.stderr)
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
