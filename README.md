# Diskium

> **WARNING, THIS PLUGIN IS IN ALPHA, IT MAY BE VERY UNSTABLE AND HAVE BUGS!**

Diskium is a Minecraft Paper plugin to manage server files and free up disk space on your Minecraft server.

## Features

**Implemented:**
- Backups & tasks management: list, delete, restore backups and do tasks on server startup (`/diskium backup` or `/diskium task`)
- Logs management: list, delete and search logs (`/diskium logs`)
- Plugin management: list, disable, enable and delete plugins (`/diskium plugin`)
- World management: look for blocks, get info and safely delete parts of the world

## Requirements

- Paper **26.1.2**, **26.2** or **26.3**
- **Java 25**

## Installation

1. Build the plugin or download a release.
2. Put the `Diskium-*.jar` file into your server's `plugins/` folder.
3. Restart the server.

### Building from source

```bash
./gradlew build
```

If you want to build specific version, use:

```bash
./gradlew :paper:<version>:build
```
_e.g. use `./gradlew :paper:26.3:build` for 26.3._

Compiled jars will be in `paper/<version>/build/libs/`.

## License

[MIT](LICENSE)
