# Master Prompt: Demon Slayer RPG for Roblox (in the style of Blox Fruits)

**How to use:** edit the `CONFIG` block below, then paste everything under the line into Claude (or another coding
AI). It builds the game one milestone at a time. Reply `continue` to get the next milestone, or `fix: <problem>` to
have it repair something first.

---

## CONFIG

```
GAME_NAME        = "Crimson Night"
PLATFORM         = Roblox (Luau, Roblox Studio; Rojo-compatible folder layout)
NAMING_MODE      = CANON      # CANON = use Demon Slayer names.
                              # ORIGINAL = rename every character, place, style and form to original names
                              #            with the same feel. Use ORIGINAL if the game will be public.
SCOPE            = FULL       # FULL = milestones M0 to M7. SLICE = M0 to M3 only (a playable Region 1).
PLAYERS_PER_SERVER = 16
DEVICES          = PC, mobile, console (every action needs a button and a gamepad bind)
MONETIZATION     = OFF        # ON adds optional gamepasses and cosmetics (see section 17)
ADMIN_USER_IDS   = {}         # Roblox UserIds allowed to use debug commands
```

---

## 1. Your role

You are a senior Roblox game designer and Luau engineer. You have shipped large anime action RPGs like Blox Fruits,
Project Slayers and Demonfall. You are building **{GAME_NAME}**, a Roblox action RPG set in the world of Demon
Slayer (Kimetsu no Yaiba). It plays like Blox Fruits: a long level grind across several regions, NPC quest givers,
stat points, bosses, raids, rare powers you collect and master, and a PvP rivalry between two sides.

The twist: every player chooses to be a **Demon Slayer** or a **Demon**.
- **Slayers** learn and master **Breathing Styles**. They work like Blox Fruits swords and fighting styles: you
  collect many, train each one, and equip one at a time.
- **Demons** get **Blood Demon Arts**. They work like Blox Fruits devil fruits: they are rare, rolled, dropped,
  traded, and you can hold only one at a time.

You write complete, working, production-quality code. You make sensible design calls yourself and list them as
assumptions. Don't stop to ask questions.

---

## 2. Pillars

1. **The grind feels good.** Every 10 to 20 minutes, the player gets a new level band, a new move, a new area or a
   new drop.
2. **The two sides are different, not mirrored.** Slayers are skill and discipline: they are fragile and rely on
   parries and gauge management. Demons are raw power and survival: they regenerate, burn in sunlight and gamble
   on Arts.
3. **The canon is the progression path.** Final Selection, Natagumo, the Mugen Train, the Entertainment
   District, the Swordsmith Village and the Infinity Castle are the level bands, in story order.
4. **Every rare thing has a story.** Sun Breathing, Moon Breathing, the Blue Spider Lily and Mythical Arts come
   from secret questlines, not just luck.
5. **Combat is readable.** Moves are telegraphed, combos have counterplay, and nothing stun-locks forever.

---

## 3. Core loop

```
Accept quest (NPC / Kasugai Crow / Blood Messenger)
  → defeat N enemies or a boss in the area for your level band
  → earn XP, Yen and mastery on whatever you used
  → spend stat points, buy or unlock moves, upgrade gear
  → move to the next area when your level outgrows the current one
  → bosses and events drop rare items (Blood Vials, blades, accessories, Spirit Fragments)
  → endgame: raids, Mark / Awakening, seat duels, PvP rank
```

Session targets for tuning: Level 100 in about 1 hour, Level 700 (Region 2) in about 8 to 10 hours, Level 1500
(Region 3) in about 30 hours, max Level 2000 in about 50 hours.

---

## 4. Factions

### Choosing
New players see a cinematic choice screen when they first join: **"Take up the blade"** or **"Accept the blood"**.
- **Slayer** spawns at Snowfall Village at the foot of Mount Sagiri. The tutorial is with the masked trainer, and
  Water Breathing is given for free.
- **Demon** spawns at an abandoned shrine in the forest at night. The tutorial is "the Demon King's blood": the
  player gets a free roll of a Common or Uncommon Art (weighted).

### Switching later (expensive and rare)
- **Slayer to Demon:** the *Muzan's Blood* item (rare boss drop, or 1,000,000 Yen from a secret NPC that only
  appears during a Blood Moon). Breathing Styles and mastery are **kept but locked**, and the player starts with a
  random Art.
- **Demon to Slayer:** *Tamayo's Cure* questline in Asakusa (Level 700+). The current Art is **kept but locked**, and
  the player gets Water Breathing back plus any styles they learned before.
- Level, stats, Yen and inventory carry over. Faction-only items are locked, not deleted.

### The core differences

| | Slayer | Demon |
|---|---|---|
| Power source | Breathing Style (Z X C V F), learned from trainers. Many owned, one equipped | Blood Demon Art (Z X C V F), rolled or eaten from a Blood Vial. One at a time |
| Resource | **Breath** gauge. Refills while not attacking; hold E to refill faster | **Blood** gauge. Refills by dealing damage and absorbing defeated enemies |
| Weapon | Nichirin Blade (M1 combo + 2 blade moves) | Demon Claws (M1 combo + 2 claw moves) |
| Survival | Normal HP. Out-of-combat regen only once Total Concentration: Constant is unlocked | **Regeneration:** strong at night, weak in combat, none in sunlight |
| Weakness | Fragile; must parry and dodge | **Sunlight** (heavy damage per second), **wisteria** zones, Nichirin damage |
| Day / night | Same strength all day; bonus XP for hunting at night | Stronger at night and during Blood Moons; must find shade during the day |
| Rank | Corps ranks (Mizunoto to Kinoe), then a Hashira seat | Stray, Named, Lower Moon 6 to 1, Upper Moon candidate, then an Upper Moon seat |
| Enhancement (like Haki) | **Total Concentration: Full Focus** (J): damage and defense buff that drains Breath | **Blood Hardening** (J): armor and damage buff that drains Blood |
| Sense (like Observation Haki) | **Scent Tracking** (T): highlights enemies and gives dodge charges | **Blood Sense** (T): same, plus shows wounded players |
| Endgame form | **Demon Slayer Mark**, then **See-Through World**, then **Red Blade** | **Awakened Art**, then **Upper Moon Form** |
| Home and safe zone | Butterfly Mansion, wisteria houses (hurt demons) | Demon hideouts, the Infinity Castle antechamber (sun-proof) |

---

## 5. Levels, stats and ranks

- **Max level:** 2000. XP to the next level: `floor(40 * L^1.7 + 150)`. Keep every number in `GameConfig`.
- **Stat points:** 3 per level. Each stat caps at 2000, so a max-level player can max three of the four stats.
  - **Vitality:** max HP (`100 + 20*Vit + 5*Level`)
  - **Strength:** fighting-style (unarmed) damage
  - **Blade:** Nichirin Blade / Demon Claw damage
  - **Technique:** Breathing Style / Blood Demon Art damage, plus max Breath or Blood
- **Stat reset:** a *Reset Scroll* item (boss drop, or Yen that gets more expensive each time).
- **Slayer ranks** (permanent; each unlocks shops, quests, a haori color and +2% damage):
  Trainee → **Mizunoto** (pass Final Selection, Lv 150) → Mizunoe 250 → Kanoto 350 → Kanoe 450 →
  Tsuchinoto 600 → Tsuchinoe 750 → Hinoto 900 → Hinoe 1050 → Kinoto 1200 → **Kinoe** 1400 →
  **Pillar Candidate** (Lv 1500 + Hashira Trial).
- **Demon ranks** (permanent): Stray Demon → **Named Demon** (Lv 300 + Notoriety) →
  **Lower Moon Six to One** (Lv 600 Kizuki Trial, then every 100 levels, needs Notoriety) →
  **Upper Moon Candidate** (Lv 1500 + the Demon King's Trial).
- **Server seats** (temporary crowns you can lose; see section 13): 9 **Hashira** seats and 6 **Upper Moon** seats
  per server.

---

## 6. The world

Three regions, unlocked by story quests. Each area has a **Slayer quest giver** (a Kasugai Crow on a post) and a
**Demon quest giver** (a Blood Messenger in the shadows). Each has its own enemy families. Slayers hunt demon NPCs;
demons hunt Slayer Corps NPCs. Bosses can be fought by both sides, with faction-specific quests and loot tables.

### Region 1: The Mountain Villages (Lv 1 to 650)

| Area | Levels | Slayer targets | Demon targets | Bosses |
|---|---|---|---|---|
| Snowfall Village & Mount Sagiri | 1 to 75 | Feral Demon (5), Starved Demon (30), Mountain Demon (55) | Bandit (5), Trainee Swordsman (30), Kakushi Scout (55) | none |
| Fujikasane Mountain (wisteria ring) | 75 to 175 | Selection Demon (90), Wisteria-Starved Demon (125) | Selection Candidate (90), Rookie Slayer (125) | **Hand Demon** (175) |
| Asakusa (night city) | 175 to 300 | Alley Demon (190), Lantern Demon (240) | Mizunoto Patrol (190), Lantern Squad (240) | **Susamaru & Yahaba** (300) |
| Tsuzumi Mansion (rotating rooms) | 300 to 425 | Mansion Demon (320), Tongue Demon (370) | Mizunoe Slayer (320), Kanoto Slayer (370) | **Kyogai** (425) |
| Mount Natagumo | 425 to 650 | Spider Demon (450), Spider Brother (520), Spider Mother (580) | Kanoe Squad (450), Tsuchinoto Slayer (520), Kakushi Cleanup (580) | **Rui, Lower Moon Five** (625), **Water Hashira** (650), **Insect Hashira** (650) |

**Region gate (Lv 650):** Slayers are sent by their crow to board the Mugen Train. Demons are summoned to the
Infinity Castle by the Biwa and dropped off in Region 2.

### Region 2: The Capital Line (Lv 650 to 1500)

| Area | Levels | Slayer targets | Demon targets | Bosses |
|---|---|---|---|---|
| Mugen Train (moving train, carriage by carriage) | 650 to 850 | Flesh Spawn (675), Dream Puppet (750) | Train Guard Slayer (675), Hinoto Slayer (750) | **Enmu, Lower Moon One** (850), **Flame Hashira** (850) |
| Yoshiwara Entertainment District | 850 to 1050 | Obi Puppet (875), District Demon (950) | Hinoe Slayer (875), Sound Squad Ninja (950) | **Daki & Gyutaro, Upper Moon Six** (1050), **Sound Hashira** (1050) |
| Swordsmith Village (hot springs, forges) | 1050 to 1300 | Pot Fish Demon (1075), Rage Clone (1175) | Kinoto Slayer (1075), Corps Elite (1175) | **Yoriichi Type Zero** puppet (1150), **Gyokko, Upper Moon Five** (1200), **Hantengu, Upper Moon Four** (1300), **Mist Hashira** and **Love Hashira** (1300) |
| Hashira Training Grounds | 1300 to 1500 | Night Raider Demon (1325), Blood-Frenzied Demon (1400) | Kinoe Slayer (1325), Hashira Tsuguko (1400) | **Wind Hashira** (1450), **Stone Hashira** (1500). World boss: **Akaza, Upper Moon Three** (1400, appears at dawn) |

**Region gate (Lv 1500):** the Ubuyashiki Estate falls. Everyone drops into the Infinity Castle.

### Region 3: The Endless Night (Lv 1500 to 2000)

| Area | Levels | Slayer targets | Demon targets | Bosses |
|---|---|---|---|---|
| Infinity Castle: Shifting Halls | 1500 to 1650 | Castle Demon (1525), Biwa Sentinel (1600) | Infiltrator Slayer (1525), Kinoe Strike Team (1600) | **Nakime** (1650), secret: **Kaigaku** (1700) |
| Infinity Castle: Lotus Hall | 1650 to 1800 | Ice Lotus Wraith (1675), Frost Acolyte (1750) | Tsuguko Pair (1675), Mark-Bearer Slayer (1750) | **Doma, Upper Moon Two** (1800) |
| Infinity Castle: Moonlit Dojo | 1800 to 1900 | Crescent Blade Demon (1825), Upper Rank Hopeful (1875) | Pillar Guard (1825), Mark-Bearer Elite (1875) | **Kokushibo, Upper Moon One** (1900), **Serpent Hashira** (1900) |
| Dawn City (the final night) | 1900 to 2000 | Muzan's Spawn (1925), Flesh Tendril (1975) | Corps Vanguard (1925), Hashira Echo (1975) | Raid: **Muzan, the Demon King** (2000). Raid: **Pillar Gauntlet** (2000) |

**Travel:** a map menu. Slayers fast-travel by Kasugai Crow (unlocked per area once visited). Demons travel by
Biwa portal. Both take 3 seconds and can't be used in combat.

**Safe zones:** Butterfly Mansion (Region 1/2 Slayer hub), wisteria houses in each area (deal 2% max HP per second
to demons and block Arts), demon hideouts (sun-proof, block Slayer quests), and the Trade Hall in each region (no
PvP).

---

## 7. Quests

- **Level quests** (the main grind, like Blox Fruits): "Defeat 6 to 8 <enemy>." The NPC offers the
  highest-level quest the player qualifies for in that area. Reward: XP = `XPToNext(questLevel) / 4`, plus Yen.
  The tracker shows progress, and finished quests can be turned in remotely from the quest menu.
- **Boss quests:** "Defeat <boss>." Big XP/Yen, plus a roll on the boss's loot table for your faction.
- **Story quests** (once, with short dialogue): faction choice, Final Selection, region gates, rank trials and
  breathing-style trainer quests. Done as dialogue boxes and objectives; no cutscene tech needed.
- **Daily Crow / Messenger contracts:** 3 per day. Mixed objectives (e.g. parry 20 attacks, win 1 PvP fight,
  defeat a boss without taking sun damage). Rewards: Spirit Fragments and Yen.
- **Secret questlines** (hidden, discovered through world clues): Hinokami Kagura (Sun Breathing), Moon Breathing,
  Blue Spider Lily, Honoikazuchi no Kami, Tamayo's Cure.

Quests are data-only definitions (see section 18). Adding a quest should never need new code.

---

## 8. Combat

### Controls (rebindable; mobile buttons and gamepad binds for everything)

| Key | Action |
|---|---|
| 1 / 2 / 3 / 4 | Equip Fighting Style / Blade or Claws / Breathing Style or Art / Consumable |
| M1 | Basic combo (4 hits; the 4th knocks back). Resets after 1.2 s |
| Z X C V F | Moves of the equipped tool (Fighting Style and Blade/Claws have Z X C; Breathing and Arts have all five) |
| R (hold) | Block: 80% damage reduction, drains a Guard meter. Tap R within 0.2 s of a hit to **Parry**: attacker stunned 0.8 s |
| Q | Dash (i-frames 0.15 s, 3 charges). Upgrades: Flash Step (longer), Air Dash |
| Space ×2 | Air Step (double jump, unlocked at Lv 60; triple at Lv 800) |
| E (hold) | Focus: refill gauge while standing still. Demon near a defeated enemy: **Absorb** |
| T | Scent Tracking / Blood Sense |
| J | Full Focus / Blood Hardening |
| Y | Mark / Awakened Form (endgame) |
| M, Tab | Map; main menu (stats, inventory, styles/arts, quests, settings) |

### Rules
- **The server decides all hits.** The client sends intent (move id, aim direction). The server checks cooldown,
  gauge, mastery and range, then runs the hitbox (`GetPartBoundsInBox` / shapecasts from the server-side character
  position), applies damage and status, and broadcasts a VFX event. The client plays its animation and VFX
  straight away for responsiveness.
- **Damage:** `MoveBase * (1 + Stat/400) * (1 + Mastery*0.0005) * RankMult * ClanMult * Buffs * BlockMult`. Bosses
  and players get a 25% damage cut against attackers more than 200 levels below them.
- **Anti-stunlock:** each hit adds Poise Damage. At 100, the target gets 1 s of hit immunity and a free dash.
  Ragdoll/knockdown at most once every 4 s.
- **Status effects:** Burn, Poison (stacks), Bleed, Slow, Stun, Sleep (breaks on hit), Freeze, Bind,
  Breath Disruption (halves Breath regen; Doma's mist), **Sunscorch** (stops regeneration; Sun Breathing and real
  sunlight). One status framework with duration, stacks, tick, and source tracking.
- **Damage tags:** every hit carries tags such as `Nichirin`, `Sun`, `Wisteria`, `Blood`, `Projectile` and
  `Unblockable`. Faction rules key off these tags.
- **Demon defeat rule:** if a demon (player or NPC) reaches 0 HP from a hit **without** the `Nichirin` or `Sun` tag,
  it collapses for 3 s, then regenerates to 15% HP (once every 90 s). Nichirin/Sun kills, and kills while the
  revive is on cooldown, are final. Death VFX: the body dissolves into drifting ash.
- **Absorb (demons):** a defeated enemy NPC stays for 3 s. Hold E next to it to heal 15%, gain 30 Blood and +20% XP
  for that kill. Shown as red mist flowing into the demon; nothing graphic.

---

## 9. Breathing Styles (Slayers)

- Slayers **own** every style they learn and equip one at a time (Tab menu or a trainer). Mastery is saved per style.
- Mastery (max 600) is earned by dealing damage and finishing quests with the style equipped. Moves unlock at
  these mastery levels:
  - Common: 0 / 25 / 75 / 150 / 250
  - Rare: 0 / 50 / 100 / 200 / 350
  - Legendary: 0 / 75 / 150 / 275 / 400
  - Mythical: 0 / 100 / 200 / 350 / 500
- At 600 mastery, a style can be **Refined** at its trainer with Spirit Fragments: each refinement upgrades one move
  (+20% damage, new VFX, plus one extra effect). Refining all five unlocks that style's Hashira-grade haori cosmetic.
- Each style has a **passive**, plus a Nichirin blade colour that tints the VFX.

| Style | Rarity | How to get it | Passive | Z | X | C | V | F |
|---|---|---|---|---|---|---|---|---|
| **Water** | Common | Free (tutorial, Mount Sagiri) | Moving reduces damage taken by 10% | Water Surface Slash | Water Wheel | Flowing Dance | Striking Tide | Constant Flux (dragon) |
| **Thunder** | Common | Lv 150, 25k Yen, Asakusa hills trainer: 3 lightning-parkour trials | +15% dash distance | Thunderclap and Flash | Rice Spirit | Thunder Swarm | Heat Lightning | Thunderclap and Flash: Godspeed (Sixfold at 400 mastery, Eightfold at 600) |
| **Beast** | Common | Lv 400, Natagumo outskirts: beat the boar-masked hermit barehanded | Dual blades; crits on targets under 30% HP | Pierce | Slice 'n' Dice | Crazy Cutting | Spatial Awareness (reveal + buff) | Whirling Fangs |
| **Flame** | Rare | Lv 700, 250k Yen, defeat Enmu once, Rengoku estate trainer | Hits apply Burn | Unknowing Fire | Rising Scorching Sun | Blazing Universe | Flame Tiger | Ninth Form: Rengoku |
| **Insect** | Rare | Lv 750, Butterfly Mansion: wisteria-poison trial | Thrusts stack Poison (max 5) | Butterfly Dance: Caprice | Bee Sting: True Flutter | Dragonfly: Compound Eye Hexagon | Centipede: Hundred-Legged Zigzag | Wisteria Bloom (detonates stacks) |
| **Flower** | Rare | Lv 800, Butterfly Mansion: coin-toss + parry trial | Parry window +0.1 s | Honorable Shadow Plum | Crimson Hanagoromo | Peonies of Futility | Whirling Peach | Equinoctial Vermilion Eye (slow-mo vision buff; costs HP) |
| **Sound** | Rare | Lv 900, defeat Daki & Gyutaro once, Yoshiwara trainer | Hits leave sound marks that explode on the 3rd hit | Roar | Constant Resounding Slashes | Explosive Score | String Performance | Musical Score: Finale |
| **Mist** | Legendary | Lv 1100, Swordsmith Village: beat Yoriichi Type Zero | 10% chance to dodge attacks | Low Clouds, Distant Haze | Eight-Layered Mist | Scattering Mist Splash | Lunar Dispersing Mist | Obscuring Clouds (invisibility + afterimages) |
| **Love** | Legendary | Lv 1300, Hashira Training Grounds trial | Whip blade: +40% range | Shivers of First Love | Love Pangs | Catlove Shower | Swaying Love: Wildclaw | Cat-Legged Winds of Love |
| **Serpent** | Legendary | Lv 1300, Hashira Training Grounds trial | Curving slashes ignore side-blocking | Winding Serpent Slash | Venom Fangs of the Narrow Head | Coil Choke | Twin-Headed Reptile | Slithering Serpent |
| **Wind** | Legendary | Lv 1350, beat the Wind Hashira | Hits launch enemies upward | Dust Whirlwind Cutter | Claws-Purifying Wind | Rising Dust Storm | Black Wind Mountain Mist | Idaten Typhoon |
| **Stone** | Legendary | Lv 1400, beat the Stone Hashira | Flail and axe; +30% poise | Serpentinite Bipolar | Upper Smash | Stone Skin (armour buff) | Volcanic Rock: Rapid Conquest | Arcs of Justice |
| **Sun (Hinokami Kagura)** | Mythical | Lv 1600 + secret questline (below) | Sun tag on all hits: stops regeneration, +30% vs demons | Dance | Clear Blue Sky | Burning Bones, Summer Sun | Sunflower Thrust | Dragon Sun Halo Head Dance |

**Secrets:**
- **Hinokami Kagura questline:** get the *Hanafuda Earrings*. Kamado-clan players get them from a hidden
  Snowfall Village NPC; everyone else needs 3 *Memory Shards* (5% drops from Rui, Akaza and Kokushibo). Then
  perform the Fire God Dance on a full-moon night at Snowfall Village summit: a rhythm minigame, 12 timed key
  prompts. **Thirteenth Form:** casting Z X C V F within 6 s chains into a cinematic finisher (60 s cooldown).
- **Honoikazuchi no Kami** (Thunder, Seventh Form): Agatsuma clan **or** Thunder mastery 600 + defeat Kaigaku.
  Adds an alternate F (hold F).
- **Fighting styles** (unarmed, both factions, bought with Yen + Strength requirements, 3 moves each): Basic
  Combat, Boar Rush, Hand-to-Hand (Corps), Martial Arts of Soryu (Destructive-Death-inspired; Demon only, Lv 1000).

---

## 10. Blood Demon Arts (Demons)

- A demon holds **one Art at a time**. Eating a new *Blood Vial* replaces it (confirmation prompt). Mastery is saved
  per Art, so swapping back later keeps progress.
- **Ways to get Arts (Blox Fruits style):**
  1. **Night Vials:** each in-game night, one random Blood Vial spawns at a random point in each region (glowing red,
     server announcement only during a Blood Moon). It despawns at dawn.
  2. **Blood Dealer:** an NPC in each demon hideout with 4 to 6 Arts in stock, rotating every 4 real hours (same
     stock on every server: use a time-seeded RNG). Priced in Yen by rarity.
  3. **Demon King's Favour (gacha):** one roll every 2 real hours, for Yen that scales with level. Weights:
     Common 40 / Uncommon 28 / Rare 18 / Legendary 10 / Mythical 4.
  4. **Boss drops:** each canon demon boss has a small chance (1 to 3%) to drop **its own** Art.
- Vials can be **stored** (2 storage slots, +1 at Lower Moon, +1 at Upper Moon Candidate) and **traded** (section 15).
- Mastery thresholds match the rarity tiers in section 9 (Uncommon: 0 / 35 / 90 / 175 / 300; Lunar = Mythical).
- **Awakening** (like Blox Fruits awakening): from Region 2 on, spend Spirit Fragments from **Infinity Castle
  raids** to awaken each move (+25% damage, new VFX, an extra effect). Awakening all 5 unlocks **Awakened Form** (Y):
  a 30 s transformation with a new look, +15% damage and mobility. Cooldown 120 s.

| Art | Rarity | Passive | Z | X | C | V | F |
|---|---|---|---|---|---|---|---|
| **Grasping Arms** (Hand Demon) | Common | Melee grabs reach further | Arm Lash | Pinning Grasp | Arm Wall (block) | Burrowing Hands | Hundred-Arm Prison |
| **Shadow Swamp** (Swamp Demon) | Common | Sink into the ground to move (gauge drain) | Sink Strike | Swamp Ambush | Triplet Clones | Mire Pool (slow zone) | Drowning Depths |
| **Thorn Flesh** | Common | Melee attackers take a small reflect | Thorn Burst | Spine Volley | Bramble Guard | Root Snare | Iron Maiden Bloom |
| **Temari** (Susamaru) | Uncommon | Six arms: faster M1 | Temari Toss | Ricochet | Six-Arm Barrage | Heavy Temari | Temari Storm |
| **Vector Arrows** (Yahaba) | Uncommon | Redirect projectiles while blocking | Arrow Push | Pin Down | Arrow Shield (reflect) | Gravity Slam | Arrow Maelstrom |
| **Spider Threads** (Rui) | Rare | Wall-climb; threads slow on hit | Thread Lash | Cutting Web | Thread Cocoon (bind) | Spider Family (summon) | Carved Red Silk Cage |
| **Drum** (Kyogai) | Rare | Drum hits rotate the target's camera | Drum Claws | Room Spin | Rapid Beat | Pressure Paw | Mansion Shift (shuffles nearby enemies) |
| **Dream** (Enmu) | Legendary | Hits build Drowsy; 5 stacks = Sleep | Sleep Song | Dream Tendrils | Nightmare Puppets | Flesh Fusion | Enforced Dream (AoE Sleep) |
| **Obi Sashes** (Daki) | Legendary | Long-reach M1 | Obi Whip | Obi Barrage | Silk Prison | Eight-Layered Obi | Flowing Obi Nest |
| **Blood Sickles** (Gyutaro) | Legendary | Hits apply Poison | Flying Blood Sickle | Rampaging Wheels | Rotating Circular Slash | Poison Blood Rain | Sickle Tempest |
| **Exploding Blood** (Nezuko) | Legendary | Burns demons (+50% vs demons); C buffs an ally | Blood Burst Kick | Crimson Flame Splash | Blood Ignition (ally blade buff) | Size Shift (poise up) | Exploding Blood Nova |
| **Destructive Death** (Akaza) | Mythical | Compass Needle: auto-aims and counters | Air Type | Disorder | Compass Needle (counter stance) | Leg Type: Flash Flame Flowing | Annihilation Type: Blue Silver Chaotic Afterglow |
| **Cryokinesis** (Doma) | Mythical | Freezing mist: Breath Disruption | Freezing Lotus Fans | Frozen Mist | Barren Hanging Garden | Ice Bodhisattva (summon) | Crystalline Divine Child (clones) |
| **Emotion Clones** (Hantengu) | Mythical | **True Body:** once per life, a "final" kill leaves a tiny fleeing body. Survive 6 s to revive at 20% | Sekido Thunder | Karaku Wind Fan | Urogi Sonic Cry | Aizetsu Spear | Zohakuten Wood Dragons |
| **Pots** (Gyokko) | Mythical | Teleport between placed pots | Pot Warp | Thousand Needle Fish | Water Prison Pot | Octopus Pot | Perfected Form: Jinmai Scales |
| **Biwa** (Nakime) | Mythical | Strumming moves reposition targets | Biwa Strum (teleport target) | Door Drop | Castle Reshuffle (party teleport) | Eyes Everywhere (reveal) | Infinity Castle (temporary arena) |
| **Moon Breathing** (Kokushibo) | Lunar | Hybrid: Breath and Blood gauges both active | Dark Moon, Evening Palace | Pearl Flower Moongazing | Loathsome Moon, Chains | Moon Dragon Ringtail | Catastrophe: Tenman Crescent Moon |
| **Demon King's Flesh** (Muzan) | Lunar | Strong regeneration, tendril M1 | Whip Tendrils | Flesh Maws | Shockwave Burst | Bio-Cannon | Thousand-Tendril Storm |

**Lunar Arts are never in the gacha or dealer.**
- **Moon Breathing:** you must be a Demon at Lv 1500 who mastered any Breathing Style to 600 *before* turning, and
  have defeated Kokushibo. You get it from his quest NPC after a 1v1 trial.
- **Demon King's Flesh:** 0.5% drop from the Muzan raid (demon players only). Tradeable.

---

## 11. Weapons and accessories

- **Nichirin Blades** (Slayers): Standard (starter), Corps-grade (shop, Lv 150), Swordsmith-forged
  (Swordsmith Village, needs ore drops), Hashira-grade (drops from Hashira bosses), and *Yoriichi's Blade* (0.5%
  from Yoriichi Type Zero). Each blade: M1 damage, 2 moves (Z, X), a passive, and a level requirement. Blade
  mastery unlocks the moves. The **Red Blade** (endgame) makes any blade's hits apply Sunscorch.
- **Demon Claws:** Lesser Claw, Hardened Claw, Lower Moon Claw, Upper Moon Claw, Kizuki Talons (drops and demon
  shops). Same structure as blades.
- **Accessories** (1 slot, stat bonuses): Fox Mask (Final Selection, −5% damage taken), Haori of each Hashira (boss
  drops), Hanafuda Earrings, Kizuki Eye (demon, +Blood regen), Obi Sash, Temari, Swordsmith Hyottoko Mask, Wisteria
  Charm (Slayer, damage vs demons).
- **Consumables** (slot 4): Wisteria Darts (Slayer, poison throw), Sun Parasol (Demon, 45 s sun immunity but can't
  attack), Healing Herbs, Kasugai Flare (calls a crow to mark a target for your squad).

---

## 12. Day, night, sun and wisteria

- **Cycle:** 24 real minutes: night 14, dawn 1, day 8, dusk 1. Shown on the HUD as a sun/moon dial with a countdown.
- **Blood Moon:** every 6th night. Demons +20% damage, double Night Vial spawns, Lower Moon Trial bosses spawn, and a
  secret NPC sells *Muzan's Blood*. Slayers get double Merit for demon kills.
- **Sunlight:** during day/dawn, a demon is *exposed* if a raycast 300 studs straight up (ignoring characters,
  accessories and transparent parts) hits nothing. Exposure gives a 2 s warning (screen tint, sizzling sound), then
  4% max HP per second. Arts are disabled, regen stops, and the demon gets an ash-smoke VFX. Shade, interiors,
  forests, caves, the Mugen Train interior and the Infinity Castle are safe. Region 3 is permanently night (the
  Infinity Castle) except for the Dawn City raid's finale.
- **Wisteria:** wisteria houses and the Fujikasane ring hurt demons (2% per second) and block Arts. Slayer poison
  items and Insect Breathing apply wisteria Poison, which doubles in effect against demons.
- **Blue Spider Lily** (demon secret): once per in-game day, during daytime, one flower blooms at a random location
  across Regions 1 and 2 (hinted by crows and messengers). A demon who picks it up, through the sunlight, gains
  **Sun-Touched** permanently: sun damage −75% (not immunity). A Slayer who finds it can hand it to Tamayo for a
  large Yen + Fragment reward, which denies the demons. One per server per day.

---

## 13. Bosses, raids, events and seats

- **Bosses:** each has 2 or 3 phases, a health bar, a respawn timer (8 to 15 min) and telegraphed attacks (red
  ground decals). Loot goes to everyone above 5% damage contribution, not just the last hit. Canon mechanics to
  include:
  - **Daki & Gyutaro:** both must be finished within 5 s of each other, or the dead one revives.
  - **Hantengu:** at low HP he splits into the four emotion clones; damage only sticks once the hidden true body is
    found.
  - **Kyogai:** drum hits rotate the arena's gravity.
  - **Enmu:** fought across train carriages; Sleep pulls you into a dream phase you have to escape.
  - **Akaza:** appears at dawn in Region 2 and flees at full sunrise. Any damage is kept when he respawns.
  - **Kokushibo:** crescent-blade patterns that grow at each phase.
  - **Muzan:** see raids.
- **Raids** (parties of up to 4, started with a *Raid Seal* bought from a raid NPC, 25 min timer):
  - **Infinity Castle Raid** (Region 2+): 5 rooms of waves, then a random Upper Moon echo. Rewards Spirit Fragments.
    Seal types (Ice, Thread, Flame, Sound, Pot, Biwa...) set the enemy theme and the bonus drop table.
  - **Sunrise Siege: Muzan** (Lv 1900+, up to 8 players): survive and deplete Muzan's HP. If the raid timer hits
    sunrise, Muzan is scorched (the final phase becomes a 60 s burn race). Slayers fight him as the Corps; demons as
    **Usurpers** trying to take his blood. Drops: Red Blade unlock token, Lunar Art chance, legendary cosmetics.
  - **Pillar Gauntlet** (Lv 1900+): fight echoes of all 9 Hashira in a row. Demon-favoured drops.
- **World events:** Final Selection (Region 1, every 40 min: survive 5 min in the wisteria ring; first completion
  gives the Mizunoto rank), Blood Moon, Akaza at Dawn, Kasugai Bounty (a random high-value NPC or player target).
- **Seats** (per-server crowns, from section 5):
  - 9 **Hashira seats**, one per Hashira style. Needs Pillar Candidate rank + that style at 600 mastery equipped.
  - 6 **Upper Moon seats**. Needs Upper Moon Candidate rank.
  - An empty seat is claimed at the Seat Shrine (or Infinity Castle throne room). An occupied seat can be
    challenged to a 1v1 duel in a separate arena. The holder has 3 minutes to accept, or they forfeit. If the
    holder leaves the server, the seat is freed.
  - Seat perks: a title, an aura, +10% damage, and a "Seat Bounty" (other faction players get Merit/Notoriety for
    defeating them).

---

## 14. Endgame powers

**Slayers**
1. **Total Concentration: Constant** (Lv 300, Butterfly Mansion quest): +50% Breath regen, out-of-combat HP regen.
2. **Demon Slayer Mark** (Lv 1200, Mark Trial: survive the Hashira gauntlet at 30% max HP). Toggle Y when the Rage
   meter is full (it fills from damage taken and dealt): 20 s of +20% damage and speed. Costs 10% max HP when it
   ends.
3. **See-Through World** (Lv 1700, trial in the Moonlit Dojo): while the Mark is active, parries trigger automatic
   counter-slashes and enemy wind-ups show through walls.
4. **Red Blade** (Muzan raid token): see section 11.

**Demons**
1. **Regeneration** (start), boosted at Named Demon and at each Moon rank.
2. **Awakened Art** (section 10).
3. **Upper Moon Form** (Upper Moon Candidate + all moves awakened): a 40 s form with kanji eyes and extra limbs, +25%
   damage, sun damage −50%, and every move gets a follow-up. Toggled with Y; the Blood gauge must be full.

---

## 15. Economy, inventory and trading

- **Currencies:** **Yen** (quests, enemies, bosses; spent on trainers, shops, gacha, seals). **Spirit Fragments**
  (raids, bosses, dailies; spent on refining, awakening, rerolls, Raid Seals). Optional premium currency only if
  `MONETIZATION = ON`.
- **Inventory:** tabs for Blades/Claws, Accessories, Vials, Consumables and Materials. Vials have a storage limit
  (section 10); the others have generous caps.
- **Trading:** Trade Hall NPCs in each region. Two players open a trade window with up to 4 items each plus Yen.
  Both lock, both confirm, there's a 5 s countdown, then the server swaps everything in **one atomic operation**
  through both loaded profiles. Trades can't include bound items (quest items, Lunar Art for 24 h after it drops).
  Log every trade.

---

## 16. Clans (like Blox Fruits races)

Rolled on first join. Rerolled with a *Clan Token* (Fragments, or Robux if monetized). Each has a passive, plus a
Slayer bonus or a Demon bonus:

| Clan | Chance | Passive | Slayer bonus | Demon bonus |
|---|---|---|---|---|
| Common Family | 45% | +5% XP | none | none |
| Hashibira | 15% | +10% move speed | Beast: +10% damage | Absorb heals +5% |
| Agatsuma | 12% | +1 dash charge | Thunder: +10% damage; Seventh Form route | Faster gauge refill when asleep or stunned |
| Rengoku | 10% | +5% damage | Flame: +15% damage | +5% Blood Hardening |
| Tokito | 8% | +15% mastery gain | Mist: +10% damage | +10% regeneration |
| Kamado | 5% | Smell: Scent Tracking range +50% | Sun questline earrings; Water/Sun +10% | Exploding Blood +15%; sun damage −15% |
| Shinazugawa | 3% | **Marechi blood:** nearby demons are Slowed | Wind: +10% damage | Absorb gives +50% Blood |
| Ubuyashiki | 1.5% | Foresight: +8% crit chance | Pillar Candidate trial easier (−20% boss HP) | Notoriety gain +15% |
| Tsugikuni | 0.5% | Mark unlocks at Lv 900; +10% damage | Sun +20% damage | Moon Breathing +20% damage |

---

## 17. PvP, Merit and Notoriety

- PvP is **opt-in** in Region 1 (toggle in the menu, 30 s to activate). From Region 2 on, it's on by default
  outside safe zones, and turning it off has a 60 s timer and is blocked for 30 s after combat.
- Same-faction players can't hurt each other, except in duels and arenas.
- **Merit** (Slayers) goes up when you defeat demon players or major demon bosses. **Notoriety** (Demons) goes up
  when you defeat Slayer players or Hashira bosses. Both drop when you're defeated by the other side. They gate
  ranks, cosmetics and seat eligibility.
- Demons with more than 1M Notoriety show on Slayers' maps as a "crow sighting". Slayers with more than 1M Merit show
  on demons' maps.
- **Squads** (Slayers) and **Covens** (Demons): groups of up to 12 with a name, chat, shared XP when nearby (+10%)
  and a combined Merit/Notoriety leaderboard.
- **Leaderboards** (OrderedDataStore, refreshed every 5 min): Level, Merit, Notoriety, Muzan raid clear time.
- If `MONETIZATION = ON`: 2× Mastery, 2× Yen, +2 Vial Storage, Fast Travel Anywhere, and cosmetic auras. **Nothing
  that adds raw damage.** Any paid random reward must show its odds (Roblox policy).

---

## 18. Technical architecture (Roblox)

Write `--!strict` Luau with type annotations. Use a Rojo-style tree so it can be synced or recreated by hand:

```
src/
  ReplicatedStorage/Shared/
    Config/GameConfig.luau          -- every tunable number lives here
    Data/                            -- pure data, no logic
      BreathingStyles.luau  BloodArts.luau  FightingStyles.luau  Weapons.luau
      Moves.luau            Enemies.luau    Bosses.luau          Areas.luau
      Quests.luau           Items.luau      Clans.luau           Ranks.luau
      Shops.luau            Raids.luau      Animations.luau      Sounds.luau
    Types.luau
    Net/Remotes.luau                 -- one table defining every RemoteEvent/RemoteFunction
    Util/ (Signal, Trove/Maid, RNG, TableUtil, Format)
  ServerScriptService/Server/
    Bootstrap.server.luau
    Services/
      DataService           -- ProfileStore session locking, schema version, migrations
      ProgressionService    -- XP, level, stat points, ranks
      CombatService         -- damage pipeline, tags, block/parry, poise, death and revive rules
      HitboxService         -- server spatial queries, multi-hit, projectiles
      AbilityService        -- validates and executes moves from Moves.luau data
      StatusService         -- status effects framework
      EnemyService          -- NPC spawning, pooling, state-machine AI, aggro, leashing
      BossService  RaidService  EventService  SeatService
      QuestService  ShopService  GachaService  InventoryService  TradeService
      DayNightService  SunExposureService  ZoneService (safe zones, wisteria, regions)
      PvPService  SquadService  LeaderboardService
      AntiExploitService    -- rate limits, payload validation, movement sanity
      AdminService          -- debug commands, ADMIN_USER_IDS only
    Abilities/              -- custom handlers only for moves the generic executor can't express
  StarterPlayer/StarterPlayerScripts/Client/
    Bootstrap.client.luau
    Controllers/ Input, Combat, Camera, HUD, Menu, Dialogue, QuestTracker, Map, Notifications,
                 VFX, Sound, MobileButtons, Gamepad
    VFX/        -- one module per style/art: visuals only
```

**Move definition** (data drives everything; most moves need no custom code):

```lua
{
  id = "water_constant_flux", key = "F", owner = "Water", masteryReq = 250,
  cost = 35, cooldown = 14, windup = 0.35, lockMovement = 0.6,
  hitbox = { shape = "Box", size = Vector3.new(10, 8, 30), offset = CFrame.new(0, 0, -15),
             duration = 0.9, ticks = 6 },
  damage = { base = 120, scaling = "Technique" },
  tags = { "Nichirin", "Water" }, status = { { id = "Slow", duration = 1.5 } },
  movement = { dash = 40, direction = "Look" },
  vfx = "Water_ConstantFlux", sfx = "Water_Splash", anim = "Water_F",
  customHandler = nil,  -- name of a module in Server/Abilities if needed
}
```

**Player data schema** (versioned; write a migration function per version):
`version, faction, clan, level, xp, statPoints, stats{Vitality,Strength,Blade,Technique}, yen, fragments,
slayer{rank, merit, styles{[id]=mastery}, equippedStyle, refinements{}}, demon{rank, notoriety, art, artMastery{},
awakened{}, sunTouched}, inventory{weapons[], accessories[], vials[], consumables{}, materials{}},
equipped{weapon, accessory, fighting}, quests{active, story{}, dailies{}, secrets{}}, unlocks{}, cooldowns{gacha,
dealerSeenAt}, settings{}, stats{kills, deaths, playtime}, tradeLog[]`.

**Non-negotiables:**
- **The server is the only authority** for damage, hits, currency, XP, drops, cooldowns and inventory. Clients only
  send intent. Validate every remote payload (types, ranges, ownership). Rate-limit every remote per player with
  a token bucket.
- Use ProfileStore (or ProfileService) for **session locking** so items can't be duplicated. Saves autosave every
  60 s, on leave and on `BindToClose`.
- **Performance:** StreamingEnabled. Enemies only simulate within 250 studs of a player. AI ticks at 8 to 10 Hz.
  Enemies are pooled. All VFX, damage numbers and sounds are client-side. Keep remote traffic under about 30 events
  per second per player.
- **Animations and assets:** don't depend on Toolbox models. Load `Animations.luau` and `Sounds.luau` by id, and if
  an id is empty, fall back to a procedural tween pose so the game still works. Build VFX from ParticleEmitters,
  Beams, Trails and tweened neon parts. Areas start as clean, readable greybox geometry built by a script
  (`WorldBuilder`), so they can be replaced with real builds later.
- **Admin commands** (`ADMIN_USER_IDS` only): `/level n`, `/give item`, `/yen n`, `/faction slayer|demon`,
  `/time day|night|bloodmoon`, `/spawnboss id`, `/mastery n`, `/reset`.
- **Tests:** a `Tests` folder with pure-function tests (XP curve, damage formula, gacha weights sum to 100, quest
  data validity, every move's owner exists, every quest's enemy exists). Runnable from a server script, with a
  pass/fail summary in Output.

---

## 19. UI and feel

- **HUD:** HP bar, Breath/Blood gauge (blue wave / red pulse), XP bar with level, Yen and Fragments, a sun/moon dial,
  a sun-exposure warning for demons, a 5-slot move bar with cooldown sweeps and lock icons for moves you haven't
  mastered yet, the quest tracker, and boss bars.
- **Menus:** Stats (spend points with +1/+10/+100), Styles/Arts (equip, mastery bars, move list with previews),
  Inventory, Quests, Map (fast travel), Clan, Settings (keybinds, camera shake, VFX quality, sound).
- **Style:** anime ink-wash. Bold kanji headers (canon kanji for Breathing forms in CANON mode), paper textures
  made from UI gradients, Taisho-era colours (indigo, vermilion, gold). Damage numbers pop with a brush-stroke
  style.
- **Juice:** hitstop of 0.05 s on heavy hits, camera shake (scaled by setting), slash trails coloured by blade,
  rising kanji for each form name, a crow caw when quests update, a taiko hit sound for parries.

---

## 20. Content rules

- Follow the Roblox Community Standards and target an all-ages or 9+ rating: **no gore, no dismemberment, no
  realistic blood**. Defeats dissolve into ash or petals. "Decapitation" is a slash flash followed by ash. Demons
  **absorb essence**; they never eat humans. Use dark red energy instead of blood.
- No slurs or real-world hate symbols. Make sure nothing on the map or in UI looks like one.
- If `NAMING_MODE = ORIGINAL`, consistently rename every canon proper noun (characters, places, styles, form names,
  arts, ranks) to original names with the same tone, and keep a mapping table in `Data/Names.luau`.
- Use no copyrighted art, music or audio. Everything is original or made in-engine.

---

## 21. Build plan

Build in this order. After each milestone, **stop** and wait for me to say `continue`.

| Milestone | Delivers | Done when |
|---|---|---|
| **M0: Foundations** | Folder tree, GameConfig, Types, Remotes, Util, DataService with schema + migrations, faction choice screen, clan roll, admin commands, test runner | Join, pick a side, rejoin and the data is still there. Tests pass |
| **M1: Combat core** | M1 combos, dash, block/parry, poise, damage pipeline, tags, status framework, death/respawn, demon revive rule, generic move executor, **Water Breathing** + **Temari** fully playable, HUD basics, mobile and gamepad controls | A Slayer and a Demon can duel with all 5 moves each, on PC and mobile |
| **M2: The grind** | XP, levels, stats menu, enemy AI + spawning, quest system, Region 1 greybox (all 5 areas), quest givers for both factions, Yen shops, fighting styles, Air Step | A new player can go from Lv 1 to 175 using quests only |
| **M3: Collecting power** | Trainers + Thunder and Beast, mastery unlocks, Blood Vials (night spawns, dealer, gacha, boss drops), every Common to Rare Art, storage, inventory, trading, Region 1 bosses, Final Selection event, Slayer and Demon ranks to Lv 650 | Region 1 is complete and fun from start to finish |
| **M4: Day and night** | Day/night cycle, sun exposure, wisteria, Blood Moon, Scent Tracking/Blood Sense, Full Focus/Blood Hardening, Total Concentration: Constant, Absorb, PvP toggle, Merit/Notoriety | Demons have to plan around the sun; PvP works with rewards |
| **M5: Region 2** | Mugen Train, Yoshiwara, Swordsmith Village, Hashira Grounds, the remaining Rare and Legendary styles and Arts, the bosses with their canon mechanics, Akaza at dawn, Infinity Castle raids, Spirit Fragments, refining and awakening, Tamayo's Cure, faction switching | Lv 650 to 1500 is playable |
| **M6: Region 3 & endgame** | Infinity Castle areas, Mythical and Lunar content, Sun and Moon Breathing questlines, Honoikazuchi, Mark, See-Through World, Red Blade, Awakened and Upper Moon forms, seats, Muzan raid, Pillar Gauntlet, Blue Spider Lily | Lv 2000 reachable; all secrets obtainable |
| **M7: Polish & launch** | Balancing pass (a table of time-to-level by band), leaderboards, squads/covens, settings, optional monetization, analytics events, exploit review, performance review on a low-end phone | A checklist of launch blockers, all cleared |

---

## 22. How to format each reply

For every milestone:
1. **Summary:** what was built, in 5 to 10 bullets.
2. **File tree:** every file added or changed.
3. **Code:** the complete contents of every new or changed file. **No placeholders, no `-- TODO`, no `...`, no
   "rest stays the same."** If a file passes about 400 lines, split it into modules.
4. **Studio setup:** exact steps for anything that can't be scripted (enabling API Services for DataStores,
   StreamingEnabled, where to paste each script, Rojo instructions).
5. **Test checklist:** 5 to 15 concrete things to try in Play Solo / a local 2-player server, with expected results.
6. **Assumptions & known limits:** decisions you made and anything that's deferred to a later milestone.

Write all user-facing text in English, stored in one `Data/Strings.luau` table so it can be translated later. Keep
naming consistent: PascalCase modules, camelCase functions, UPPER_SNAKE constants.

---

## 23. Start now

Reply with:
1. A design summary of **{GAME_NAME}** in 150 words or fewer, to confirm you understood the game.
2. Your assumptions for anything this prompt leaves open.
3. **Milestone M0**, delivered in the format from section 22.
