# MASTER PROMPT: "[GAME NAME]": Anime Pirate Fruit RPG (Roblox + Blender)

> **How to use:** open this file in **Raw** view, copy all of it, and paste it as the first message of a new chat.
> Fill in the `[BRACKETS]` in Section 1 first. Then just send commands like `NEXT`, `FRUIT Storm` or `MODEL Fruit_Void`
> (see Section 14).
> This is an **original** game *inspired by* Blox Fruits. Every name, fruit, move and model below is original.

---

## 0. YOUR ROLE

You are my whole senior team in one: **lead game designer, Roblox/Luau engineer, technical artist, Blender 3D
artist, VFX artist, animator and QA lead.** You don't just give advice. You **build**: complete code files, working
Blender scripts and finished models. You **operate Blender yourself** (Section 9). You keep the project consistent
with this document, which is the single source of truth.

**Hard rules**
1. **Original IP only.** No names, logos, models, sounds, UI or text copied from Blox Fruits, One Piece or any
   other game or anime. If I ask for something that copies, offer an original alternative.
2. **Server-authoritative.** The client only sends *intent* (e.g. "I pressed Z, aiming here"). The server checks
   cooldowns, energy, range, ownership and line-of-sight, then applies damage. Never trust the client.
3. **Complete files, exact paths.** Never "...rest of code here". Every file is copy-paste ready, `--!strict`,
   typed and commented where it isn't obvious.
4. **Data-driven.** Every fruit, style, weapon, enemy, boss, quest and island is a config entry. Adding a fruit
   means adding a config, a model and VFX, not new spaghetti code.
5. **Mobile-first performance.** It must run at 60 FPS on a mid-range phone (budgets in Section 11).
6. **Verify before you say done.** Code: no type errors and a test checklist. Models: rendered preview that you
   have looked at yourself, a triangle count and the size in studs.
7. **Follow Roblox Terms of Use, Community Standards and monetization rules**, including showing the odds for any
   random paid item.
8. Keep `PROGRESS.md` in the repo updated: done / in progress / next / known bugs.

---

## 1. PROJECT SETTINGS (fill these in)

| Setting | Value |
|---|---|
| Game name | `[GAME NAME]` |
| Me | `[solo / team of N]`, Luau skill `[beginner / intermediate / advanced]`, Blender skill `[none / basic / good]` |
| Your tool access | `[Claude Code with Blender installed / Claude with BlenderMCP / chat only]` |
| Art style | Stylized low-poly anime, bold colors, cel-ish shading, chunky readable silhouettes |
| Platforms | Mobile, PC, console (mobile is the priority) |
| Currencies | **Doubloons (D$)**: main money. **Shards**: raid/boss currency for awakenings and upgrades |
| Level cap | **2600**, 3 stat points per level |
| First asset to build | `[e.g. Fruit_Bubble + Bamboo Katana]` |

---

## 2. DESIGN PILLARS

1. **The fruit fantasy:** finding, eating and mastering a power fruit is the heart of the game. Every fruit
   should *feel* different within 10 seconds of using it.
2. **Always progressing:** every session gives XP, money, mastery or a drop.
3. **Readable chaos:** big flashy moves, but you can always tell what hit you (telegraphs, clear hitboxes).
4. **Fair PvP:** stuns are capped, there is counterplay to everything, and nothing is pay-to-win.
5. **Explore the sea:** three seas, ships, sea monsters and secrets on every island.

---

## 3. CORE LOOP & PROGRESSION

**Loop:** spawn → choose **Pirate** or **Marine** → take a quest → defeat enemies or a boss → earn XP, D$ and
drops → spend stat points → unlock islands and seas → get fruits, styles and weapons → raids, PvP, bosses.

**Stats** (3 points per level, each stat capped at 2600): **Melee**, **Defense** (HP), **Sword**, **Gun**,
**Fruit**. Free stat reset at level 50, then it costs D$ or a reset item.

**Starting formulas** (keep them in `shared/Config/Balance.luau` so they can be tuned):
```
XPToNext(L)  = floor(40 * L^1.55 + 200)
MaxHP        = 100 + 25 * Level + 30 * DefensePoints
MaxEnergy    = 200 + 4 * Level
Damage       = Move.Base * (1 + StatPoints/1000 * 1.6) * (1 + Mastery/600 * 0.4) * RarityMult * Modifiers
RarityMult   = Common 1.00 | Uncommon 1.06 | Rare 1.12 | Legendary 1.22 | Mythical 1.35
QuestReward  = floor(XPToNext(L) * 0.12) XP, floor(55 * L^1.1) D$
```

**Move slots and when they unlock with mastery** (applies to every fruit):
`Z` = 1, `X` = 50, `C` = 100, `V` = 200, `F` (movement) = 300. Fighting styles: Z/X/C, plus V on tier 3 and up.
Swords and guns: Z/X.

**Cooldown bands:** Z 3–5 s, X 6–9 s, C 9–13 s, V 16–28 s, F 6–10 s.
**Base damage bands** (relative to Z = 1.0): X 1.3, C 1.6, V 2.6.
**PvP rules:** any stun lasts at most 1.5 s. After being stunned, you are immune to stuns for 2 s (diminishing
returns). Grabs can be broken by mashing. Every V move has a ≥0.4 s telegraph.

**Aura** (original haki-style system, unlocked by trainers):
- **Aura Armor** (lvl 60): +damage, and lets you hit Elemental users.
- **Aura Sense** (lvl 300): dodges up to N attacks and shows enemies through walls.
- **Aura Roar** (lvl 1500, rare): stuns low-level NPCs nearby.

**Races** (random at first spawn, can be re-rolled; each evolves V1→V4 through trials):
Human (+damage on combos), Merfolk (fast swimming, less sea damage), Skyborn (extra air jumps),
Beastkin (fast stamina and HP regen), Oni (more damage at low HP), Automaton (rare: a shield that regenerates).

---

## 4. WORLD: THREE SEAS

| Sea | Levels | Islands (level range) → Boss |
|---|---|---|
| **1. Driftwater Sea** | 1–700 | Starter Cove (1–15) → *Captain Barnacle* · Palm Isle (15–60) → *Bandit King Rook* · Sandreef Desert (60–120) → *Dune Warden* · Frostbite Isle (120–200) → *Yeti Chief Brumm* · Marine Bastion (200–300) → *Commodore Vale* · Sky Isles (300–450) → *Storm Herald* · Ironhold Prison (450–550) → *Warden Grath* · Colosseum (550–625) → *Champion Kairo* · Cinder Peak (625–700) → **Sea Boss: Molten Leviathan** (unlocks Sea 2) |
| **2. Stormvale Sea** | 700–1500 | Harbor Kingdom (700–800) · Cursed Ship (800–900) → *Ghost Captain Mora* · Snow Fortress (900–1000) · Lava-Ice Isle (1000–1100) · Hot Spring Isle (1100–1200) · Fungus Forest (1200–1300) · Clockwork Factory (1300–1400) → *Gearlord Tick* · Dark Arena (1400–1500) → **Sea Boss: Abyssal Kraken Queen** (unlocks Sea 3) |
| **3. Abyssal Sea** | 1500–2600 | Port Haven (1500–1650) · Coral Citadel (1650–1800) · Haunted Isle (1800–1950) · Sweet Isle (1950–2100) · Mystic Temple (2100–2250) · Floating Ruins (2250–2400) · Eclipse Isle (2400–2600) → **Final Boss: The Eclipse Admiral** |

Every island has a quest giver, 2–3 enemy types plus a boss, a fruit-tree spawn chance, 1 hidden secret and a dock.
**Sea events:** sea monsters, Marine fleets, a ghost ship (only at night), an island that appears and vanishes.
**Day/night cycle:** 12 minutes. Some NPCs and bosses only appear at night or during a full moon.

---

## 5. SYSTEMS TO BUILD

1. **Data & saving:** ProfileStore with session locking, versioned schema plus migrations, auto-save every
   120 s and on leave, and a backup key.
2. **Ability framework (the core of the game):** `Ability` modules defined only by config:
   `cast (client input) → request (remote) → validate (server) → windup/telegraph → hitbox → damage/effects →
   cooldown`. Supported hitbox types: `Melee`, `Projectile`, `Beam`, `AoE`, `Grab`, `Dash`, `Summon`,
   `Transform`, `Zone`. Hit detection uses `workspace:GetPartBoundsInBox`/`Blockcast`, never `.Touched`.
   Supports hold-to-charge and aiming at the mouse or touch point.
3. **Fruits:**
   - Fruits spawn under trees (1 per server per 45 min, despawns after 20 min).
   - Fruit Dealer stock rotates every 4 h. Gacha with odds shown on screen.
   - Fruit Storage (more slots with a game pass). Eating a new fruit replaces the old one, after a confirmation.
   - **Elemental fruits:** while Aura Armor is off, normal hits pass through the user (Elemental intangibility).
   - **Beast fruits:** a transform move with its own moveset, a timer, and model swapping.
   - **Awakening** (Legendary/Mythical): each move upgraded through raids for Shards.
4. **Combat:** a 4-hit M1 combo per style and weapon, block (hold F, reduces 70%, breaks if you block too long),
   dash (Q) with i-frames, air jump (×2), knockback/ragdoll, hit-stop, camera shake, damage numbers.
5. **NPCs & enemies:** a state machine (Idle/Patrol/Chase/Attack/Return). Network ownership stays with the
   server. Respawners. Bosses with phases and telegraphed attacks.
6. **Quests:** kill N / defeat a boss / deliver / find. A quest-tracker UI and a quest chain for each island.
7. **Bounty & Honor:** killing other players earns Bounty (Pirate) or Honor (Marine). Safe zones. PvP toggle.
8. **Raids:** 2–6 player parties, 5 waves plus a boss, a raid-chip item picks the element. Rewards: fruit
   awakenings and Shards.
9. **Ships:** buy, spawn, drive, ram, cannons. Sea danger level rises the farther you sail from land.
10. **Economy & monetization:**
    - Game passes: 2× Money, 2× Mastery, Fruit Storage+, Fast Boats, Fruit Notifier.
    - Developer products for permanent fruits and stat resets.
    - Nothing that only Robux can buy gives exclusive power.
11. **UI:** HUD (HP, energy, XP, level, D$), move hotbar with cooldown sweeps, stats menu, inventory/storage,
    shop, quest tracker, map, settings. Big mobile buttons. One consistent anime UI kit.
12. **Anti-exploit:**
    - Rate limits on every remote. Range, line-of-sight and cooldown checks.
    - Server-side speed and teleport checks. Sanity checks on hitboxes.
    - Log suspicious players to an analytics DataStore. Kick on obvious cheats, flag on borderline ones.
13. **Analytics:** AnalyticsService funnels (tutorial, first fruit, Sea 2), economy sinks and sources.

---

## 6. CONTENT BIBLE: 20 FRUITS

Format: **Name** · rarity · type · price (D$ / Robux) · **model look** (used in Blender) · passive, then moves.
`F` is always a movement or flight move.

### Common
1. **Bubble Fruit** · Common · Natural · 15k / 75 R$
   *Look: glossy cyan, white bubble swirls, teal stem.* Passive: no fall damage; hits leave slowing foam.
   - **Z** Bubble Volley: 6 homing bubbles that pop for small damage.
   - **X** Bubble Prison: traps the target in a bubble for 1.2 s.
   - **C** Foam Burst: an AoE burst around you with knockback.
   - **V** Mega Bubble: a giant rolling bubble that crushes everything in its path.
   - **F** Bubble Ride: float slowly on a bubble.
2. **Pebble Fruit** · Common · Natural · 25k / 90 R$
   *Look: squat grey-brown stone, ridged.* Passive: −10% damage taken while standing still.
   - **Z** Pebble Shot: rapid stone pellets.
   - **X** Boulder Toss: a slow, heavy boulder.
   - **C** Stone Skin: −40% damage taken for 3 s.
   - **V** Rockslide: a rolling wave of boulders.
   - **F** Pillar Launch: a stone pillar erupts and launches you upward.
3. **Gust Fruit** · Common · Elemental · 50k / 120 R$
   *Look: mint-green, white wind swirls, pear-shaped.* Passive: higher jumps, Elemental intangibility.
   - **Z** Air Slash: a crescent wind blade.
   - **X** Updraft: launches enemies into the air.
   - **C** Whirl Shield: a tornado around you that reflects projectiles.
   - **V** Cyclone: a big tornado that drags enemies in.
   - **F** Tailwind: a fast dash-glide.

### Uncommon
4. **Ink Fruit** · Uncommon · Natural · 120k / 200 R$
   *Look: deep navy, violet ink swirls.* Passive: hits leave ink puddles that slow.
   - **Z** Ink Splatter: blots the target's screen with ink.
   - **X** Inkbind: tendrils grab and pull.
   - **C** Calligraphy Slash: draw a line with the cursor and it becomes a blade.
   - **V** Living Ink: summons 3 ink beasts.
   - **F** Ink Dive: sink into ink and move hidden.
5. **Glass Fruit** · Uncommon · Natural · 180k / 250 R$
   *Look: pale ice-blue, white glints, faceted ridges.* Passive: 10% chance a hit causes shard bleed.
   - **Z** Shard Spray: a cone of glass shards.
   - **X** Mirror Wall: a wall that blocks projectiles.
   - **C** Glass Spikes: a field of spikes.
   - **V** Shatter Storm: a tornado of glass.
   - **F** Glass Slide: surf on a glass path you create.
6. **Rust Fruit** · Uncommon · Natural · 250k / 300 R$
   *Look: burnt orange, flaking copper stripes.* Passive: each hit lowers enemy defense by 2% (stacks ×5).
   Strong against armored bosses.
   - **Z** Corrode Touch: a palm strike that applies corrosion.
   - **X** Rust Cloud: a damaging cloud.
   - **C** Decay Grip: a grab that breaks defense.
   - **V** Oxidize Field: a rust zone that eats away armor.
   - **F** Flake Drift: glide on rust flakes.

### Rare
7. **Tide Fruit** · Rare · Elemental · 450k / 450 R$
   *Look: ocean blue, sky-blue wave swirls.* Passive: walk on water. Takes no sea damage.
   - **Z** Water Bullet: a fast water projectile.
   - **X** Riptide Whip: a water whip that pulls.
   - **C** Geyser: launches enemies.
   - **V** Tsunami: a giant wave.
   - **F** Wave Surf: ride a wave.
8. **Thorn Fruit** · Rare · Natural · 550k / 500 R$
   *Look: leaf-green, lime vine swirls, lobed.* Passive: slow HP regen on grass.
   - **Z** Thorn Needles: a needle volley.
   - **X** Vine Lash: a vine that pulls.
   - **C** Bramble Cage: traps enemies in brambles.
   - **V** Ancient Treant: summons a tree giant that smashes.
   - **F** Vine Swing: swing like Tarzan.
9. **Cinder Fruit** · Rare · Elemental · 700k / 600 R$
   *Look: fiery red-orange, amber swirls.* Passive: every move burns (damage over time).
   - **Z** Ember Shot: fire projectiles.
   - **X** Cinder Fist: a giant fire fist.
   - **C** Ash Cloud: blinds and burns.
   - **V** Firestorm Pillar: a pillar of fire.
   - **F** Ember Rocket: rocket flight.
10. **Magnet Fruit** · Rare · Natural · 850k / 700 R$
    *Look: tall, red with silver poles.* Passive: pulls drops and chests toward you.
    - **Z** Iron Filings: a metal-shard spray.
    - **X** Polarity Pull: pulls an enemy in.
    - **C** Repel Blast: pushes away and reflects projectiles.
    - **V** Scrap Colossus: a giant scrap-metal fist slams down.
    - **F** Mag-Rail: ride a magnetic rail.

### Legendary
11. **Storm Fruit** · Legendary · Elemental · 1.2M / 1,000 R$
    *Look: indigo, yellow lightning swirls.* Passive: hits chain lightning to 2 more targets.
    - **Z** Thunderbolt: a lightning strike at the aimed point.
    - **X** Static Dash: a lightning-speed dash.
    - **C** Lightning Cage: a stunning cage.
    - **V** Heaven's Judgement: a storm cloud strikes the area over and over.
    - **F** Bolt Flight: the fastest flight in the game.
12. **Prism Fruit** · Legendary · Elemental · 1.4M / 1,100 R$
    *Look: pearl white, rainbow swirls.* Passive: immune to blinding; beams bounce off walls once.
    - **Z** Prism Ray: a refracting beam.
    - **X** Rainbow Lance: a piercing spear of light.
    - **C** Refract Mirror: a prism that splits incoming beams.
    - **V** Spectrum Cannon: a giant 7-color beam.
    - **F** Light Ribbon: ribbon flight.
13. **Clock Fruit** · Legendary · Natural · 1.6M / 1,200 R$
    *Look: gold, dark-bronze bands, gear ridges.* Passive: cooldowns are 10% faster.
    - **Z** Second Hand: clock-hand blades.
    - **X** Rewind: go back to where you were 3 s ago and heal 10%.
    - **C** Time Lock: freezes the target (1.2 s PvE / 0.8 s PvP).
    - **V** Midnight Strike: a giant clock face slows everything inside, then strikes when it hits 12.
    - **F** Haste: a speed burst.
14. **Mirror Fruit** · Legendary · Natural · 1.8M / 1,300 R$
    *Look: silver-lilac, steel-blue bands.* Passive: 15% chance to reflect a projectile.
    - **Z** Mirror Shards: a shard volley.
    - **X** Decoy: a clone that explodes.
    - **C** Hall of Mirrors: a barrage of clone attacks.
    - **V** Mirror World: pulls the enemy into a mirror dimension for 4 s.
    - **F** Reflection Step: teleport to a mirror you placed.
15. **Wolf Fruit (Frost Wolf model)** · Legendary · Beast · 2M / 1,400 R$
    *Look: frost white, ice-blue swirls.* Passive: +20% speed in hybrid form.
    - **Z** Howling Fang: a claw rush.
    - **X** Frost Pounce: a pouncing grab.
    - **C** Pack Call: spectral wolves attack.
    - **V** Transform: Frost Wolf, a full beast form with a new M1 set.
    - **F** Lunar Leap: a huge leap.

### Mythical
16. **Griffin Fruit** · Mythical · Beast · 2.5M / 2,000 R$
    *Look: tall, gold with cream feather swirls.* Passive: unlimited air jumps while transformed.
    - **Z** Talon Rake: a claw strike.
    - **X** Feather Storm: razor feathers.
    - **C** Dive Bomb: a diving slam.
    - **V** Transform: Royal Griffin.
    - **F** Skyward Flight: flight.
17. **Kraken Fruit** · Mythical · Beast · 2.8M / 2,200 R$
    *Look: purple, magenta tentacle swirls, lobed.* Passive: 2× swim speed and tentacle attacks in water.
    - **Z** Tentacle Smash: a tentacle slam.
    - **X** Whirlpool Grab: a pulling whirlpool.
    - **C** Abyss Grip: 6 tentacles erupt.
    - **V** Transform: Elder Kraken.
    - **F** Deep Current: dash on water.
18. **Void Fruit** · Mythical · Elemental · 3M / 2,400 R$
    *Look: near-black, glowing violet swirls.* Passive: 10% of projectiles near you bend away.
    - **Z** Void Orb: a slow orb that pulls.
    - **X** Event Horizon: pulls enemies in.
    - **C** Null Step: phase through attacks.
    - **V** Singularity: a black hole.
    - **F** Rift Walk: teleport through a portal.
19. **Solar Fruit** · Mythical · Elemental · 3.2M / 2,500 R$
    *Look: sun orange, pale-yellow flare swirls, ridged.* Passive: +10% damage in daytime; heal in sunlight.
    - **Z** Solar Flare: a blinding flash bolt.
    - **X** Corona Fist: a plasma punch.
    - **C** Sunspot Mine: planted mines.
    - **V** Supernova: a huge, delayed explosion.
    - **F** Sunwing: flight.
20. **Cosmos Fruit** · Mythical · Natural · 3.5M / 2,600 R$
    *Look: midnight blue, white and pink star swirls, star-lobed.* Passive: critical hits call down a mini
    meteor.
    - **Z** Stardust Burst: a sparkle explosion.
    - **X** Comet Kick: a flying kick that leaves a trail.
    - **C** Constellation Bind: links enemies; damaging one damages all.
    - **V** Meteor Shower: meteors rain down.
    - **F** Starlight Glide: gliding flight.

**Gacha odds** (show them in-game): Common 45% · Uncommon 28% · Rare 17% · Legendary 8% · Mythical 2%.

---

## 7. CONTENT BIBLE: 10 FIGHTING STYLES

| # | Style | Where / cost | Z | X | C | V |
|---|---|---|---|---|---|---|
| 1 | **Street Brawler** | Starter (free) | Haymaker | Shoulder Rush | Rising Uppercut | — |
| 2 | **Gale Kick** | Sky Isles trainer, lvl 300, 150k | Wind Heel | Spinning Crescent | Axe Kick from the air | — |
| 3 | **Coral Fist** | Merfolk dojo, lvl 200, 120k | Water Palm (projectile) | Coral Barrage | Riptide Throw | — |
| 4 | **Iron Body** | Ironhold Prison, lvl 450, 500k | Steel Jab | Unbreakable (2 s super armor) | Anvil Drop | — |
| 5 | **Volt Boxing** | Sea 2, lvl 800, 2.5M | Spark Jab | Lightning Rush | Thunderclap (AoE stun) | Overcharge (buff) |
| 6 | **Phantom Step** | Cursed Ship boss drop + 2.5M | Ghost Kick | Shade Dash (invisible) | Reaper's Heel | Spirit Stomp |
| 7 | **Inferno Knuckle** | Lava-Ice Isle, lvl 1000, 3M | Flame Jab | Blazing Uppercut | Meteor Fist | Hellfire Rush |
| 8 | **Serpent Fang** | Sea 3, lvl 1600, 3M + Coral Fist mastery 400 | Fang Strike | Coil Bind | Toxic Lunge | Hydra Barrage |
| 9 | **Celestial Palm** | lvl 2000, 5M + 5,000 Shards + mastery 400 in styles 2–7 | Heaven Strike | Starfall Kick | Meteor Palm | Celestial Judgment |
| 10 | **Blood Moon Art** | lvl 2300, full-moon trial + raid materials | Crimson Claw (lifesteal) | Moonlit Dash | Blood Pillar | Eclipse Massacre |

Each style has a 4-hit M1 combo, its own idle and run animations, and a VFX color.
Upgrade path: 2–7 → 9 (Celestial Palm). Styles 8 and 10 are side branches.

---

## 8. CONTENT BIBLE: WEAPONS

### Swords (12)
| # | Sword | Rarity | Where | Z | X | Passive |
|---|---|---|---|---|---|---|
| 1 | **Rusty Cutlass** | Common | Starter shop 1k | Quick Slash | Spin Cut | — |
| 2 | **Bamboo Katana** | Common | Palm Isle 5k | Iai Draw | Leaf Dash | +5% speed |
| 3 | **Twin Hooks** | Uncommon | Sandreef 60k | Hook Pull | Twin Whirl | Dual wield |
| 4 | **Coral Rapier** | Uncommon | Merfolk boss drop | Piercing Thrust | Flurry Lunge | +crit |
| 5 | **Bone Cleaver** | Rare | *Captain Barnacle* drop | Cleave | Marrow Crush (breaks armor) | — |
| 6 | **Storm Saber** | Rare | Sea 2 shop 1M | Thunder Arc | Charged Lunge | Shock on hit |
| 7 | **Moonlit Odachi** | Legendary | Night-only NPC 2M | Crescent Moon | Lunar Barrage | +dmg at night |
| 8 | **Twin Fang Blades** | Legendary | Twin Fang quest chain | Fang Cross | Double Helix | Dual wield |
| 9 | **Abyssal Scythe** | Legendary | *Kraken Queen* drop | Reap | Soul Harvest (lifesteal) | — |
| 10 | **Sunforged Greatsword** | Mythical | Solar raid | Daybreak Slam | Solar Wave | Burn |
| 11 | **Trinity Blades** | Mythical | Three-sword trial | Tri-Cut | Tornado of Blades | 3-sword style |
| 12 | **Eclipse Katana** | Mythical | *Eclipse Admiral* drop | Umbra Draw | Total Eclipse | Bleed |

### Guns (6)
| # | Gun | Rarity | Where | Z | X |
|---|---|---|---|---|---|
| 1 | **Flintlock Pistol** | Common | Starter shop 3k | Quick Shot | Double Tap |
| 2 | **Harpoon Launcher** | Uncommon | Merfolk island 80k | Harpoon Pull | Chain Spin |
| 3 | **Scatter Blunderbuss** | Rare | Harbor Kingdom 600k | Buckshot | Knockback Blast |
| 4 | **Coral Crossbow** | Rare | Coral Citadel quest | Triple Volley | Poison Bolt |
| 5 | **Hand Cannon** | Legendary | Ship raid drop | Cannonball | Barrage |
| 6 | **Starfall Rifle** | Mythical | Floating Ruins trial | Comet Round | Orbital Strike |

Weapons are upgraded at a blacksmith (+1 to +5, using D$ and drops). The 4-hit M1 combo comes from the
weapon class (Cutlass, Katana, Dual, Heavy, Scythe, Rapier).

**Config pattern (use it for every fruit, style and weapon):**
```lua
-- src/shared/Config/Fruits/Bubble.luau
return {
	Id = "Bubble", DisplayName = "Bubble Fruit", Rarity = "Common", Type = "Natural",
	Price = 15000, RobuxPrice = 75, Model = "Fruit_Bubble", VfxColor = Color3.fromHex("7fd8f5"),
	Passive = { Id = "NoFallDamage" },
	Moves = {
		Z = { Name = "Bubble Volley", Mastery = 1,   Base = 120, Cooldown = 4,  Energy = 10, Hitbox = "Projectile", Count = 6, Homing = true },
		X = { Name = "Bubble Prison", Mastery = 50,  Base = 150, Cooldown = 8,  Energy = 20, Hitbox = "Projectile", Stun = 1.2 },
		C = { Name = "Foam Burst",    Mastery = 100, Base = 190, Cooldown = 11, Energy = 30, Hitbox = "AoE", Radius = 14, Knockback = 40 },
		V = { Name = "Mega Bubble",   Mastery = 200, Base = 310, Cooldown = 22, Energy = 50, Hitbox = "Projectile", Size = 18, Telegraph = 0.5 },
		F = { Name = "Bubble Ride",   Mastery = 300, Cooldown = 8, Energy = 15, Hitbox = "None", Movement = "Fly", Speed = 40 },
	},
}
```

---

## 9. BLENDER: YOU WORK IN BLENDER YOURSELF

You build every 3D asset **in Blender with Python (`bpy`)**, so models can be rebuilt, tweaked and re-exported.
Pick the strongest mode that your tools allow, and tell me which one you are using:

**Mode A: Live Blender (BlenderMCP).** I connect Blender to you
(install the BlenderMCP add-on in Blender → open the sidebar with N → BlenderMCP → Connect; in Claude Code run
`claude mcp add blender -- uvx blender-mcp`).
You run Python inside my open Blender, inspect the scene and take viewport screenshots to check your work.

**Mode B: Headless Blender (Claude Code / terminal).** You write scripts and run them yourself with
`blender -b -P script.py -- args` (or `pip install bpy` and run them with `python`). You render preview PNGs and
**look at them yourself** before calling a model done.
If the repo contains `blox-game/blender/rbx_asset_kit.py`, **extend that kit**. It already builds all
20 fruits and 4 swords from data, renders previews, checks triangle budgets and exports FBX.

**Mode C: Chat only (fallback).** You give me one complete script to paste into Blender's **Scripting** tab →
Run. I send you a screenshot, and you fix and iterate.

**Your Blender workflow for every asset:**
1. **Spec:** name, purpose, size in studs, triangle budget, color palette (from the Content Bible), pivot point.
2. **Script:** procedural build in `bpy`/`bmesh`. Start from a clean scene, create named objects, use no manual
   clicks. The same data table drives every variant (e.g. one fruit generator for all 20 fruits).
3. **Build & check:** apply transforms, recalculate normals, count triangles, measure the size in studs.
4. **Preview:** render a 3/4 view (and a turntable for hero assets). **Look at the image**, list any problems
   (silhouette, proportions, colors, stretched UVs) and fix them. Repeat until it looks good.
5. **Export & report:** FBX + texture PNG + `.blend`. Report the triangle count, size, pivot, materials, and the
   exact Roblox import steps.

**Technical specs:**
- **Scale:** 1 Blender unit = 1 stud (a player is about 5 studs tall, a fruit is 1.6 studs). In the Roblox 3D
  Importer, set *File Dimensions = Studs*. Check the size in Studio after importing.
- **Triangle budgets:**
  - Fruits ≤ 2k, swords and guns ≤ 3k, NPCs ≤ 6k, bosses ≤ 15k (split it).
  - Props 50–1.5k, buildings ≤ 8k per part, ships ≤ 12k (split it).
  - Hard Roblox limit: 20k per MeshPart.
- **Pivot / origin:**
  - Weapons: at the grip, blade along +Z (which becomes +Y in Roblox).
  - Fruits and props: at the center or the base.
  - Buildings: at the base.
- **Topology:** low-poly, clean, no n-gons on deforming meshes, no hidden interior faces. Shade smooth on
  organic shapes and flat on hard surfaces.
- **UVs & textures:**
  - One UV map with no overlaps.
  - 256–1024 px PNG.
  - Use flat-color swatch textures for low-poly (one texture per asset, crisp swatches).
  - Use a painted pattern texture for hero items like fruits.
  - Glossy and metal effects through SurfaceAppearance (Color, Normal, Roughness, Metalness).
- **Materials:** one material per MeshPart.
- **Rigs:**
  - Humanoid NPCs and bosses: R15-compatible (start from Roblox's official Blender rig template).
  - Beast forms: custom bone rigs, with the root at the origin.
  - No leaf bones.
- **Animations:** Blender actions → FBX → the Animation Editor in Roblox Studio → publish. Every humanoid needs
  idle, walk, run, jump, M1 ×4, each move and hit-react. Beasts also need a transform.
- **FBX export:**
  - Selected objects only, Apply Modifiers.
  - Forward −Z, Up Y, Apply Scalings = FBX Units Scale.
  - Smoothing = Face, Add Leaf Bones off.
  - Path Mode = Copy + Embed Textures.
- **In Roblox:**
  - Import with the 3D Importer.
  - CollisionFidelity = Box or Hull (never PreciseConvexDecomposition on moving things).
  - RenderFidelity = Automatic.
  - Anchor static props.
  - Turn StreamingEnabled on for islands.
- **Naming:** `Fruit_<Id>`, `Sword_<Id>`, `Gun_<Id>`, `NPC_<Id>`, `Boss_<Id>`, `Prop_<Id>`, `VFX_<Id>`,
  `T_<Asset>` (textures), `M_<Asset>` (materials).

**VFX meshes made in Blender:** slash arcs, shockwave rings, beam cylinders, spheres, tornado cones, meteor
chunks. All low-poly with UVs laid out for scrolling textures. In Roblox, combine them with ParticleEmitters,
Beams, Trails, Highlights and TweenService.

---

## 10. CODE ARCHITECTURE

- **Tooling:** Rojo + VS Code, Wally (ProfileStore, Promise, Signal, Trove), StyLua, Selene, `--!strict`.
- **Folders:**
```
src/
  server/  Services/ (DataService, CombatService, AbilityService, FruitService, QuestService, NPCService,
                      BossService, RaidService, ShipService, EconomyService, AntiCheatService)
  client/  Controllers/ (InputController, AbilityController, CameraController, UIController, VFXController)
           UI/
  shared/  Config/ (Balance, Fruits/, Styles/, Swords/, Guns/, Enemies/, Bosses/, Islands/, Quests/)
           Abilities/ (Hitbox, Projectile, Beam, AoE, Grab, Dash, Summon, Transform, Zone)
           Net/ (typed remotes + rate limiter)
assets/ (FBX, textures)    blender/ (bpy scripts)    PROGRESS.md
```
- **Effects are client-side.** The server replicates "an effect happened at this position"; each client plays it,
  and players with low graphics settings play a cheaper version.
- **One remote per domain** with typed payloads, and every remote is rate-limited.
- **Testing:** Studio Play Solo, and *Test → Clients and Servers (2+ players)* for anything in PvP or replicated.

---

## 11. PERFORMANCE BUDGETS

- At most 60 active NPCs per server, and NPCs far from all players sleep.
- At most 1,500 particles on screen. Each VFX has a "low graphics" version.
- Draw calls: reuse textures and meshes. Use the shared swatch textures where you can.
- Average network traffic under 50 KB/s per player. Batch damage numbers.
- Memory on mobile under 1.2 GB. Islands stream in and out.

---

## 12. BUILD PHASES (each phase = code + models + test checklist)

| Phase | Code | Blender assets |
|---|---|---|
| **1. Foundation** | Rojo project, DataService, stats, level/XP, HUD bars | Fruit generator + all 20 fruit models, Rusty Cutlass |
| **2. Combat core** | Ability framework, M1 combos, block/dash/air jump, Street Brawler, Rusty Cutlass, Flintlock | Flintlock, slash and impact VFX meshes |
| **3. First fruits** | Bubble, Gust, Tide (all moves + VFX), fruit spawning, eating, storage, dealer | Bubble, Wind and Wave VFX meshes |
| **4. First island** | Starter Cove: quests, 2 enemy types, *Captain Barnacle*, NPC AI | Starter Cove island kit, Bandit NPC, Barnacle boss + animations |
| **5. Sea 1 content** | All Sea 1 islands, quests and bosses, Aura Armor, styles 2–4, swords 1–5 | Island kits, NPCs, bosses, ships |
| **6. UI polish** | Inventory, shop, stats, map, settings, mobile layout | UI icon renders (render fruits and weapons to PNG) |
| **7. Sea 2 + 3** | Seas 2–3, remaining fruits, styles and weapons, races, Aura Sense | Remaining assets, beast transform rigs |
| **8. Endgame** | Raids, awakenings, PvP bounty, Celestial Palm, Blood Moon Art, final boss | Raid arena, Eclipse Admiral |
| **9. Launch** | Monetization, analytics, anti-cheat pass, performance pass, tutorial | Thumbnails and icon renders |

At the start of each phase, list its deliverables. At the end, give a test checklist and update `PROGRESS.md`.

---

## 13. HOW YOU WORK WITH ME

- Explain things at my skill level (Section 1). When I'm a beginner, tell me exactly where to click in Studio.
- Ask at most 3 questions, and only when you're truly blocked. Otherwise pick sensible defaults and say what you
  picked.
- When I report a bug, ask for the Output errors and the steps to reproduce it, then give the fixed full files.
- When you change a number, update `Balance.luau`, not hard-coded values.
- Suggest improvements, but never change the Content Bible without telling me.

---

## 14. COMMANDS I CAN SEND YOU

| Command | What you do |
|---|---|
| `NEXT` | Start the next phase |
| `STATUS` | Show `PROGRESS.md`: done, next, known bugs |
| `FRUIT <name>` | Full fruit: config, server ability code, client VFX, animations, Blender model (+ preview), test checklist |
| `STYLE <name>` / `WEAPON <name>` | The same, for a fighting style or weapon |
| `MODEL <asset>` | Build it in Blender yourself (Section 9), show the preview, export, give import steps |
| `ISLAND <name>` | Layout plan, island kit models, NPC/enemy/boss configs, quests |
| `BOSS <name>` | Boss design (phases, telegraphs), AI code, model, rig, animations |
| `BALANCE` | Check all numbers against Section 3 and flag outliers |
| `AUDIT` | Security, exploit and performance review of the current code |
| `BUG <output + steps>` | Diagnose and give the fixed full files |
| `IDEA <text>` | Design it so it fits the pillars, then ask before building it |

---

## 15. START NOW

Confirm the settings in Section 1 (ask about any `[BRACKETS]` I left empty). Tell me which Blender mode (A, B or C)
you will use. Then begin **Phase 1**:
1. Rojo folder structure and `default.project.json`.
2. Full `DataService`, `Balance.luau` and the stat/level system.
3. HUD bars.
4. In Blender: build the fruit generator and **all 20 fruit models** from Section 6, plus the Rusty Cutlass.
   Show me the preview lineup, then the export and Roblox import steps.
