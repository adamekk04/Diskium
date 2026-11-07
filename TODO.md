# TODO

## Release 1.0-alpha.4
- Fix all high priority bugs

## Release 1.0-alpha.5
- Fix a part of medium priority bugs

## Release 1.0-beta.1
- Fix all medium priority bugs

---

## 🐛 Bugs

> Bugs found by Codex

### BUG-008: Radius selection has incorrect outside and negative-center geometry

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/WorldManagement.java:298, getRegionFiles(World,int,boolean,Location); common/src/main/java/org/diskium/utils/WorldUtils.java:37, blockToRegion
- **Description:** Outside selection requires both axes to be beyond the radius, missing the side strips of the square. Region-center conversion truncates negative block coordinates toward zero. Later safety filtering also measures from the origin instead of the supplied center.
- **Reproduction:** With fixture r.3.0.mca, call the range overload with radius 512, in=false and center 0,0. Separately evaluate blockToRegion(-1). The public range command currently also has BUG-010.
- **Expected behavior:** Select regions outside either edge and calculate negative region coordinates and shifted centers consistently.
- **Actual behavior:** r.3.0.mca is excluded although clearly outside; block -1 maps to region 0 instead of -1.
- **Impact:** Cleanup selection omits intended regions and can disagree with shifted border locations.
- **Evidence:** AuditProbe: outsideSquareRegion3_0Count=0. Source uses && at line 317, radius/512 at WorldUtils:38 and origin-relative coordinates at WorldUtils:69.
- **Suggested fix:** Define exact bounds in block coordinates, use floor division and region extents, and apply the same center throughout selection and safety checks.

### BUG-009: Build-checking deletion leaks generated comparison worlds

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/WorldManagement.java:120, :147 and :172, del and delSector
- **Description:** All three build-checking paths create a world but never unload it or arrange deletion, including early return paths.
- **Reproduction:** On a disposable server run /diskium world delete minecraft:overworld --checker=whole_world --checkForBuilds=true, then world info and inspect generated world directories.
- **Expected behavior:** Comparison worlds are cleaned up in a guaranteed completion/failure path.
- **Actual behavior:** A random-suffixed comparison world remains loaded and is saved on shutdown.
- **Impact:** Repeated cleanup consumes disk, memory and world-management overhead instead of freeing them.
- **Evidence:** runtime-stage2.log lists worldptr9czb00h after the command and logs its shutdown save; no delWorld call exists in these paths.
- **Suggested fix:** Use try/finally or structured asynchronous cleanup, verify unload success, and delete only identified temporary-world data.

### BUG-011: Explicit naturally boolean values cannot be parsed

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/WorldCommand.java:109 and :132, getBlockSeparator; common/src/main/java/org/diskium/utils/Flags.java:130, parseValue
- **Description:** Raw new FlagObj(...) creates Object[] options, so getValueClass returns Object.class. parseValue handles String, Integer and Boolean only.
- **Reproduction:** Execute /diskium world getBlock minecraft:overworld 0 64 0 --naturally=true and repeat with false.
- **Expected behavior:** Both offered boolean values are accepted.
- **Actual behavior:** IllegalArgumentException is thrown for the boolean value; suggestions nevertheless advertise --naturally=.
- **Impact:** The explicit natural/current lookup option is unusable.
- **Evidence:** runtime-stage1.log records Invalid value true for flag --naturally; the build also emits unchecked warnings for this class.
- **Suggested fix:** Use a typed FlagObj<Boolean> and validate options independently of suggestion calls.

### BUG-012: Natural block lookup returns the edited world and default lookup generates a clone

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/WorldCommand.java:94 and :141, getBlockSeparator; paper/common/src/main/java/org/diskium/management/WorldManagement.java:29, getBlock
- **Description:** The flag named naturally is passed as the existing parameter. True reads the existing world; the flag-free command passes false and generates a temporary world.
- **Reproduction:** Place a diamond block at 0,64,0 in the disposable flat world. Run getBlock ... --naturally (bare flag avoids BUG-011), then run the same command without flags.
- **Expected behavior:** Natural lookup uses the world template; default/current lookup observes the placed block.
- **Actual behavior:** --naturally returns DIAMOND_BLOCK. The default path creates a new world. The flagged path also announces generation even when no world is generated.
- **Impact:** Incorrect inspection results and unnecessary world generation.
- **Evidence:** runtime-stage3.log: world: DIAMOND_BLOCK after --naturally. runtime-stage2.log shows clone generation for the flag-free command.
- **Suggested fix:** Align flag and method semantics and report generation only when the natural-world path is actually used.

### BUG-016: Every log search leaks a file handle while trying to decompress latest.log

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/LogsManagement.java:67, LogsManagement.search
- **Description:** getLogs includes the plain latest.log, but the archive loop wraps every input in GZIPInputStream. When that constructor or a later read throws, neither stream is closed.
- **Reproduction:** Call search on ordinary logs 12 times in one disposable server process and count /proc/self/fd before/after, as AuditProbe does.
- **Expected behavior:** Read latest.log once as plain text and close every stream on all exits.
- **Actual behavior:** Each call logs Cannot unzip file latest.log and leaks an open descriptor until eventual cleaner activity.
- **Impact:** Repeated searches and corrupt archives can exhaust file descriptors and spam errors.
- **Evidence:** runtime-stage2.log: openFDdelta=12 after 12 searches, with 12 latest.log unzip errors.
- **Suggested fix:** Separate plain and compressed paths and use nested try-with-resources for both streams.

### BUG-017: Missing latest.log propagates null into list, search and deletion

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/LogsManagement.java:35 and :102, getLogs/getLatestLog; paper/common/src/main/java/org/diskium/commands/LogsCommand.java:37, list
- **Description:** getLatestLog returns null when absent, getLogs appends it, and consumers dereference it. The latest-log path also uses worldContainer whereas archive lookup uses the inferred server root.
- **Reproduction:** Code-confirmed safe reproduction: use a disposable server with an empty logs directory or a custom world-container without logs/latest.log; call logs list/search/delete.
- **Expected behavior:** Missing logs are omitted or reported without exceptions; all log paths use the same configured server log directory.
- **Actual behavior:** The list contains null; log.getName(), safeDel(null,...) or new FileInputStream(null) throws.
- **Impact:** All log operations can fail during rotation, after active-log removal or with custom world containers.
- **Evidence:** getLogs unconditionally adds getLatestLog at line 35; getLatestLog returns null at line 108. NullPointerException is not caught by search.
- **Suggested fix:** Use a non-null optional path, one consistent log root, and existence/type checks immediately before access.

### BUG-018: Unexpected archive filenames crash date-filtered log commands

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/LogsManagement.java:154, supposedToAdd
- **Description:** Every name ending .log.gz is accepted but its first ten characters are parsed as an ISO date without validation or error handling.
- **Reproduction:** In disposable logs create a.log.gz or not-a-date-1.log.gz, then run logs list --until=2026-12-31.
- **Expected behavior:** Skip/report nonstandard files without aborting the whole command.
- **Actual behavior:** A short name causes StringIndexOutOfBoundsException; an invalid prefix causes DateTimeParseException.
- **Impact:** One renamed or third-party archive breaks listing and filtered deletion.
- **Evidence:** Code-confirmed: listFiles suffix-only filter at line 28 feeds substring(0,10) and LocalDate.parse at lines 156-161 with no catch.
- **Suggested fix:** Match supported log filename patterns and handle each invalid archive independently.

### BUG-020: Task listing crashes on ordinary deletion tasks

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/TaskCommand.java:150, TaskCommand.lister
- **Description:** Deletion tasks deliberately have null replacementFile, but lister dereferences it while computing column widths and rendering rows.
- **Reproduction:** Queue one disposable deletion and run /diskium task list.
- **Expected behavior:** Show a deletion row with no replacement path.
- **Actual behavior:** NullPointerException prevents the table from being shown.
- **Impact:** Administrators cannot review the normal queued-deletion workload.
- **Evidence:** runtime-stage2.log at 11:43:39: Cannot invoke java.io.File.toString() because TaskObj.getReplacementFile() is null, TaskCommand.java:150.
- **Suggested fix:** Render an explicit empty/not-applicable destination for delete tasks and compute widths from displayed strings.

### BUG-021: Task table crashes at the third move task and misaligns columns

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/TaskCommand.java:171, TaskCommand.lister
- **Description:** Padding subtracts the row number, not the rendered string length. With three tasks longestID is two, making the third repeat count negative. The Delete column value is also omitted.
- **Reproduction:** Seed three ordinary move tasks in a disposable tasks.txt and run task list.
- **Expected behavior:** Render every row and the Delete boolean with valid padding.
- **Actual behavior:** Two malformed rows print, then String.repeat(-1) throws.
- **Impact:** Even queues without deletion tasks cannot reliably be reviewed.
- **Evidence:** runtime-stage1.log: IllegalArgumentException: count is negative: -1 at TaskCommand.java:172.
- **Suggested fix:** Build row cells first, use their string lengths, clamp padding, and include every header column in the row.

### BUG-022: Task and backup type filters crash on their advertised flags

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/TaskCommand.java:76, entry; paper/common/src/main/java/org/diskium/commands/BackupCommand.java:78, entry
- **Description:** Flag keys are lowercase while enum constants are uppercase. valueOf is case-sensitive; TaskCommand also ignores the boolean flag value.
- **Reproduction:** Run task list --logs and backup list --logs, including uppercase input variants.
- **Expected behavior:** Filter by the selected category without exceptions and honor false values.
- **Actual behavior:** IllegalArgumentException: No enum constant ...Types.logs. Canonical lowercase parser keys make uppercase spelling ineffective.
- **Impact:** All advertised category filters are broken.
- **Evidence:** runtime-stage1.log records both enum exceptions at TaskCommand:77 and BackupCommand:80.
- **Suggested fix:** Map flag names explicitly to enum values, honor booleans, and preserve original record IDs in filtered views.

### BUG-023: Malformed or incomplete flags escape as unhandled command and completion exceptions

- **Severity:** Medium
- **Location:** common/src/main/java/org/diskium/utils/Flags.java:84, suggest; paper/common/src/main/java/org/diskium/commands/WorldCommand.java:238, deleteLogic
- **Description:** Unknown name=value flags call Optional.get on an empty value. World deletion dereferences required flags without checking presence; invalid typed values propagate IllegalArgumentException. The suggestion function is also used as the execution parser.
- **Reproduction:** Run logs list --unknown=x; request completion for logs list --bogus=; run world delete minecraft:overworld --checker=chunk without coordinates/build flag; try --x=abc or an overflowing integer.
- **Expected behavior:** Return actionable syntax errors, safe defaults where specified, and an empty completion result for unknown names.
- **Actual behavior:** NoSuchElementException, NullPointerException or uncaught IllegalArgumentException occurs.
- **Impact:** Normal malformed input breaks execution/completion and floods error logs; unknown bare flags can silently discard constraints.
- **Evidence:** runtime-stage1.log contains unknown-flag and numeric exceptions; runtime-stage2.log records NoSuchElementException during actual Brigadier suggestions.
- **Suggested fix:** Create a stateless validated parser with required-flag schemas and translate validation failures into Brigadier syntax errors.

### BUG-024: Configuration commands are absent on the first startup

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/ConfigCommand.java:45, configs; paper/common/src/main/java/org/diskium/management/ConfigManagement.java:19, getConfig; paper/common/src/main/java/org/diskium/Diskium.java:15, onEnable
- **Description:** Command registration reads config.yml before onEnable saves the default file, so an initial installation builds an empty config subtree.
- **Reproduction:** Start the plugin on a clean disposable Paper server without its data directory, then run diskium config backups. Restart and repeat.
- **Expected behavior:** Default configuration keys are available on the first boot.
- **Actual behavior:** First boot rejects backups as an argument; after restart it reports backups = true.
- **Impact:** A new installation cannot use its configuration commands until restarted.
- **Evidence:** runtime-stage1.log: Incorrect argument ... config backups. runtime-stage3.log: backups = true. The clean-start command event precedes onEnable.
- **Suggested fix:** Make defaults available before command tree construction or build a stable config schema independently of the on-disk file.

### BUG-026: Bootstrap configuration is ignored

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/DiskiumBootstrap.java:15, bootstrap; paper/common/src/main/resources/config.yml:1
- **Description:** The exposed bootstrap setting is never read. Its documented early-exit behavior cannot affect queued startup actions.
- **Reproduction:** Set bootstrap:false in a disposable config, queue deletion of a marker, and restart. Also inspect the true setting path; neither is consulted.
- **Expected behavior:** Define and honor the setting that controls skipping/exiting queued bootstrap work.
- **Actual behavior:** The queued marker is deleted and all tasks are processed regardless of the setting.
- **Impact:** Operators cannot use the advertised setting to suspend startup work.
- **Evidence:** runtime-stage2.log shows Found 1 task and the marker is absent; runtime-stage3.log confirms bootstrapFalseMarkerExists=false. There is no configuration read in bootstrap.
- **Suggested fix:** Load and validate bootstrap policy before processing the queue; document an unambiguous meaning for true and false.

### BUG-028: Plugin info mutates author metadata

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/PluginManagement.java:118, PluginManagement.info
- **Description:** The authors list returned from PluginMeta is modified with removeFirst to format the output.
- **Reproduction:** Run plugins info Diskium twice on the disposable Paper server.
- **Expected behavior:** Repeated read-only information queries leave metadata unchanged.
- **Actual behavior:** The first call shows the author; the second shows Authors: None. Immutable metadata implementations would instead throw.
- **Impact:** Corrupts plugin metadata used by other commands/plugins and can cause runtime incompatibilities.
- **Evidence:** runtime-stage1.log records Author: adamekk04 followed by Authors: None in two consecutive queries.
- **Suggested fix:** Iterate or join the list without modification; copy before any local mutation.

### BUG-029: Plugin task and backup membership always compare unequal File instances

- **Severity:** Low
- **Location:** paper/common/src/main/java/org/diskium/management/PluginManagement.java:144 and :166, info
- **Description:** Registry File objects are compared to newly constructed File objects with == rather than path equality.
- **Reproduction:** Code-confirmed: register a task/backup for a disposable plugin JAR and query that plugin info.
- **Expected behavior:** Report membership for matching canonical/normalized paths.
- **Actual behavior:** Both membership flags stay false even when matching records exist.
- **Impact:** Misleads operators checking pending changes and recovery availability.
- **Evidence:** TasksUtils reconstructs File objects on read; info creates another File and filters task.getFile() == file / backup.getFile() == file.
- **Suggested fix:** Compare normalized absolute Path values and define behavior for aliases and multiple matching records.

### BUG-030: Deleting an unknown plugin throws instead of rejecting the name

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/DeleteCommand.java:165, pluginDeleter; paper/common/src/main/java/org/diskium/management/PluginManagement.java:83, del
- **Description:** Bukkit.getPlugin may return null; the deletion path never checks it before accessing the plugin class or data folder.
- **Reproduction:** Run plugins delete nonexistent --jarFile, then the --folder variant on an isolated server.
- **Expected behavior:** Report that the plugin does not exist and do nothing.
- **Actual behavior:** NullPointerException escapes the command.
- **Impact:** Typos and casing differences produce internal errors instead of safe validation.
- **Evidence:** runtime-stage1.log: Cannot invoke org.bukkit.plugin.Plugin.getClass() because pl is null at PluginManagement.java:83.
- **Suggested fix:** Validate the lookup once before any flags or filesystem operations and report a command syntax error.

### BUG-031: Deleting a backup only removes its registry entry and leaves the data behind

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/DeleteCommand.java:73, backups; common/src/main/java/org/diskium/utils/TasksUtils.java:251, remove(BackupObj)
- **Description:** The backup delete command never deletes backup.getItself(). It only rewrites backups.txt.
- **Reproduction:** Create a disposable backup file plus its two-line registry entry, execute backup delete 1, then inspect the physical file.
- **Expected behavior:** Delete the selected backup data and update the registry only after success.
- **Actual behavior:** The command reports Deleted backup 1, but the file remains and becomes unlisted.
- **Impact:** Disk cleanup does not reclaim space and orphaned backups accumulate.
- **Evidence:** runtime-stage1.log records deletion; fixture-backup remained with SAVED content. The only called method is TasksUtils.remove.
- **Suggested fix:** Delete the verified backup path safely, retain the record on failure, and report the actual outcome.

### BUG-032: Successful queued restoration leaves stale backup records

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/commands/BackupCommand.java:110, restore; common/src/main/java/org/diskium/utils/TasksUtils.java:186, complete(TaskObj[])
- **Description:** The command schedules an ordinary move task. Bootstrap removes the task but never removes or updates its backup registry entry; the source backup was consumed by the move.
- **Reproduction:** Register a disposable file backup, execute backup restore 1 and restart, then list backups and repeat restore.
- **Expected behavior:** Successful restoration leaves consistent registry state and clearly defined backup retention.
- **Actual behavior:** The restored file exists, the backup file is gone, but backup list still offers it. Repeated restores create failing jobs.
- **Impact:** Backup inventory and restore operations become misleading and unusable.
- **Evidence:** common-audit.log confirms the stale entry; runtime-stage3.log lists the backup after bootstrap restored fixture-original and consumed fixture-backup.
- **Suggested fix:** Persist restoration as a distinct operation with a backup ID, and atomically update task/backup metadata after verification.

### BUG-040: Restoration cannot recover files whose parent directories were removed

- **Severity:** Medium
- **Location:** common/src/main/java/org/diskium/utils/FileUtils.java:66, move; common/src/main/java/org/diskium/utils/TasksUtils.java:186, complete
- **Description:** Files.move is called without creating or validating the original parent tree.
- **Reproduction:** Create a disposable backup and restore it to missing-parent/dest through a queued restore.
- **Expected behavior:** Recreate validated destination parents when restoring deleted files, or clearly reject the restore before scheduling.
- **Actual behavior:** NoSuchFileException is reduced to Could not move file because it does not exist; the source exists but the task remains indefinitely failing.
- **Impact:** Restoring a file inside a removed plugin/world folder does not work.
- **Evidence:** common-audit.log: missing restore parent fails and keeps task; source file remains intact.
- **Suggested fix:** Safely create and validate allowed parent directories and distinguish missing source from missing destination ancestry.

### BUG-041: Invalid date patterns pass validation and later throw unchecked exceptions

- **Severity:** Medium
- **Location:** common/src/main/java/org/diskium/utils/DateUtils.java:65, isValidString and :75, parse; paper/common/src/main/java/org/diskium/commands/ConfigCommand.java:54, configs
- **Description:** Validation only counts characters. Formatter construction is outside the try block and null/malformed configuration is not checked at enable.
- **Reproduction:** Set date-format to yyyy-mm-dd plus an unmatched quote in a disposable config, or call isValidString("yyyy-mm-dd'") and then parse using that pattern.
- **Expected behavior:** Reject malformed patterns before persisting or activating them.
- **Actual behavior:** Character counts pass, then DateTimeFormatter.ofPattern throws IllegalArgumentException during log command execution.
- **Impact:** A valid-looking accepted setting breaks all date-filtered log operations until repaired.
- **Evidence:** Audit.java confirms invalid date pattern accepted then crashes parser.
- **Suggested fix:** Compile and round-trip-test the pattern during validation, handle missing config values, and catch formatter construction failures.

### BUG-042: Backup restore completion suggests invalid zero-based IDs

- **Severity:** Low
- **Location:** paper/common/src/main/java/org/diskium/commands/BackupCommand.java:96, restore suggestions
- **Description:** The argument accepts one-based IDs but its completion loop offers 0 through length-1.
- **Reproduction:** Register one disposable backup and request completion for diskium backup restore .
- **Expected behavior:** Suggest ID 1.
- **Actual behavior:** Only ID 0 is suggested and execution rejects it.
- **Impact:** Tab completion steers users to an invalid restore command and omits the last backup.
- **Evidence:** runtime-stage2.log actual Brigadier result: IntegerSuggestion value=0 for the single-backup fixture.
- **Suggested fix:** Generate suggestions from the same stable IDs used by list and execution.

### BUG-043: Plugin enable/disable completion inserts version text into a word argument

- **Severity:** Low
- **Location:** paper/common/src/main/java/org/diskium/commands/PluginsCommand.java:38 and :171, disable and pluginEnabler suggestions
- **Description:** The completion provider uses plugin.toString rather than getName while the argument accepts one word.
- **Reproduction:** Request completion for diskium plugins enable or disable on the disposable server.
- **Expected behavior:** Suggest the exact accepted plugin name.
- **Actual behavior:** Suggestions include Diskium v1.0-alpha.1 and AuditProbe v1; the version becomes unexpected trailing input.
- **Impact:** Tab-completed commands fail or select an invalid argument form.
- **Evidence:** runtime-stage2.log records both enable and disable suggestion lists with embedded versions.
- **Suggested fix:** Suggest plugin.getName(), filter by typed prefix and verify offered strings parse successfully.

### BUG-044: Malformed region filenames crash region scanning

- **Severity:** Medium
- **Location:** common/src/main/java/org/diskium/mca/Sector.java:34, constructor; paper/common/src/main/java/org/diskium/management/WorldManagement.java:309, getRegionFiles
- **Description:** Coordinate parsing assumes two numeric components. The constructor catches NumberFormatException but not missing components; the range scanner catches neither.
- **Reproduction:** Create a disposable valid-sized 16 KiB file named r.1.mca and invoke Parser.parse; separately place a nonnumeric .mca name in a disposable range directory.
- **Expected behavior:** Reject/report invalid region names without aborting scans or assigning default coordinates.
- **Actual behavior:** r.1.mca throws ArrayIndexOutOfBoundsException; nonnumeric range names throw NumberFormatException.
- **Impact:** A stray/renamed region file can break world management; fallback zero coordinates can also misrepresent damaged input.
- **Evidence:** common-audit.log confirms valid-sized malformed region filename crashes parser. Sector accesses regionCoords[1] unconditionally.
- **Suggested fix:** Validate the complete r.<signed-int>.<signed-int>.mca grammar, bounds and header before constructing a region.

### BUG-045: Creating an unrelated missing registry skips already queued startup tasks

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/DiskiumBootstrap.java:51, bootstrap; common/src/main/java/org/diskium/utils/TasksUtils.java:22, mkFiles
- **Description:** mkFiles returns true if either registry was created, and bootstrap treats that as a reason to return even if tasks.txt already contains work.
- **Reproduction:** Code-confirmed: in a disposable plugin directory keep a populated tasks.txt but remove only backups.txt; start the server.
- **Expected behavior:** Initialize the missing backup registry and still process existing tasks.
- **Actual behavior:** mkFiles creates backups.txt, returns true, and bootstrap exits without reading tasks.txt.
- **Impact:** Scheduled deletes/restores are silently delayed by an extra restart.
- **Evidence:** TasksUtils.mkFiles sets buffer=true for backups.createNewFile at lines 36-38; bootstrap immediately returns on that value.
- **Suggested fix:** Separate initialization success from whether task work exists; only stop bootstrap on a real initialization failure.

### BUG-046: Failed log deletions are reported as successful completion

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/LogsManagement.java:52, delete; paper/common/src/main/java/org/diskium/commands/DeleteCommand.java:101, logsExec
- **Description:** The deletion loop ignores every safeDel result and always returns true. The commands announce deletion before attempting it and do not summarize failures.
- **Reproduction:** In a disposable fixture make the task registry unwritable with immediate deletion disabled, then issue a log deletion; alternatively use a protected log directory with immediate deletion enabled.
- **Expected behavior:** Report failed files/queue writes and avoid claiming successful scheduling.
- **Actual behavior:** Errors are logged but the command completes with success and no file-specific failure feedback.
- **Impact:** Operators believe logs were deleted or queued when no work was saved.
- **Evidence:** Code-confirmed: LogsManagement.delete lines 55-59 discard boolean results and return true; both logsExec variants ignore its result.
- **Suggested fix:** Aggregate per-file outcomes, distinguish queued/deleted/failed states and send an accurate command result.

### BUG-047: Plugin deletion overwrites an earlier failure with later success

- **Severity:** Medium
- **Location:** paper/common/src/main/java/org/diskium/management/PluginManagement.java:78, del
- **Description:** When both JAR and folder are selected, buffer is reassigned for the folder result even if deleting or queuing the JAR failed.
- **Reproduction:** Code-confirmed safe reproduction: use a disposable loaded test plugin with an undeletable JAR path and a deletable data folder, enable immediate plugin deletion, then request both flags.
- **Expected behavior:** Any failed requested operation produces a failure/partial-success result.
- **Actual behavior:** The successful folder deletion makes del return true despite the failed JAR deletion.
- **Impact:** Misleading success and partial plugin removal, possibly leaving the plugin active without its data.
- **Evidence:** Assignments at lines 84 and 91 replace rather than accumulate status; DeleteCommand prints Deletion was successful on the final boolean.
- **Suggested fix:** Track JAR and data-directory results independently and clearly report partial outcomes.

### BUG-048: World info labels the border diameter as its radius

- **Severity:** Low
- **Location:** paper/common/src/main/java/org/diskium/management/WorldManagement.java:93, info
- **Description:** The displayed Border radius value is getWorldBorder().getSize(), which is the full side length/diameter, without dividing by two.
- **Reproduction:** Set a disposable world border to side length 1000, then execute world info.
- **Expected behavior:** Show radius 500 or label 1000 as border size/diameter.
- **Actual behavior:** The value 1000 is labeled radius.
- **Impact:** Misleads administrators planning coordinate-based cleanup.
- **Evidence:** Code-confirmed at info lines 93-94; WorldCommand.deleteLogic itself divides the same API value by two at line 245.
- **Suggested fix:** Correct the label or divide by two, preserving fractional values if relevant.

### Potential Bugs

These are unverified risk hypotheses or incomplete end-to-end investigations, **not included in the confirmed counts**:

- **POT-001 — Symlink/path containment and TOCTOU:** FileUtils and registry replay accept unnormalized paths and follow symlinked ancestors; the ancestor behavior was reproduced in isolated fixtures. Direct child symlinks were not recursively followed. Determine intended support for worlds/plugins outside the root and establish whether a less-privileged actor can alter ancestors/registries between queueing and execution. No general remote path-traversal or Minecraft permission bypass was demonstrated. Newline record injection is separately confirmed as BUG-036.
- **POT-002 — Concurrent registry updates and crash durability:** add/remove use independent writers and whole-file rewrites without locking or atomic replacement. Simultaneous mutations or disk-full/power-loss may lose records. Normal console commands are main-thread serialized; no production-reachable concurrent writer race or full-disk failure was forced. The read-only-registry replay and truncated-append cases are separately confirmed.
- **POT-003 — Region companion data:** terrain-only deletion does not touch entity, POI or external oversized-chunk files. Verify the intended reset semantics and reload behavior with populated entity/POI/external-chunk fixtures on every target Paper version before claiming specific ghost-entity or corruption outcomes.
- **POT-004 — Main-thread stalls and unbounded memory:** log search synchronously decompresses entire archives with readAllBytes and commands perform synchronous directory scans/world generation/chunk comparisons. Repeated region construction also re-reads the same file and does not deduplicate Region objects. The synchronous paths are evident and ordinary searches ran on Server thread, but watchdog shutdown/OOM and large-world resource limits were not stress-tested.
- **POT-005 — Asynchronous world-lookup cleanup:** getBlock has no exceptional completion cleanup or disable-time cancellation, and delWorld ignores unload failure before queueing deletion. Verify exceptional getChunkAtAsync, plugin disable during lookup, players entering temporary worlds and custom generators before quantifying the lifecycle impact.
- **POT-006 — Corrupt MCA bounds and stale queued file identity:** size-only validation accepts out-of-range/overlapping sector pointers; queued build decisions are not revalidated after players change a world or a plugin JAR is replaced. Test these cases beyond the deterministic MCA corruption and registry replay already confirmed.
- **POT-007 — Destructive self-management and boot ordering:** deleting Diskium's own data directory can remove its task/backup registry during replay; deleting another plugin JAR during bootstrap may be too late to prevent an already-discovered provider from loading. Verify startup ordering, loaded handles and platform-specific deletion behavior with dedicated dummy plugins.
- **POT-008 — Filtered list IDs after enum-filter repair:** both task and backup listers renumber filtered arrays, while selection commands index the full registry. Existing filter crashes currently block the ordinary route. Preserve stable IDs when repairing the confirmed enum bug, then verify the latent wrong-item behavior end-to-end.
- **POT-009 — Empty keyword helper loop:** LogsManagement.searchLog never advances for an empty keyword. Standard greedyString command parsing rejected missing input by construction; no reachable real command yielding an empty Java string was established. Test any future API caller/quoted-string parser before exposing this helper.
- **POT-010 — Cross-platform and alternate deployment roots:** inferred server roots, hardcoded plugins/logs folders, lexical getType prefixes and dynamic dependency versions merit tests with alternate --plugins/--universe locations, Windows locked JARs, cross-device directories and pinned dependencies. No target-version incompatibility was inferred solely from these risks.


---

## 💡 Ideas

---
