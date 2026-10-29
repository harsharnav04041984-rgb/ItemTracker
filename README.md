# ItemTrackerPro

ItemTrackerPro is a Paper Minecraft server plugin for tracking item movement and investigating suspicious item activity.

## Requirements

- Minecraft server running Paper 1.21.x
- Java 21 or newer
- Maven 3.9 or newer to build from source

## Build from source

1. Install a JDK 21 and Maven.
2. Open a terminal in the folder containing `pom.xml`.
3. Run:

   ```bash
   mvn clean package
   ```

4. The plugin JAR will be created in `target/`, named similar to `ItemTrackerPro-1.0.0.jar`.
5. Copy the JAR into your Paper server's `plugins/` folder and restart the server.

## Use

- In-game command: `/itemtracker`
- Aliases: `/itp`, `/itemtrackerpro`
- Permission: `itemtrackerpro.admin` (defaults to server operators)

Configuration is generated from `src/main/resources/config.yml` when the plugin first runs. Back up your server and review the configuration before using this plugin on a production server.

## GitHub

This repository contains the plugin source code, Maven build file, and resources. The compiled JAR and Maven `target/` output are intentionally excluded; build them with Maven or attach the JAR to a GitHub Release.

## Project layout

```text
ItemTrackerPro/
├── .github/workflows/maven.yml
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        ├── java/com/itemtrackerpro/
        └── resources/
            ├── config.yml
            └── plugin.yml
```

## Compatibility note

The Maven build currently targets Java 21 and declares the Paper API dependency in `pom.xml`. Check the server version and test on a staging server before deploying.
