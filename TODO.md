# TODO

## Release 1.0.0-dev.3
Goal: Fully migrate to Paper's command system

## Release 1.0
Goal: stable version

---

## Paper

### Commands
#### 🌍 World Commands

### `/diskium world`

---

## GetBlock

- [ ] Implement `/diskium world getBlock`
- [ ] Support world as separate argument
- [ ] Support `allWorlds`
- [ ] Get block by coordinates

### Sources

#### `thisWorld`

- [ ] Accept coordinates
- [ ] Get block from current world
- [ ] Output block type
- [ ] Output block properties


#### `naturally`

- [ ] Accept coordinates
- [ ] Get block from natural world
- [ ] Output block type
- [ ] Output block properties


---

# Delete

- [ ] Implement `/diskium world delete`
- [ ] Use Task Queue system


## Delete inside border

### `in`

- [ ] Accept range
- [ ] Add build checking option

### `out`

- [ ] Accept range
- [ ] Add build checking option


---

## Build checking

### `checkForBuilds`

- [ ] Compare against separate world
- [ ] Detect player-made builds
- [ ] Prevent deleting protected builds


### `dontCheckForBuilds`

- [ ] Skip build checking
- [ ] Add warning before execution
- [ ] Allow deleting player builds


---

## Delete targets

### `wholeWorld`

- [ ] Delete entire world
- [ ] Support `checkForBuilds`
- [ ] Support `dontCheckForBuilds`


### `region`

- [ ] Accept region coordinates
- [ ] Validate region boundaries
- [ ] Queue deletion task


### `chunk`

- [ ] Accept chunk coordinates
- [ ] Queue chunk deletion task


---

# World Info

- [ ] Implement `/diskium world info`
- [ ] Display world information:
    - [ ] World name
    - [ ] World size
    - [ ] Seed
    - [ ] Chunk count
    - [ ] Active tasks


---


# 📋 Task Commands

## `/diskium task`

---

## List

- [ ] Implement `/diskium task list`

### Categories

#### `logs`

- [ ] List all log tasks


#### `plugins`

- [ ] List all plugin tasks


#### `world`

- [ ] List all world tasks


---

## Remove

- [ ] Implement `/diskium task remove`
- [ ] Accept task file name
- [ ] Remove task from `tasks.yml`
- [ ] Confirm deletion


---

## Info

- [ ] Implement `/diskium task info`
- [ ] Accept task file name
- [ ] Display task information


---


# 💾 Backup Commands

## `/diskium backup`

---

## List

- [ ] Implement `/diskium backup list`

### Categories

#### `logs`

- [ ] List all log backups


#### `plugins`

- [ ] List all plugin backups


#### `world`

- [ ] List all world backups


---

## Remove

- [ ] Implement `/diskium backup remove`
- [ ] Accept backup file name
- [ ] Remove backup


---

## Restore

- [ ] Implement `/diskium backup restore`
- [ ] Accept backup file name
- [ ] Load backup data
- [ ] Create restore task
- [ ] Execute restore through Task Queue

### Configuration
*Nothing yet*

---

## Refactoring
- [ ] Use Paper's command system (not Bukkit's one)

---

## Bugs
*Nothing yet*

---

## 💡 Ideas
*Nothing yet*