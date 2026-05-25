# HUD Compass Mod — Minecraft 1.21.10 (Fabric)

A lightweight client-side HUD mod that displays a small compass in the top-left corner of your screen.

Follow me in github- https://github.com/PrithwishDutta

Download Mod here for minecraft- 

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/compassfindings)

## Features

| Feature | Detail |
|---------|--------|
| HUD Compass | Always-visible mini compass in the top-left corner |
| **Spawn Mode** | Needle points toward world spawn (0, 0) |
| **Player Mode** | Shows one compass per nearby/tracked player |
| **Toggle hotkey** | `Ctrl + Alt + Z` — switches between modes |
| Player tracking | `/compasstrack add <name>` to pin specific players |

---

## Controls

| Action | Keybind |
|--------|---------|
| Toggle Spawn ↔ Player mode | `Ctrl + Alt + Z` |

> The mode name is briefly shown in the action bar when you toggle.

---

## Commands (client-side)

```
/compasstrack add <player>     — Add a player to the tracked list
/compasstrack remove <player>  — Remove a player from the tracked list
/compasstrack list             — Show currently tracked players
/compasstrack clear            — Clear all tracked players (auto-detect mode)
```

When the tracked list is **empty**, Player Mode auto-detects all players visible in the current chunk/world.

---


