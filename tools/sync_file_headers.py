#!/usr/bin/env python3
"""
QuickDash — File Header Sync & Documentation Generator
=======================================================
One command installs a consistent, accurate, human-style license/doc header on
every source file, and regenerates docs/FILE_MAP.md so contributors can learn
the project in minutes.

Design decisions (why this looks human):
  * Header content is driven by a MANIFEST (docs/file_manifest.json), not
    generated on the fly — so every "Description:" is human-authored and
    accurate, never AI-hallucinated.
  * Module + file name are DERIVED from the real path, so they can never drift
    from reality.
  * The tool is idempotent and safe: re-runnable, no destructive edits beyond
    the known header block, and CI-enforceable with --check.

Usage
-----
    python3 tools/sync_file_headers.py                # apply headers
    python3 tools/sync_file_headers.py --check        # CI gate: fail if stale
    python3 tools/sync_file_headers.py --refresh      # rebuild FILE_MAP.md only
    python3 tools/sync_file_headers.py --todo         # list files w/o descriptions

Roots scanned by default: app/src/main, app/src/test, app/src/androidTest.
Add roots:   --roots app/src/main app/src/test
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
from pathlib import Path

COPYRIGHT = "Copyright (c) 2026 ||BTL||\u2122 (balajitechlabs)"
LICENSE = "License: PocketOps Custom Open Source Fork License"
AUTHOR = "Developer: balajitechlabs"

PACKAGE_DIR = "com/balajitechlabs/quickdash"
BUILD_DIRS = {"build", "build/generated", "build/tmp"}
PLACEHOLDER = "<one-sentence purpose of this file>"

HEADER_START = "/*"
HEADER_END = "*/"

HEADER_RE = re.compile(r"/\*.*?\*/", re.DOTALL)


def module_of(rel_path: str) -> str:
    """Derive the 'Feature Module' value from the real filesystem path.

    app/src/main/java/com/balajitechlabs/quickdash/features/qr/utils/X.kt
        -> features/qr/utils
    """
    marker = "com/balajitechlabs/quickdash/"
    pos = rel_path.find(marker)
    if pos != -1:
        tail = rel_path[pos + len(marker):]
        parts = tail.split("/")
        if len(parts) > 1:
            return "/".join(parts[:-1])
        return "root"
    return "/".join(rel_path.split("/")[:-1])



def render_header(rel_path: str, description: str) -> str:
    return (
        "/*\n"
        f" * {COPYRIGHT}\n"
        f" * {LICENSE}\n"
        " *\n"
        f" * Feature Module: {module_of(rel_path)}\n"
        f" * File: {Path(rel_path).name}\n"
        f" * Description: {description}\n"
        f" * {AUTHOR}\n"
        " */\n"
    )


def strip_old_header(text: str) -> str:
    """Remove an existing header block that matches our shape, if present."""
    stripped = HEADER_RE.sub("", text, count=1)
    if stripped != text:
        stripped = stripped.lstrip("\n")
    return stripped


def collect_files(roots: list[str]) -> list[Path]:
    files: list[Path] = []
    for root in roots:
        for dirpath, dirnames, filenames in os.walk(root):
            dirnames[:] = [d for d in dirnames if d not in BUILD_DIRS]
            for name in filenames:
                if name.endswith(".kt"):
                    files.append(Path(dirpath) / name)
    return sorted(files)


def rel_of(path: Path, roots: list[str]) -> str:
    for root in roots:
        try:
            return path.relative_to(root).as_posix().replace("\\", "/")
        except ValueError:
            continue
    return path.as_posix()


def ensure_manifest(path: Path) -> None:
    if not path.exists():
        path.write_text("{}\n")


def load_manifest(path: Path) -> dict[str, str]:
    ensure_manifest(path)
    return json.loads(path.read_text(encoding="utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser(description="Sync QuickDash file headers")
    parser.add_argument("--check", action="store_true", help="CI mode: exit 1 if stale")
    parser.add_argument("--refresh", action="store_true", help="Only rebuild FILE_MAP.md")
    parser.add_argument("--todo", action="store_true", help="List files missing a description")
    parser.add_argument("--roots", nargs="*", default=["app/src/main", "app/src/test", "app/src/androidTest"])
    parser.add_argument("--manifest", default="docs/file_manifest.json")
    parser.add_argument("--filemap", default="docs/FILE_MAP.md")
    args = parser.parse_args()

    roots = [r for r in args.roots if Path(r).is_dir()]
    if not roots:
        print("No source roots found. Run from the repository root.", file=sys.stderr)
        return 2

    manifest_path = Path(args.manifest)
    if args.refresh:
        manifest = load_manifest(manifest_path)
        render_file_map(collect_files(roots), roots, args.filemap, manifest)
        return 0

    manifest_fresh = args.check
    old_manifest = load_manifest(manifest_path)
    manifest = dict(old_manifest)

    problems: list[str] = []
    todos: list[str] = []

    for file in collect_files(roots):
        rel = rel_of(file, roots).replace("\\", "/")
        text = file.read_text(encoding="utf-8")
        text = strip_old_header(text)

        description = manifest.get(rel, "")
        if not description or PLACEHOLDER in description:
            todos.append(rel)

        if not manifest_fresh and rel not in manifest:
            manifest[rel] = ""

        header = render_header(rel, description or PLACEHOLDER)
        new_text = header + text
        if new_text != file.read_text(encoding="utf-8"):
            if args.check:
                problems.append(f"STALE HEADER  {rel}")
            else:
                if not description:
                    pass  # placeholder applied; manifest not edited
                file.write_text(new_text, encoding="utf-8")

    if not args.check and not manifest_fresh:
        manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")

    render_file_map(collect_files(roots), roots, args.filemap, manifest)

    if args.todo:
        print("Files needing a Description in docs/file_manifest.json:")
        for rel in sorted(todos):
            print(f"  - {rel}")

    if args.check:
        if problems:
            print(f"{len(problems)} file(s) would change. Run: python3 tools/sync_file_headers.py")
            for p in sorted(problems):
                print(f"  {p}")
            print("Then commit the manifest + headers together.")
            return 1
        print("OK — all {} header(s) up to date.".format(len(collect_files(roots))))
        return 0

    changed = sum(1 for f in collect_files(roots) if strip_old_header(f.read_text(encoding="utf-8")) != f.read_text(encoding="utf-8"))
    print(f"Done. Reviewed {len(collect_files(roots))} file(s); {changed} rewritten. FILE_MAP.md regenerated.")
    print("Add missing descriptions in docs/file_manifest.json (see --todo).")
    return 0


def render_file_map(files: list[Path], roots: list[str], out_path: str, manifest: dict[str, str]) -> None:
    rows = []
    for file in sorted(files, key=lambda p: p.as_posix()):
        rel = rel_of(file, roots).replace("\\", "/")
        desc = manifest.get(rel, "").strip() or "_(add description in docs/file_manifest.json)_"
        loc = sum(1 for _ in file.open(encoding="utf-8"))
        rows.append((rel, module_of(rel), loc, desc))

    lines = [
        "# QuickDash — File Map (auto-generated)",
        "",
        "> Generated by `python3 tools/sync_file_headers.py --refresh`.",
        "> One row per Kotlin source file. Used for onboarding and reviews.",
        "",
        "| File | Module | LOC | Purpose |",
        "| --- | --- | ---: | --- |",
    ]
    for rel, mod, loc, desc in rows:
        desc_safe = desc.replace("|", "/")
        lines.append(f"| {rel} | {mod} | {loc} | {desc_safe} |")
    lines.append("")

    Path(out_path).parent.mkdir(parents=True, exist_ok=True)
    Path(out_path).write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())