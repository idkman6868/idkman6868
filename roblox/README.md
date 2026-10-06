# Roblox (Rojo) project

## Setup
1. Install Rojo: `aftman install` (uses `aftman.toml`), or download from https://github.com/rojo-rbx/rojo/releases
2. Install the Rojo plugin in Roblox Studio (Plugins → Manage Plugins, or `rojo plugin install`).

## Use
```
cd roblox
rojo serve
```
In Studio, open the Rojo plugin and click **Connect**. Edits in `src/` sync live.

| Folder | Lands in |
|---|---|
| `src/shared` | ReplicatedStorage.Shared |
| `src/server` | ServerScriptService.Server |
| `src/client` | StarterPlayer.StarterPlayerScripts.Client |

`*.server.luau` = Script, `*.client.luau` = LocalScript, plain `.luau` = ModuleScript.

Build a place file: `rojo build -o game.rbxlx`
