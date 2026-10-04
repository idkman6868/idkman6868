# JoJo's Bizarre Adventure: Steel Ball Run — 0.1.0

NeoForge 21.1 / Minecraft 1.21.1 mod. *1890. Six thousand kilometres on horseback, from San Diego Beach to New York.*

0.1.0 is the first milestone of the [master prompt](../prompts/steel_ball_run_master_prompt.md): **the race and the
horses**. Spin, Stands, the Saint's Corpse and Funny Valentine come in later versions (see the roadmap below).

A jar is in [`dist/steel_ball_run-0.1.0.jar`](dist/steel_ball_run-0.1.0.jar). **Read "Building" below before relying
on it: it has not been launched in-game yet.**

---

## Entering the race

1. A **start line, grandstand and Race Office** are built 40 blocks east of world spawn when you first load the world.
   **Stephen Steel** stands outside the office.
2. Right-click Stephen Steel and click a **racer background** in chat:

   | Background | What you get |
   |---|---|
   | **Jockey** | A better horse, and it tires 15% slower under you. |
   | **Zeppeli Apprentice** | A horse that already trusts you (bond 40). The Spin arrives in 0.2.0. |
   | **Cowboy** | Two leads and extra dollars. |
   | **Native Runner** | No horse: you run the race, 35% faster on foot (it costs hunger instead of stamina). |
   | **Wanderer** | A random horse that could be a champion or a nag, and more money. |

   Everyone gets a **Race Number**, the **Map of America**, a **Horse Brush**, wheat and some **dollars**.
   Everyone but Native Runners gets a tamed horse and a saddle.
3. The race starts on its own on **world day 3** (`startDay`) once at least one player has registered, after a
   10-second countdown. Be at the start line for the gun.

## The race

* The course runs **east** from the start line through nine stages, about **12,000 blocks** in all (`routeScale`):
  San Diego Beach · Arizona Desert · Monument Valley · Rocky Mountains · Great Plains · Mississippi River ·
  Lake Michigan · Philadelphia · New York.
* Each stage ends at a **checkpoint gate** (two posts, bunting and lanterns) that is built as you approach.
  Ride within 14 blocks of the gate to pass it. Gates must be passed in order. Miss one and you're told to go back.
* **Stage points** by place: 100, 70, 50, 40, 30, 25, 20, 15, 10, 8, 6, 5, 4, 3, 2, 1 (`pointsTable`).
  The overall winner has the most points when the race ends.
* The **race panel** (top-left) shows the stage, the next checkpoint's distance with an arrow, your place on the road
  and overall, your points and the race clock. The **Map of America** lists every checkpoint with coordinates.
* The race ends when every rider is home. Once all registered players are done, the rivals still riding get
  `finishGraceSeconds` (3 minutes) to finish. Players who logged off mid-race don't hold it open once all the rivals
  are home. `/sbr withdraw` retires you.
* **Prizes:** the overall winner gets the **$50,000,000 Prize Cheque** and the **Steel Ball Run Trophy**. Every
  finisher gets dollars according to their overall place.

### Rivals

24 rivals ride against you: **Gyro Zeppeli** (on Valkyrie), **Johnny Joestar** (Slow Dancer), **Diego Brando**
(Silver Bullet), **Sandman** (on foot), **Pocoloco** (luckier pace swings), **Hot Pants**, **Mountain Tim**, and 17
other riders. Every rival always has a position on the course and keeps riding whether you see them or not.
When you come within 112 blocks of one, they appear on a horse and ride the course for real (at most 6 at a time).
When you leave, they go back to being simulated. They rest more at night. A rival who is killed retires from the race.

### Rules

Refused outright: **ender pearls**, **chorus fruit**, **Nether/End shortcuts**.
Stopped and penalised **25 points**: **gliding with elytra**, **boats**, **minecarts**.
Each rule is a config toggle. `/sbr rules` lists the ones in force.

## Horses

Any tamed horse you ride becomes a racehorse with hidden stats, rolled the first time it matters:
**breed** (Appaloosa, Quarter Horse, Thoroughbred, Mustang, Arabian, Morgan), **terrain affinity** (desert, plains,
mountains, snow), **max stamina** and **endurance** (recovery). `/sbr horse` shows them for the horse you're on.

* **Stamina** (bar above the hotbar): galloping drains it. Slower riding recovers it, and standing still recovers it
  fastest. On its favourite terrain a horse drains 25% less.
* At zero the horse is **exhausted**: 60% slower and recovering slowly until it is back to half. Pulling up to rest
  *before* that happens is faster overall.
* **Feeding** a tamed horse restores stamina and heals it: hay bale 45, apple 14, carrot 12, sugar 8, wheat 6.
  Golden carrots and golden apples still work as in vanilla, so breeding is untouched.
* **Bond** (0–100) grows by 1 for every 300 blocks ridden, by 1 for each of the first three feeds a day, and by 3
  for brushing once a day with the **Horse Brush**. Bond raises max stamina by up to 50% and cuts drain by up to 25%.

## Commands

Anyone: `/sbr standings`, `/sbr route`, `/sbr rules`, `/sbr horse`, `/sbr withdraw`, and `/sbr register <background>`
(this is what the chat buttons run; you must be near Stephen Steel or the start line).

Operators:
```
/sbr race status | start | start now | finish | reset
/sbr race gate <0-9>            teleport to a checkpoint gate
/sbr race gate <0-9> rebuild    build that gate again
/sbr steel                      put Stephen Steel where you stand
/sbr horse summon               a good tamed horse for testing
```

## Config

`serverconfig/steel_ball_run-server.toml`:
* **race:** autoStart, startDay, countdownSeconds, routeScale, startOffset, gateRadius, pointsTable,
  finishGraceSeconds, registrationRadius, buildGates
* **rivals:** count, speedMultiplier, nightFactor, maxInWorld, appearRange
* **rules:** banElytra, banEnderPearls, banChorusFruit, banBoats, banMinecarts, banOtherDimensions, penaltyPoints
* **horses:** starterHorses, baseStamina, gallopDrain, trotRegen, restRegen, exhaustedRegen, recoverFraction,
  exhaustedSlowdown, runnerSpeed

`config/steel_ball_run-client.toml`: raceHud, raceHudRight, horseHud.

## Canon liberties

* The manga restarts the field at every stage. Here the race is one continuous ride with points awarded at each gate.
* Stage lengths are scaled for a block game, and the named contenders' pace and horses are interpretations.
  The 17 riders who aren't named contenders are original characters.
* The terrain along the course is whatever the world generated. Landmarks and terrain theming for each stage come in
  0.4.0.
* Every name and line of text is in `SbrLang.java`, so they're easy to correct.

## Roadmap

| Version | Milestone |
|---|---|
| **0.1.0** | The race and horses *(this version)* |
| 0.2.0 | Spin: steel balls, Spin techniques, the Golden Rectangle and a Spin powered by your horse |
| 0.3.0 | The Saint's Corpse and Stands: the Devil's Palm, the Stand framework, Tusk Acts 1–3 |
| 0.4.0 | Valentine's assassins as puzzle bosses, stage landmarks and waystation towns |
| 0.5.0 | Funny Valentine: D4C, Love Train, Tusk Act 4, Ball Breaker, endings |

---

## Building

```
./gradlew build      # -> build/libs/steel_ball_run-0.1.0.jar
./gradlew runClient
./gradlew runData    # regenerates lang/en_us.json from SbrLang
```

**About `dist/steel_ball_run-0.1.0.jar`:** the machine this was written on could not reach the NeoForge or Mojang
servers, so Gradle couldn't run. The jar was built with `tools/sandbox-build.sh`, which:

1. compiles the mod against API **stubs** generated from the bytecode of the Cursed Domain jar in the parent folder,
   plus hand-written signatures in `tools/stub-spec-extra.txt`. The NeoForge ones were checked against the NeoForge
   1.21.1 sources. Brigadier (commands) and Netty are compiled against the real libraries.
2. runs `tools/LogicTests.java` (194 checks on the course geometry, points, standings, stamina and pacing).
3. writes `en_us.json` from `SbrLang` and checks every class with ASM's bytecode verifier.

Every Minecraft/NeoForge member the jar uses was listed from its bytecode and checked by hand against 1.21.1.
Still, stubs prove the code type-checks against the signatures written down, not that those match the game.
**The jar has never been launched.** Build it with Gradle, run it, and report anything odd from the log.
The most likely spots to need tuning in play are the rivals' steering (pathfinding over rough terrain) and the
stamina numbers.

Textures are generated by `tools/textures/gen_textures.py` (needs Pillow).

Fan-made, non-commercial. *JoJo's Bizarre Adventure* is © Hirohiko Araki / Shueisha. All assets are original or
procedurally generated.
