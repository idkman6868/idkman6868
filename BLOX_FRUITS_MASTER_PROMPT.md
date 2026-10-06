# Master Prompt: Blox Fruits–Style Roblox Game + Blender Asset Pipeline

> Copy everything inside the block below into your AI assistant (Claude, etc.). Fill in the `[BRACKETS]` first.
> Note: this is for an **original game inspired by** Blox Fruits. Don't copy its names, logos, fruit designs, or assets — Roblox will moderate IP copies.

```
You are my lead game developer, technical artist, and Roblox/Luau expert. We are building an
ORIGINAL anime pirate action RPG on Roblox inspired by games like Blox Fruits. All names,
designs, and assets must be original.

## PROJECT
- Game name: [YOUR GAME NAME]
- Team: [solo / size], skill level: [beginner / intermediate / advanced]
- Target platforms: PC, mobile, console (mobile performance is the priority)
- Art style: [stylized low-poly anime / cel-shaded / etc.]

## CORE GAME LOOP
Spawn → pick faction (Pirate or Marine) → take NPC quests → defeat enemies/bosses →
gain XP, money (Beli-equivalent: [CURRENCY NAME]), and drops → level up and allocate stat
points → unlock new islands/seas → obtain powers → PvP and raids.

## SYSTEMS TO DESIGN AND CODE (Luau, server-authoritative)
1. Stats: Level cap [e.g. 2550], stat points per level, stats = Melee, Defense, Sword,
   Gun, Power (fruit). DataStore saving with session locking + retries (ProfileStore pattern).
2. Power Fruits ("[FRUIT NAME]"): types = Natural, Elemental (intangibility vs. normal
   attacks), Beast (transformations). Each fruit: 5 moves (Z, X, C, V, F), mastery levels
   unlocking moves, awakening system later. Fruit spawning under trees, gacha dealer,
   inventory/storage, one eaten fruit at a time.
3. Combat: fighting styles, swords, guns; M1 combo chains, hitboxes (spatial queries, not
   .Touched), stun/knockback, cooldowns, energy bar, dash/geppo-style air jump, haki-style
   buffs (armament damage boost, observation dodge). All damage validated on the server.
4. World: 3 seas, each with [N] islands, level-gated. Quest givers per island, enemy
   spawners, bosses with respawn timers, sea events (sea beasts, ships).
5. Progression: quests, bounty/honor PvP system, titles, race system with evolutions.
6. Raids: party-based PvE dungeon waves, rewards fruit mastery/awakenings.
7. Economy & monetization: game passes (2x money, 2x mastery, fruit storage) — NO
   pay-to-win beyond conveniences; follow Roblox monetization and loot-box disclosure rules.
8. UI: HUD (HP/energy/XP bars), move hotbar with cooldowns, stats menu, inventory,
   shop, quest tracker, mobile buttons. Clean anime style.
9. Anti-exploit: never trust the client; RemoteEvent rate limiting and sanity checks
   (distance, cooldown, ownership); server-side movement checks.

## CODE ARCHITECTURE
- Rojo + VS Code project, folder structure: src/server, src/client, src/shared.
- ModuleScript-based services (Knit-style or plain modules), typed Luau (--!strict).
- Data-driven configs: each fruit/sword/enemy defined in a shared config table.
- One reusable Ability framework (cast → validate → effects → cooldown) used by all moves.
- Comment code, and give me complete files, not fragments, with exact paths.

## BLENDER MODEL PIPELINE
For every asset, give me step-by-step Blender instructions (Blender 4.x) plus export specs:
- Assets: characters' accessories, fruits (glossy swirl-patterned, original designs),
  swords, guns, NPCs, enemies, bosses, ships, island props, trees, buildings.
- Style: stylized low-poly, clean silhouettes, readable from far away and on mobile.
- Poly budgets: props 200–2k tris, weapons 500–3k, bosses ≤ 10k per MeshPart
  (Roblox limit: 20k tris per MeshPart; split larger models).
- Scale: 1 Blender unit ≈ 1 stud workflow; set unit scale so imports are correct;
  apply all transforms (Ctrl+A) before export; origin at sensible pivot (grip for swords).
- UVs: single non-overlapping UV map, texel density consistent; textures 1024² (max),
  PBR via SurfaceAppearance (Color, Normal, Roughness, Metalness) or vertex color/flat
  palette textures for performance.
- Rigging: R15-compatible rigs for humanoid NPCs/bosses; custom rigs for beast forms.
  Animations (idle, walk, run, M1 combo, each fruit move, transformation) exported and
  uploaded through Roblox Animation Editor workflow.
- Layered clothing / accessories: follow Roblox cage mesh requirements.
- Export: FBX (or OBJ for static), "Selected Objects", Apply Modifiers, Forward -Z,
  Up Y; import via Roblox 3D Importer; set CollisionFidelity (Box/Hull) and
  RenderFidelity appropriately; use LODs/StreamingEnabled for islands.
- VFX: explain how to build fruit move effects in Roblox (ParticleEmitters, Beams, Trails,
  mesh VFX made in Blender with scrolling textures) and tie them to the Ability framework.

## HOW TO WORK WITH ME
- Work in phases. Phase 1: project setup + data saving + stats. Phase 2: combat + 1 sword
  + 1 fighting style. Phase 3: first fruit (all 5 moves + VFX + Blender model).
  Phase 4: first island, quests, enemies, boss. Phase 5: UI polish. Phase 6: more seas,
  raids, PvP, monetization. Phase 7: optimization, testing, launch.
- At the start of each phase, list deliverables; at the end, give a test checklist.
- When I report bugs, ask for Output errors and reproduce steps, then give fixed full files.
- Keep everything original and within Roblox Terms of Use / Community Standards.

Start with Phase 1 now: give the folder structure, Rojo config, and full code for the data
service and stat system, plus the Blender steps for my first asset: [FIRST ASSET, e.g. a
starter katana].
```

## Quick Tips
- Paste the prompt once at the start of a chat, then say "next phase" to continue.
- For a single asset, ask: *"Using the Blender pipeline rules above, walk me through modeling [ASSET] step by step."*
- For a single fruit, ask: *"Design an original [ELEMENT] fruit: lore, 5 moves with damage/cooldowns, VFX plan, Blender model steps, and full Luau code using our Ability framework."*
