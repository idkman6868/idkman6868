package com.steelballrun.datagen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * All English text. Pure Java: {@link SbrLanguageProvider} feeds it to datagen, and {@link #main} writes en_us.json
 * directly for builds without the Minecraft toolchain. Edit names and lines here.
 */
public final class SbrLang {
   private SbrLang() {
   }

   public static void addAll(BiConsumer<String, String> add) {
      // creative tab, items, entities
      add.accept("itemGroup.steel_ball_run", "Steel Ball Run");
      add.accept("item.steel_ball_run.map_of_america", "Map of America");
      add.accept("item.steel_ball_run.map_of_america.tooltip", "Use to see the course and every checkpoint");
      add.accept("item.steel_ball_run.horse_brush", "Horse Brush");
      add.accept("item.steel_ball_run.horse_brush.tooltip", "Use on your horse once a day to build your bond");
      add.accept("item.steel_ball_run.race_number", "Race Number");
      add.accept("item.steel_ball_run.race_number.tooltip", "Official Steel Ball Run entrant, 1890");
      add.accept("item.steel_ball_run.dollar", "Dollar");
      add.accept("item.steel_ball_run.dollar.tooltip", "Spend it at trading posts along the route (coming soon)");
      add.accept("item.steel_ball_run.prize_cheque", "Prize Cheque");
      add.accept("item.steel_ball_run.prize_cheque.tooltip", "Pay to the bearer: fifty million dollars. Signed, Stephen Steel");
      add.accept("item.steel_ball_run.trophy", "Steel Ball Run Trophy");
      add.accept("item.steel_ball_run.trophy.tooltip", "Winner of the Steel Ball Run, San Diego to New York");
      add.accept("entity.steel_ball_run.stephen_steel", "Stephen Steel");
      add.accept("entity.steel_ball_run.rival_rider", "Rival Rider");

      // stages
      add.accept("stage.steel_ball_run.san_diego_beach", "San Diego Beach");
      add.accept("stage.steel_ball_run.arizona_desert", "Arizona Desert");
      add.accept("stage.steel_ball_run.monument_valley", "Monument Valley");
      add.accept("stage.steel_ball_run.rocky_mountains", "Rocky Mountains");
      add.accept("stage.steel_ball_run.great_plains", "Great Plains");
      add.accept("stage.steel_ball_run.mississippi_river", "Mississippi River");
      add.accept("stage.steel_ball_run.lake_michigan", "Lake Michigan");
      add.accept("stage.steel_ball_run.philadelphia", "Philadelphia");
      add.accept("stage.steel_ball_run.new_york", "New York");
      add.accept("gate.steel_ball_run.start", "Start line");

      // backgrounds
      add.accept("background.steel_ball_run.jockey", "Jockey");
      add.accept("background.steel_ball_run.jockey.desc", "A better horse, and it tires 15% slower under you.");
      add.accept("background.steel_ball_run.zeppeli_apprentice", "Zeppeli Apprentice");
      add.accept("background.steel_ball_run.zeppeli_apprentice.desc", "A horse that already trusts you. (The Spin comes in a later version.)");
      add.accept("background.steel_ball_run.cowboy", "Cowboy");
      add.accept("background.steel_ball_run.cowboy.desc", "Leads and extra dollars. Any horse on the prairie can be yours.");
      add.accept("background.steel_ball_run.native_runner", "Native Runner");
      add.accept("background.steel_ball_run.native_runner.desc", "No horse. You run the whole way, much faster than anyone on foot.");
      add.accept("background.steel_ball_run.wanderer", "Wanderer");
      add.accept("background.steel_ball_run.wanderer.desc", "A horse that might be a champion or a nag, and more money.");

      // Stephen Steel
      add.accept("steel.steel_ball_run.says", "[Stephen Steel] %s");
      add.accept("steel.steel_ball_run.welcome",
         "Welcome, welcome! This is the Steel Ball Run: six thousand kilometres on horseback, from this beach to New York City. Fifty million dollars to the winner!");
      add.accept("steel.steel_ball_run.choose", "Tell me what kind of rider you are, and I'll put your name down:");
      add.accept("steel.steel_ball_run.accepted", "Splendid! You are rider number %s, entered as a %s. See you at the starting gun!");
      add.accept("steel.steel_ball_run.registered", "You're rider number %s. The race starts on day %s. Look after that horse!");
      add.accept("steel.steel_ball_run.ride", "What are you doing talking to me? Ride! New York is that way!");
      add.accept("steel.steel_ball_run.closed", "I'm sorry, registration has closed. You can still follow the race!");
      add.accept("steel.steel_ball_run.over", "What a race! Here are the final standings:");

      // race messages
      add.accept("message.steel_ball_run.registration_closed", "Registration for the Steel Ball Run has closed.");
      add.accept("message.steel_ball_run.already_registered", "You are already registered.");
      add.accept("message.steel_ball_run.too_far_to_register", "Register with Stephen Steel at the start line on San Diego Beach.");
      add.accept("message.steel_ball_run.unknown_background", "Unknown background: %s");
      add.accept("message.steel_ball_run.after_register",
         "The race starts on world day %s. Saddle your horse, use the Map of America to see the course, and keep your horse fed and rested.");
      add.accept("message.steel_ball_run.countdown_start", "The Steel Ball Run is about to begin! Riders to the start line at %s, %s!");
      add.accept("message.steel_ball_run.started", "And they're off! The Steel Ball Run has begun!");
      add.accept("message.steel_ball_run.crossed_start", "You crossed the start line. Ride east!");
      add.accept("message.steel_ball_run.missed_gate", "You missed checkpoint %s! Ride back through it.");
      add.accept("message.steel_ball_run.stage_winner", "%s wins stage %s: %s!");
      add.accept("message.steel_ball_run.race_leader_finished", "%s is the first to cross the finish line in New York!");
      add.accept("message.steel_ball_run.player_stage", "%s finishes %s on stage %s: %s.");
      add.accept("message.steel_ball_run.player_finished", "%s crosses the finish line in %s!");
      add.accept("message.steel_ball_run.closing", "Every registered rider is home. The race closes in %s seconds.");
      add.accept("message.steel_ball_run.race_over", "The Steel Ball Run is over!");
      add.accept("message.steel_ball_run.podium", "%s: %s with %s points");
      add.accept("message.steel_ball_run.your_result", "You finished %s overall with %s points.");
      add.accept("message.steel_ball_run.standings_header", "Steel Ball Run: overall standings");
      add.accept("message.steel_ball_run.route_header", "The course: about %s km from San Diego to New York");
      add.accept("message.steel_ball_run.route_line", "%s. %s at %s, %s (%s blocks away)");
      add.accept("message.steel_ball_run.rival_retired", "%s has retired from the race.");
      add.accept("message.steel_ball_run.status", "Race: %s. Start line %s, %s; course %s blocks; %s entrants (%s players).");
      add.accept("message.steel_ball_run.cannot_start", "The race can't be started now (%s).");
      add.accept("message.steel_ball_run.not_running", "The race isn't running.");
      add.accept("message.steel_ball_run.reset", "The race was reset. Registration is open again.");
      add.accept("message.steel_ball_run.not_racing", "You aren't riding in the race.");
      add.accept("message.steel_ball_run.withdrawn", "You have withdrawn from the Steel Ball Run.");
      add.accept("message.steel_ball_run.not_riding", "You aren't riding a horse.");
      add.accept("message.steel_ball_run.horse_exhausted", "Your horse is exhausted! Let it rest.");
      add.accept("message.steel_ball_run.horse_recovered", "Your horse has its wind back.");
      add.accept("message.steel_ball_run.already_brushed", "You've already brushed this horse today.");
      add.accept("message.steel_ball_run.horse_care", "Stamina %s / %s  ·  Bond %s");
      add.accept("message.steel_ball_run.horse_info", "%s (rides best in the %s). Stamina %s / %s, bond %s, top speed %s blocks/s, endurance %s");

      add.accept("title.steel_ball_run.countdown", "Steel Ball Run");
      add.accept("title.steel_ball_run.go", "GO!");
      add.accept("title.steel_ball_run.go_sub", "Six thousand kilometres to New York");
      add.accept("title.steel_ball_run.stage_done", "Stage %s complete: %s");
      add.accept("title.steel_ball_run.finished", "FINISH!");
      add.accept("title.steel_ball_run.place_points", "%s place  ·  +%s points");
      add.accept("title.steel_ball_run.winner", "Champion!");
      add.accept("title.steel_ball_run.winner_sub", "You won the Steel Ball Run");

      add.accept("standings.steel_ball_run.line", "%s. #%s %s: %s pts (%s)");
      add.accept("standings.steel_ball_run.retired", "retired");
      add.accept("standings.steel_ball_run.finished", "finished");
      add.accept("standings.steel_ball_run.at_start", "at the start");

      // rules
      add.accept("rule.steel_ball_run.header", "Steel Ball Run rules:");
      add.accept("rule.steel_ball_run.penalty", "Race official: %s is against the rules! -%s points.");
      add.accept("rule.steel_ball_run.refused", "%s is against the race rules.");
      add.accept("rule.steel_ball_run.checkpoints", "Skipping a checkpoint");
      add.accept("rule.steel_ball_run.checkpoints.desc", "Ride through every checkpoint gate, in order.");
      add.accept("rule.steel_ball_run.elytra", "Gliding");
      add.accept("rule.steel_ball_run.elytra.desc", "No elytra.");
      add.accept("rule.steel_ball_run.ender_pearl", "Throwing ender pearls");
      add.accept("rule.steel_ball_run.ender_pearl.desc", "No ender pearls.");
      add.accept("rule.steel_ball_run.chorus_fruit", "Teleporting with chorus fruit");
      add.accept("rule.steel_ball_run.chorus_fruit.desc", "No chorus fruit teleports.");
      add.accept("rule.steel_ball_run.boat", "Taking a boat");
      add.accept("rule.steel_ball_run.boat.desc", "No boats. Swim your horse across.");
      add.accept("rule.steel_ball_run.minecart", "Riding a minecart");
      add.accept("rule.steel_ball_run.minecart.desc", "No minecarts.");
      add.accept("rule.steel_ball_run.dimension", "Cutting through another dimension");
      add.accept("rule.steel_ball_run.dimension.desc", "No Nether or End shortcuts.");

      // horses
      add.accept("affinity.steel_ball_run.desert", "desert");
      add.accept("affinity.steel_ball_run.plains", "plains");
      add.accept("affinity.steel_ball_run.mountain", "mountains");
      add.accept("affinity.steel_ball_run.snow", "snow");
      add.accept("breed.steel_ball_run.appaloosa", "Appaloosa");
      add.accept("breed.steel_ball_run.quarter_horse", "Quarter Horse");
      add.accept("breed.steel_ball_run.thoroughbred", "Thoroughbred");
      add.accept("breed.steel_ball_run.mustang", "Mustang");
      add.accept("breed.steel_ball_run.arabian", "Arabian");
      add.accept("breed.steel_ball_run.morgan", "Morgan");

      // HUD
      add.accept("hud.steel_ball_run.title", "Steel Ball Run");
      add.accept("hud.steel_ball_run.registration", "Registration open: the race starts on day %s");
      add.accept("hud.steel_ball_run.unregistered", "Talk to Stephen Steel at the start line to enter");
      add.accept("hud.steel_ball_run.countdown", "Starting in %s...");
      add.accept("hud.steel_ball_run.to_start", "Ride to the start line");
      add.accept("hud.steel_ball_run.stage", "Stage %s: %s");
      add.accept("hud.steel_ball_run.next", "Checkpoint %s: %s blocks %s");
      add.accept("hud.steel_ball_run.place", "On the road: %s / %s");
      add.accept("hud.steel_ball_run.overall", "Overall: %s  ·  %s pts");
      add.accept("hud.steel_ball_run.time", "Race time %s");
      add.accept("hud.steel_ball_run.finished", "Finished %s  ·  %s pts");
      add.accept("hud.steel_ball_run.spectating", "Race in progress (%s riders)");
      add.accept("hud.steel_ball_run.over", "The race is over. /sbr standings");
      add.accept("hud.steel_ball_run.stamina", "Stamina");
      add.accept("hud.steel_ball_run.exhausted", "EXHAUSTED");
      add.accept("hud.steel_ball_run.bond", "Bond %s");
   }

   /** Writes en_us.json (sorted as added) to the path given, or prints it. */
   public static void main(String[] args) throws Exception {
      Map<String, String> map = new LinkedHashMap<>();
      addAll(map::put);
      StringBuilder sb = new StringBuilder("{\n");
      int i = 0;
      for (Map.Entry<String, String> e : map.entrySet()) {
         sb.append("  \"").append(escape(e.getKey())).append("\": \"").append(escape(e.getValue())).append('"');
         sb.append(++i < map.size() ? ",\n" : "\n");
      }
      sb.append("}\n");
      if (args.length > 0) {
         java.nio.file.Path out = java.nio.file.Path.of(args[0]);
         java.nio.file.Files.createDirectories(out.getParent());
         java.nio.file.Files.writeString(out, sb.toString());
      } else {
         System.out.print(sb);
      }
   }

   private static String escape(String s) {
      StringBuilder sb = new StringBuilder();
      for (char c : s.toCharArray()) {
         switch (c) {
            case '"' -> sb.append("\\\"");
            case '\\' -> sb.append("\\\\");
            case '\n' -> sb.append("\\n");
            default -> {
               if (c < 0x20 || c > 0x7e) {
                  sb.append(String.format("\\u%04x", (int)c));
               } else {
                  sb.append(c);
               }
            }
         }
      }
      return sb.toString();
   }
}
