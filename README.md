# Diskium

> **WARNING, THIS PLUGIN IS STILL IN DEVELOPMENT! IT'S NOT FINISHED YET AND USE IT AT YOUR OWN RISK!**

Diskium is a Minecraft Paper plugin to manage server files and free up disk space on your Minecraft server.

## Features

**Implemented:**
- Config management (`/diskium config`)
- Log management: list, delete and search logs (`/diskium logs`)
- Plugin management: list, disable, enable and delete plugins (`/diskium plugin`)
- Block lookup in a world (`/diskium world`)

**Planned:** backups, task queue, world deletion. See [TODO.md](TODO.md) for the full roadmap.

## Requirements

- Paper **26.2** or **26.3**
- **Java 25**

## Installation

1. Build the plugin or download a release.
2. Put the `Diskium-*.jar` file into your server's `plugins/` folder.
3. Restart the server.

### Building from source

```bash
./gradlew build
```

The two compiled jars will be in `paper/26.2/build/libs/` and
`paper/26.3/build/libs/`.

## License

[MIT](LICENSE)
