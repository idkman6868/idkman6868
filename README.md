# Jujutsu Kaisen: Cursed Domain — 0.3.0

NeoForge 21.1 / Minecraft 1.21.1 mod. On top of the 0.1.0 story (awakening → recruitment letter → Jujutsu High →
grades) it adds **Tokyo and Kyoto Jujutsu High** with real entrance interviews, **cities and landmarks** built into the
world, a **technique menu** with **five** ability slots, **Kenjaku's twenty-fingers offer** for Shrine users, the
**Shibuya Incident** and the **Culling Game**.

A ready-to-use jar is in [`dist/cursed_domain-0.3.0.jar`](dist/cursed_domain-0.3.0.jar). It replaces the older jar in
your `mods` folder; existing worlds keep working, and the new buildings appear in them too.

---

## Becoming a sorcerer: Tokyo & Kyoto Jujutsu High

The recruitment letter now leads somewhere. **Tokyo Jujutsu High** stands where the letter points: a walled compound
with torii gates, stone lanterns, a main hall, dorms, a training hall and ground, a pond and cherry trees.
**Kyoto Jujutsu High** is near Kyoto, with vermilion pillars and a pagoda.

1. Get awakened (curse attacks, a seal break, a cursed object…), then follow the letter.
2. Right-click **Principal Yaga** in the main hall (or **Principal Gakuganji** in Kyoto). He asks why you want to be a
   sorcerer. Click an answer in chat.
3. **Entrance exam:** Yaga sends his **cursed corpses** at you. Gakuganji releases curses the school keeps for
   exams. Defeat them within 3 minutes. Answering "for power" gets you a third one. Dying, running off or running
   out of time means you can retry after a 3-minute cooldown.
4. Answer his final question and you're a **student**: your **cursed technique is revealed and unlocked** (Grade 4).
   A title shows which technique you have.

*Curse user path* (needs `allowCurseUserPath`, on by default): hit Yaga twice instead of answering and you become a
**curse user**, which also unlocks your technique. Yaga can't be hurt (`yagaInvulnerable`).

## Technique menu & five slots

* There are now **five** quick slots. Slot 5 defaults to **M**, after Z/V/B/N, and the HUD bar shows all five.
  The ability wheel accepts 1–5.
* Press **K** to open the **technique menu**. It shows every ability you have (icon, description, cost, cooldown,
  grade requirement) and your five slots. Click an ability, then click a slot (or press 1–5) to equip it.
* The first time you load an old world, your slot assignments reset to abilities 1–5 once.

## Cities & landmarks

Each world gets these, placed from the seed (`/atlas` lists them with coordinates and how much is built):

| Landmark | What's there |
|---|---|
| **Tokyo Jujutsu High** | at the letter's location; Principal Yaga |
| **Shibuya** | the scramble crossing, Shibuya Station with Hachiko out front, the 109 building, the big screens, city blocks |
| **Tokyo (Minato)** | **Tokyo Tower** (100 blocks, two observation decks) in a dense city |
| **Kyoto** | wooden machiya streets, the five-storey pagoda, a Fushimi Inari tunnel of torii |
| **Kyoto Jujutsu High** | near Kyoto; Principal Gakuganji |
| **Sendai, Osaka, Yokohama** | modern cities: glass towers, offices, apartments, shop rows, parks, street lights |
| **Abandoned temple** | a ruined temple far from spawn, where **Kenjaku** waits |
| **Ruined colony cities** | when the Culling Game starts, the named colonies become wrecked cities |

They're built **chunk by chunk as players come near** (about 170 blocks out), so they also appear in existing worlds.
The ground is flattened under each footprint. Operators can add more with `/jjk landmark build <type>`.

## Kenjaku's offer: twenty fingers

If your innate technique is **Shrine**, find Kenjaku. He's at the abandoned temple (and during Shibuya and the
Culling Game). Right-click him and he offers to make you into **twenty fingers**; click
**[Make me into twenty fingers]**.

Your soul is split and you drift as a spectator for **5–10 seconds**. Then you **wake up in a random villager's
body**: you're teleported to a random loaded villager, which is gone, and everyone sees you as that villager
(profession included). If no villager is loaded, a new one is created somewhere 300–1200 blocks away and you wake there. You come back as the King of Curses:
**Special Grade**, the *incarnated sorcerer* trait, your complete domain unlocked, and full cursed energy. If you
weren't enrolled anywhere, you become a curse user.

---

## The Shibuya Incident

*October 31st, 19:00. A curtain falls over Shibuya.*

* **Start:** on its own at nightfall of world day **31** (`autoStartDay`), or with `/jjk event shibuya [pos]`.
  Everyone is told where the curtain is (`/jjk locate shibuya` shows it too).
* **The curtain** is a ring of dark smoke. **Non-sorcerers inside cannot leave**; sorcerers come and go.
  Curses are invisible to non-sorcerers, as in the base mod, so getting hit in there is a quick way to awaken.
* The incident waits until a player steps inside, then plays out in stages:
  1. **Curses pour in.** Three waves of Grade 3/2/1 curses.
  2. **The disaster curses.** Jogo (ember insects, *Maximum: Meteor*, then **Coffin of the Iron Mountain**),
     Hanami (wooden spears, roots, regenerates) and Dagon (fish volleys). Each has a boss bar.
  3. **Mahito.** His touch causes soul damage, he transfigures people into Transfigured Humans, and at half
     health he opens **Self-Embodiment of Perfection**. When he is nearly dead, **Kenjaku absorbs him**.
  4. **Prison Realm: Gate Open.** Gojo is sealed and **Kenjaku** fights you (Cursed Spirit Manipulation,
     *Maximum: Uzumaki*, Antigravity System). Once he is badly hurt he **escapes**. He drops the
     **Prison Realm** item and lets loose a last swarm of curses.
* If nobody enters within `abandonDays`, Kenjaku's plan succeeds without you.
* When it ends, every curse he released spreads through Tokyo. A minute later **the Culling Game begins**.

## The Culling Game

Kenjaku speaks to everyone. **Ten colonies** appear in a line running through the world, with Tokyo No. 1 standing
where Shibuya was:
Sakurajima · No. 6 · No. 7 · **Lake Gosho** · No. 8 · No. 9 · **Tokyo No. 2** · **Tokyo No. 1** · No. 10 · **Sendai**.
The five named colonies have their characters; the other five have generic players. Your source text didn't name
them, so they show as "Unnamed Colony"; rename them in the lang file if you like.

### Players, marks and Kogane

* **Marked ones:** non-sorcerers online when the game starts (or who join later) are marked by Kenjaku and
  **awakened**. **Rule 1:** they have **19 days** to declare at a colony.
* **Vessels** (the `vessel` trait) are counted as players straight away, like Yuji.
* **Entering a colony makes you a player** (rule 3). You are sent to one of the colony's **nine arrival points**,
  and a non-sorcerer gets their secondary awakening. Anyone who happened to be inside when the game started is
  first put outside, as the rule 3 note describes.
* **The barrier keeps players in** until someone adds rule 12. Each colony's edge is drawn as a curtain of smoke.
* **Kogane** is your interface: a gold status bar (points · colony · days before rule 8 hits) plus chat
  announcements. Type `/kogane` for a clickable menu.

### Rules

| # | Rule | How it works in the mod |
|---|------|-------------------------|
| 1–2 | Declare within 19 days or lose your technique | **Cursed technique removal kills you**. Players with no technique are spared, as in canon. |
| 3 | Non-players who enter become players | Automatic when you cross a barrier. |
| 4–5 | Points for ending lives | Sorcerer players: 5. Non-sorcerer players: 1. NPC players: 5 (Daido: 1). Curses inside a colony: 5, or 1 for Grade 4. Kills by your shikigami count for you. **Dying wipes your points** (configurable). |
| 6–7 | Spend 100 points to add a rule | `/kogane addrule <rule>`. The game master only accepts rules that are implemented (below). |
| 8 | Score unchanged for 19 days → removal | Same as rule 2. The timer resets whenever your score changes. |
| 9 | Player information *(Kashimo, day 2)* | Enables `/kogane players`. |
| 10 | Point transfer *(Higuruma)* | Enables `/kogane transfer <player> <amount>`. Beaten NPC players then **surrender their points instead of dying**. |
| 11 | Leave by substitute | `/kogane leave` costs 100 points and invites a substitute NPC into your colony. |
| 12 | Free movement across borders | The barriers stop holding players in. |
| 13 | No new players *(Kenjaku, day 18)* | Barriers reject non-players and no one new is marked. |
| 14 | End condition *(Kenjaku, day 24)* | **The endgame:** Kenjaku starts culling at the Lake Gosho Colony. |
| 15 | Authority passes *(Kenjaku's failsafe)* | Beating Kenjaku hands the authority to **Ryomen Sukuna**, who becomes the final boss. |

Players can buy rules 9–12, in any order, before the scripted characters add them. Rules are numbered in the
order they're added, as in canon.

### Who you meet

| Colony | Players |
|---|---|
| Tokyo No. 1 | **Hiromi Higuruma** (Deadly Sentencing: Judgeman's verdict either confiscates your technique and drains your energy, or hands him the Executioner's Sword; beaten, he throws the fight and adds rule 10), **Reggie Star** (Contract Repossession), **Iori Hazenoki** (explosions), Chizuru Hari, Remi, Hanyu, Haba, **Fumihiko Takaba** (Comedian: can't be hurt; right-click him for a joke), **Hana Kurusu, the Angel** (her Jacob's Ladder wipes out nearby curses) |
| Tokyo No. 2 | **Hajime Kashimo** (lightning; *Mythical Beast Amber* at half health; surrenders his 100 points), **Charles Bernard** (G-Warstaff foresight: dodges hits) |
| Sendai | **Ryu Ishigori** (Granite Blast), **Takako Uro** (Sky Manipulation), **Dhruv Lakdawalla** (shikigami swarm), **Kurourushi** (cockroach swarms) |
| Sakurajima | **Naoya Zenin**'s vengeful spirit (Projection Sorcery: dashes and freeze-frames), **Hagane Daido** (swordsman, not a sorcerer), **Rokujushi Miyo** (sumo: can't be hurt; right-click him once a day for a lesson that puts you in the Zone) |
| Lake Gosho | **Kenjaku** (endgame), then **Sukuna** (Dismantle, *Open* / Divine Flame, **Malevolent Shrine** at half health) |

Every colony also spawns generic **Awakened Players**, **Incarnated Sorcerers** and curses around players inside it.
At night, the curses Kenjaku released keep showing up across the world.

### Freeing Gojo

Bring the **Prison Realm** that Kenjaku dropped in Shibuya to **Hana Kurusu** in Tokyo No. 1 and right-click her.
Her Jacob's Ladder opens the back gate. **Satoru Gojo is released**, and Sukuna enters the endgame with 40% less health.

### Ending

* Defeat Sukuna: **the Great Merger is prevented**, and every player still in the game gets a big grade evaluation boost.
* Fail to by day 34 (`mergerDay`): **the Great Merger happens**. Bad ending, and the game ends.

---

## Commands

**Players** (no permissions): `/atlas`, `/kogane`, `/kogane rules`, `/kogane players`, `/kogane colonies`,
`/kogane addrule <rule>`, `/kogane transfer <player> <amount>`, `/kogane leave`. `/interview <n>` and
`/kenjaku accept|refuse` are what the clickable chat answers run.

**Operators:**

```
/jjk event shibuya [pos]                 drop the curtain (default: where you stand)
/jjk shibuya status | stop | skip        inspect, cancel, or skip to the next stage
/jjk cullinggame start [pos]             start the game now (anchor = Tokyo No. 1)
/jjk cullinggame stop | reset | status
/jjk cullinggame day <n>                 jump the game clock to day n (testing the scripted rules)
/jjk cullinggame endgame                 Kenjaku moves on Lake Gosho now
/jjk cullinggame rule <rule>             add a rule as the game master
/jjk cullinggame points <players> <n>    give or take points
/jjk cullinggame summon <profile>        spawn an NPC player, e.g. hajime_kashimo, kenjaku, sukuna
/jjk cullinggame curse <variant>         spawn a curse, e.g. jogo, mahito, naoya, grade_1
/jjk locate shibuya | colony <name>
/jjk landmark list | build <type> | rebuild <id> | remove <id>
/jjk cullinggame summon masamichi_yaga | yoshinobu_gakuganji | kenjaku_hideout | cursed_corpse
```

## Config — `serverconfig/cursed_domain-culling-server.toml`

Day length (default: one Minecraft day per Culling Game day), the 19-day deadlines, rule cost, colony radius and
spacing, the scripted rule days (Kashimo 2, no new players 18, endgame 24, Great Merger 34), NPC and curse density,
the curse surge, whether dying wipes points, whether removal also deletes your technique, the Shibuya settings
(auto-start day, curtain radius, abandon timer, whether Shibuya starts the Culling Game), and `landmarks.enabled` /
`landmarks.chunksPerTick` for the buildings. Set `autoStartDay = 0` to start everything only by command.

---

## Building

```
./gradlew build      # -> build/libs/cursed_domain-0.3.0.jar
./gradlew runClient
./gradlew runData    # regenerates lang/models into src/main/resources
```

### About this source tree

* Only the 0.1.0 **jar** was available, so the 0.1.0 code under `src/main/java` was **recovered with the Vineflower
  decompiler**. It reads like normal source, but decompiled code can need small fixes before it compiles with
  Gradle. Typical ones are redundant casts and generic inference around lambdas. If you still have your original
  0.1.0 sources, use them and copy in the new packages listed below.
* New code lives in its own packages and hooks into the mod without changing any existing class at runtime:
  * `com.curseddomain.cullinggame` — game state, rules, Kogane, commands, registries, plus a second `@Mod`
    entry point (`CullingGameMod`)
  * `com.curseddomain.cullinggame.npc` — the Culling Game players
  * `com.curseddomain.shibuya` — the incident and the new curse entity
  * `com.curseddomain.client.culling` — renderers (client `@Mod` entry point)
  * `com.curseddomain.landmark` — landmark placement, the blueprints and the chunk-by-chunk builder
  * `com.curseddomain.school` — the Jujutsu High interview and exam
  * `com.curseddomain.incarnation` — Kenjaku's offer and waking up in a villager
  * `com.curseddomain.datagen.lang.CullingLang` — all English text. `ModLanguageProvider` and
    `ModItemModelProvider` gained one hook each for datagen.
* The **only changes to existing 0.1.0 code** are the 4 → 5 slot edits in `AbilityState`, `AbilityManager`,
  `AbilityBarHud`, `AbilityInput`, `ModKeys` and `AbilityWheelScreen`, plus one lang line and the new key in
  `TechniqueLang`.
* Textures are generated by `tools/textures/gen_textures.py`.
* `dist/cursed_domain-0.3.0.jar` was built with `tools/sandbox-build/build.sh` because the build machine couldn't
  reach the NeoForge maven. That script compiles only the new classes, against API stubs generated from 0.1.0's
  own bytecode. In the jar, the five-slot change is applied to the original 0.1.0 classes by `SlotPatch.java`,
  which refuses to run unless it finds exactly the bytecode it expects. Every Minecraft/NeoForge call that 0.1.0
  didn't already make was checked by hand against the 1.21.1 API, and every class passes ASM's structural
  verifier (`Verify.java`). **The jar has not been launched in-game yet**, so please report anything odd from the
  log.
