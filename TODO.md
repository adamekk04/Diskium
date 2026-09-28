# TODO

## Release 1.0-dev.8
- [x] Add JUnit tests
- [x] Add 26.3 support

## Release 1.0-dev.9
Fix critical bugs

## Release 1.0
Goal: stable version

---

## 🐛 Bugs

---

## 💡 Ideas
- Add filtering options for logs commands
- Add /diskium delete (with subcommands like logs, plugins, world, etc.)
- Remove and modify world only in bootstrap

## Changelog
### Additions
- Add support for Paper 26.3
- Add JUnit tests

### Changes
- Little better error handling in MCA Parser
- Remove RegionManagement (all things are migrated to Region.java)

### Fixes
- Remove task/backup after completing it
- Use right index while reading .mca header