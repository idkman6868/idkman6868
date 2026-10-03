# Jujutsu Kaisen: Cursed Domain — 0.2.0: Shibuya Incident & Culling Game

NeoForge 21.1 / Minecraft 1.21.1 mod. This update adds the **Shibuya Incident** and Kenjaku's **Culling Game** on top of
the 0.1.0 story (awakening → recruitment letter → Jujutsu High → grades).

A ready-to-use jar is in [`dist/cursed_domain-0.2.0.jar`](dist/cursed_domain-0.2.0.jar). It replaces
`cursed_domain-0.1.0.jar` in your `mods` folder; existing worlds keep working.

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

**Players** (no permissions): `/kogane`, `/kogane rules`, `/kogane players`, `/kogane colonies`,
`/kogane addrule <rule>`, `/kogane transfer <player> <amount>`, `/kogane leave`.

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
```

## Config — `serverconfig/cursed_domain-culling-server.toml`

Day length (default: one Minecraft day per Culling Game day), the 19-day deadlines, rule cost, colony radius and
spacing, the scripted rule days (Kashimo 2, no new players 18, endgame 24, Great Merger 34), NPC and curse density,
the curse surge, whether dying wipes points, whether removal also deletes your technique, and the Shibuya settings
(auto-start day, curtain radius, abandon timer, whether Shibuya starts the Culling Game). Set `autoStartDay = 0` to
start everything only by command.

---

## Building

```
./gradlew build      # -> build/libs/cursed_domain-0.2.0.jar
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
  * `com.curseddomain.datagen.lang.CullingLang` — all English text. `ModLanguageProvider` and
    `ModItemModelProvider` gained one hook each for datagen.
* Textures are generated by `tools/textures/gen_textures.py`.
* `dist/cursed_domain-0.2.0.jar` was built with `tools/sandbox-build/build.sh` because the build machine couldn't
  reach the NeoForge maven. That script compiles only the new classes, against API stubs generated from 0.1.0's
  own bytecode. Every Minecraft/NeoForge call that 0.1.0 didn't already make was checked by hand against the
  1.21.1 API. **The jar has not been launched in-game yet**, so please report anything odd from the log.
