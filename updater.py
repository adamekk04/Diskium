# VIBECODED, made for updating dependencies, etc.

import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent

GRADLE_PROPERTIES = ROOT / "gradle.properties"
LIB_VERSIONS_TOML = ROOT / "gradle" / "libs.versions.toml"
ROOT_BUILD = ROOT / "build.gradle.kts"
README = ROOT / "README.md"
GRADLE_WRAPPER = ROOT / "gradle" / "wrapper" / "gradle-wrapper.properties"
PAPER_PLUGIN_YML = ROOT / "paper" / "src" / "main" / "resources" / "paper-plugin.yml"
GRADLEW = ROOT / "gradlew"

PLUGIN_KEYS = {"run-paper": "run-task", "shadow": "shadow"}

MAIN_CHOICES = [
    ("diskium", "Diskium (plugin version)"),
    ("paper", "Paper (paper-api + minecraft)"),
    ("java", "Java (toolchain)"),
    ("run-paper", "Plugin: run-paper"),
    ("shadow", "Plugin: shadow"),
    ("gradle", "Gradle wrapper"),
]

ALL_TARGETS = [name for name, _ in MAIN_CHOICES]


def read_file(path):
    return path.read_text(encoding="utf-8")


def write_file(path, content):
    path.write_text(content, encoding="utf-8")


def current_version(name):
    if name == "diskium":
        m = re.search(r"(?m)^version\s*=\s*(\S+)", read_file(GRADLE_PROPERTIES))
    elif name == "paper":
        m = re.search(r'(?m)^paper-api\s*=\s*"([^"]+)"', read_file(LIB_VERSIONS_TOML))
    elif name == "java":
        m = re.search(r"JavaLanguageVersion\.of\((\d+)\)", read_file(ROOT_BUILD))
    elif name in PLUGIN_KEYS:
        key = PLUGIN_KEYS[name]
        m = re.search(rf"(?m)^{re.escape(key)}\s*=\s*\"([^\"]+)\"", read_file(LIB_VERSIONS_TOML))
    elif name == "gradle":
        m = re.search(r"gradle-([\d.]+)-bin\.zip", read_file(GRADLE_WRAPPER))
    else:
        return "?"
    return m.group(1) if m else "?"


class UpdateError(RuntimeError):
    pass


class Updater:
    def __init__(self, dry_run=False):
        self.dry_run = dry_run
        self.changed = []

    def replace(self, path, pattern, replacement, description):
        content = read_file(path)
        new_content, count = re.subn(pattern, replacement, content, count=1)
        if count == 0:
            raise UpdateError(f"pattern '{pattern}' not found in {path.name}")
        self.changed.append((path, description))
        if not self.dry_run:
            write_file(path, new_content)


def update(updater, name, version):
    if name == "diskium":
        updater.replace(GRADLE_PROPERTIES, r"(?m)^version\s*=\s*\S+$", f"version={version}", "plugin version")
        updater.replace(PAPER_PLUGIN_YML, r"(?m)^version:\s*'.*'$", f"version: '{version}'", "version in paper-plugin.yml")
    elif name == "paper":
        m = re.match(r"(?P<mc>\d+\.\d+)", version)
        if m and m.group("mc") == version:
            minecraft, paper_api = version, f"{version}.build.+"
        elif m:
            minecraft, paper_api = m.group("mc"), version
        else:
            cur = re.search(r'(?m)^minecraft\s*=\s*"([^"]+)"', read_file(LIB_VERSIONS_TOML))
            minecraft = cur.group(1) if cur else version
            paper_api = version
        updater.replace(LIB_VERSIONS_TOML, r'(?m)^minecraft\s*=\s*"[^"]*"$', f'minecraft = "{minecraft}"', "minecraft")
        updater.replace(LIB_VERSIONS_TOML, r'(?m)^paper-api\s*=\s*"[^"]*"$', f'paper-api = "{paper_api}"', "paper-api")
        updater.replace(README, r"(?m)^- Paper \*\*[^*]+\*\*$", f"- Paper **{minecraft}**", "README (Paper)")
        updater.replace(PAPER_PLUGIN_YML, r"(?m)^api-version:\s*'[^']*'$", f"api-version: '{minecraft}'", "api-version in paper-plugin.yml")
    elif name == "java":
        if not re.fullmatch(r"\d+", version):
            raise UpdateError(f"'{version}' is not a valid Java version number")
        updater.replace(ROOT_BUILD, r"JavaLanguageVersion\.of\(\d+\)", f"JavaLanguageVersion.of({version})", "toolchain")
        updater.replace(README, r"(?m)^- \*\*Java [^*]+\*\*$", f"- **Java {version}**", "README (Java)")
    elif name in PLUGIN_KEYS:
        key = PLUGIN_KEYS[name]
        updater.replace(LIB_VERSIONS_TOML, rf'(?m)^{re.escape(key)}\s*=\s*"[^"]*"$', f'{key} = "{version}"', f"plugin {name}")
    elif name == "gradle":
        if not re.fullmatch(r"\d+(\.\d+)*", version):
            raise UpdateError(f"'{version}' is not a valid Gradle version")
        if not updater.dry_run:
            try:
                subprocess.run(
                    [str(GRADLEW), "wrapper", f"--gradle-version={version}"],
                    cwd=ROOT,
                    check=True,
                    capture_output=True,
                    text=True,
                )
            except (subprocess.CalledProcessError, FileNotFoundError) as e:
                raise UpdateError(f"gradlew wrapper command failed: {e}")
        updater.changed.append((GRADLE_WRAPPER, "gradle wrapper"))
    else:
        raise UpdateError(f"unknown target '{name}'")


def apply_update(name, version, dry_run, quiet=False):
    version = version.strip() if version else ""
    before = current_version(name)
    if not version:
        return None, None, None
    try:
        update(Updater(dry_run=True), name, version)
        updater = Updater(dry_run=dry_run)
        update(updater, name, version)
    except (UpdateError, OSError) as e:
        return before, None, str(e)
    if not quiet:
        prefix = "[simulation] " if dry_run else "[OK] "
        for path, desc in updater.changed:
            print(f"    {prefix}{path.relative_to(ROOT).as_posix()}  ({desc})")
    return before, version, None


def parse_indices(text):
    indices = set()
    for part in re.split(r"[,\s;]+", text):
        if re.fullmatch(r"\d{1,2}", part):
            i = int(part)
            if 1 <= i <= len(MAIN_CHOICES):
                indices.add(i)
    return sorted(indices)


def interactive(dry_run):
    print()
    print("=" * 46)
    print("  Diskium updater")
    print("=" * 46)
    print("\nCurrent versions:")
    for name, _ in MAIN_CHOICES:
        print(f"  {name:<10} {current_version(name)}")

    while True:
        print()
        print("What do you want to update? (comma-separated numbers, 'a' = all, 0 = exit)")
        for i, (_, label) in enumerate(MAIN_CHOICES, 1):
            print(f"  {i}) {label}")
        print("  0) Exit")
        choice = input("> ").strip().lower()
        if choice in ("0", ""):
            print("\nNothing changed. Exiting.")
            return
        if choice == "a":
            names = ALL_TARGETS
            break
        indices = parse_indices(choice)
        if not indices:
            print("  Invalid input, try again.")
            continue
        names = [MAIN_CHOICES[i - 1][0] for i in indices]
        break

    print()
    for name in names:
        label = dict(MAIN_CHOICES)[name]
        before = current_version(name)
        version = input(f"  {label} - current {before} - new version [enter = skip]: ").strip()
        before, _applied, error = apply_update(name, version, dry_run)
        if error:
            print(f"    [ERROR] {label}: {error}")
        elif version:
            after = current_version(name)
            print(f"    {label}: {before} -> {after}")
        else:
            print(f"    {label}: skipped")


def main():
    parser = argparse.ArgumentParser(description="Diskium updater - update versions in the project")
    parser.add_argument(
        "-u",
        "--update",
        action="append",
        metavar="NAME=VERSION",
        help="target and version (e.g. diskium=1.0-dev.5); can be given multiple times",
    )
    parser.add_argument("--dry-run", action="store_true", help="only show what would change, without writing")
    parser.add_argument("-q", "--quiet", action="store_true", help="suppress per-change output")
    args = parser.parse_args()

    updates = []
    if args.update:
        for item in args.update:
            if "=" not in item:
                parser.error(f"'{item}' must be in the form NAME=VERSION")
            name, version = item.split("=", 1)
            name = name.strip().lower()
            if name not in ALL_TARGETS:
                parser.error(f"unknown target '{name}'. Options: {', '.join(ALL_TARGETS)}")
            updates.append((name, version))

    if updates:
        if not args.quiet:
            print("Diskium updater\n")
        for name, version in updates:
            before, _applied, error = apply_update(name, version, args.dry_run, args.quiet)
            if error:
                print(f"[ERROR] {name}: {error}")
                sys.exit(1)
            if not args.quiet:
                print(f"  {name}: {before} -> {current_version(name)}")
        return

    interactive(args.dry_run)


if __name__ == "__main__":
    main()