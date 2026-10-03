package com.curseddomain.datagen.lang;

import net.neoforged.neoforge.common.data.LanguageProvider;

/** English text for the Shibuya Incident and the Culling Game. */
public final class CullingLang {
   private CullingLang() {
   }

   public static void addTo(LanguageProvider lang) {
      items(lang);
      entities(lang);
      rules(lang);
      colonies(lang);
      kogane(lang);
      game(lang);
      npcs(lang);
      shibuya(lang);
      commands(lang);
   }

   private static void items(LanguageProvider lang) {
      lang.add("item.cursed_domain.prison_realm", "Prison Realm");
      lang.add("item.cursed_domain.prison_realm.desc", "A cube of eyes, sealed shut. Something enormous waits inside.");
      lang.add("item.cursed_domain.prison_realm.hint", "Only a technique that extinguishes other techniques could open it.");
   }

   private static void entities(LanguageProvider lang) {
      String npc = "entity.cursed_domain.sorcerer_npc";
      lang.add(npc, "Culling Game Player");
      lang.add(npc + ".hiromi_higuruma", "Hiromi Higuruma");
      lang.add(npc + ".reggie_star", "Reggie Star");
      lang.add(npc + ".iori_hazenoki", "Iori Hazenoki");
      lang.add(npc + ".chizuru_hari", "Chizuru Hari");
      lang.add(npc + ".remi", "Remi");
      lang.add(npc + ".hanyu", "Hanyu");
      lang.add(npc + ".haba", "Haba");
      lang.add(npc + ".fumihiko_takaba", "Fumihiko Takaba");
      lang.add(npc + ".hana_kurusu", "Hana Kurusu (Angel)");
      lang.add(npc + ".hajime_kashimo", "Hajime Kashimo");
      lang.add(npc + ".charles_bernard", "Charles Bernard");
      lang.add(npc + ".ryu_ishigori", "Ryu Ishigori");
      lang.add(npc + ".takako_uro", "Takako Uro");
      lang.add(npc + ".dhruv_lakdawalla", "Dhruv Lakdawalla");
      lang.add(npc + ".hagane_daido", "Hagane Daido");
      lang.add(npc + ".rokujushi_miyo", "Rokujushi Miyo");
      lang.add(npc + ".kenjaku", "Kenjaku");
      lang.add(npc + ".sukuna", "Ryomen Sukuna");
      lang.add(npc + ".awakened_player", "Awakened Player");
      lang.add(npc + ".incarnated_sorcerer", "Incarnated Sorcerer");
      String curse = "entity.cursed_domain.rampant_curse";
      lang.add(curse, "Cursed Spirit");
      lang.add(curse + ".grade_3", "Grade 3 Curse");
      lang.add(curse + ".grade_2", "Grade 2 Curse");
      lang.add(curse + ".grade_1", "Grade 1 Curse");
      lang.add(curse + ".jogo", "Jogo");
      lang.add(curse + ".hanami", "Hanami");
      lang.add(curse + ".dagon", "Dagon");
      lang.add(curse + ".mahito", "Mahito");
      lang.add(curse + ".kurourushi", "Kurourushi");
      lang.add(curse + ".naoya", "Naoya Zenin (Vengeful Spirit)");
   }

   private static void rules(LanguageProvider lang) {
      String r = "cullinggame.cursed_domain.rule.";
      lang.add(r + "declare", "Once a player has awakened their cursed technique, they must declare their participation in the Culling Game at a colony of their choice within 19 days.");
      lang.add(r + "technique_removal", "Any player who breaks the previous rule will be subject to cursed technique removal.");
      lang.add(r + "non_players_entering", "Non-players who enter a colony become players at the moment of entry and are considered to have declared participation.");
      lang.add(r + "scoring", "Players score points by ending the lives of other players.");
      lang.add(r + "point_values", "The point value of a life is decided by the game master. As a general rule, sorcerers are worth 5 points and non-sorcerers 1 point.");
      lang.add(r + "adding_rules", "Excluding the value of their own life, a player can spend 100 points to negotiate with the game master and add a new rule.");
      lang.add(r + "game_master", "In accordance with rule 6, the game master must accept any new rule as long as it does not have a long-lasting effect on the game.");
      lang.add(r + "stagnation", "If a player's score remains the same for 19 days, they will be subject to cursed technique removal.");
      lang.add(r + "player_info", "Players may view information on other players: their name, points, number of rules added and current colony.");
      lang.add(r + "point_transfer", "Players may transfer any number of points to one another.");
      lang.add(r + "leave_by_substitute", "A player may leave the game by spending 100 points to invite a substitute into their colony.");
      lang.add(r + "free_borders", "Players may enter and exit across all colony borders.");
      lang.add(r + "no_new_players", "From this point on, no new players may join the Culling Game.");
      lang.add(r + "end_condition", "The Culling Game will end once every player other than the game's chosen few is dead.");
      lang.add(r + "merger_authority", "Authority to activate the Great Merger passes to Ryomen Sukuna.");
   }

   private static void colonies(LanguageProvider lang) {
      String c = "colony.cursed_domain.";
      lang.add(c + "tokyo_1", "Tokyo No. 1 Colony");
      lang.add(c + "tokyo_2", "Tokyo No. 2 Colony");
      lang.add(c + "sendai", "Sendai Colony");
      lang.add(c + "sakurajima", "Sakurajima Colony");
      lang.add(c + "lake_gosho", "Lake Gosho Colony");
      lang.add(c + "colony_6", "Unnamed Colony (No. 6)");
      lang.add(c + "colony_7", "Unnamed Colony (No. 7)");
      lang.add(c + "colony_8", "Unnamed Colony (No. 8)");
      lang.add(c + "colony_9", "Unnamed Colony (No. 9)");
      lang.add(c + "colony_10", "Unnamed Colony (No. 10)");
   }

   private static void kogane(LanguageProvider lang) {
      String k = "cullinggame.cursed_domain.kogane.";
      lang.add("cullinggame.cursed_domain.kogane.name", "[Kogane]");
      lang.add(k + "vessel", "Hellooo! You're already being counted as a player. Funny, isn't it? I'm your personal liaison to the Culling Game!");
      lang.add(k + "marked", "You've been marked! You have %s days to declare your participation at a colony. Just walk inside one. Easy!");
      lang.add(k + "points", "%1$s has gained %2$s points for %3$s! Total: %4$s points.");
      lang.add(k + "transfer_received", "%1$s points received from %2$s! Total: %3$s points.");
      lang.add(k + "transfer_sent", "%1$s points sent to %2$s. You have %3$s left.");
      lang.add(k + "negotiated", "The game master has heard your proposal...");
      lang.add(k + "rule_added", "A rule has been added! Rule %1$s, by %2$s: %3$s");
      lang.add(k + "left", "Thank you for playing! A substitute has been invited in your place.");
      lang.add(k + "removal_spared", "Your deadline has passed, but you have no cursed technique to remove. Lucky you!");
      lang.add(k + "removal.rule2", "You did not declare your participation in time. Rule 2: cursed technique removal!");
      lang.add(k + "removal.rule8", "Your score has not changed for too long. Rule 8: cursed technique removal!");
      lang.add(k + "warn_declare", "Reminder! %s day(s) left to declare your participation at a colony.");
      lang.add(k + "warn_stagnation", "Reminder! Your score must change within %s day(s).");
      lang.add(k + "joined", "Welcome to the %1$s! You are now a player. You were sent to arrival point %2$s of 9.");
      lang.add(k + "civilian_exit", "You were inside the %s when the game began, so you've been put outside. Walk back in and you're a player!");
      lang.add(k + "secondary_awakening", "Entering the barrier has finished awakening you. Good luck out there!");
      lang.add(k + "points_lost", "You died holding %s points. They're gone now!");
      lang.add(k + "welcome_back", "Welcome back, player! You are holding %s points.");
      lang.add(k + "endgame_location", "Kenjaku is culling the players of the %1$s, around %2$s, %3$s.");
      lang.add(k + "not_running", "The Culling Game hasn't started.");
      lang.add(k + "over", "The Culling Game is over.");
      lang.add(k + "status.day", "Day %1$s of the Culling Game. %2$s rules in force.");
      lang.add(k + "status.none", "You are not a player. Walk into a colony to become one!");
      lang.add(k + "status.player", "You have %1$s points. Location: %2$s. Rules you added: %3$s.");
      lang.add(k + "status.marked", "You are marked and must declare at a colony before your deadline.");
      lang.add(k + "rules.header", "The rules of the Culling Game:");
      lang.add(k + "no_info_rule", "That information isn't available to players. Not unless somebody adds a rule!");
      lang.add(k + "players.header", "Players (name / points / rules added / location):");
      lang.add(k + "colonies.header", "The colonies:");
      lang.add(k + "rule_unknown", "The game master does not know that rule.");
      lang.add(k + "not_player", "Only players can do that.");
      lang.add(k + "rule_refused", "The game master refuses: that rule would have a long-lasting effect on the game.");
      lang.add(k + "rule_exists", "That rule is already in force.");
      lang.add(k + "not_enough", "Not enough points! It costs %s.");
      lang.add(k + "no_transfer_rule", "Players can't transfer points. There's no rule for it!");
      lang.add(k + "bad_target", "That person isn't a player you can transfer points to.");
      lang.add(k + "no_leave_rule", "Players can't just leave the game. There's no rule for it!");
   }

   private static void game(LanguageProvider lang) {
      String g = "cullinggame.cursed_domain.";
      lang.add(g + "start.1", "Can you hear me? I have unsealed every cursed object I left inside you, and finished adjusting the brains of the rest.");
      lang.add(g + "start.2", "Ten colonies now stand across this land. Those I have marked have nineteen days to declare at one of them, or lose their technique. And their lives with it.");
      lang.add(g + "start.3", "Players earn points by ending the lives of other players. Enough points buy a new rule from the game master.");
      lang.add(g + "start.4", "I once tried to make something greater with my own hands. It never surpassed me. So: chaos. Show me the golden age of jujutsu.");
      lang.add(g + "start.title", "The Culling Game");
      lang.add(g + "start.subtitle", "Ten colonies. Hundreds of sorcerers. No way out.");
      lang.add(g + "mark", "You're one of mine. Wake up.");
      lang.add(g + "rule_title", "Rule %s");
      lang.add(g + "removal.title", "Cursed Technique Removal");
      lang.add(g + "removal.subtitle", "The game master has ruled.");
      lang.add(g + "gojo.title", "The Prison Realm is open");
      lang.add(g + "gojo.subtitle", "Satoru Gojo has been released.");
      lang.add(g + "endgame", "Enough waiting. I'll add the ending myself, and see who is still standing at Lake Gosho.");
      lang.add(g + "failsafe", "Kenjaku's failsafe");
      lang.add(g + "failsafe.line", "Did you think I wouldn't plan for this? The authority goes to him. Enjoy the rest of the show.");
      lang.add(g + "sukuna.title", "Ryomen Sukuna");
      lang.add(g + "sukuna.subtitle", "The King of Curses holds the authority to end the game.");
      lang.add(g + "end.averted.title", "The Culling Game is over");
      lang.add(g + "end.averted.subtitle", "The Great Merger has been prevented.");
      lang.add(g + "end.averted", "With Sukuna's defeat the Culling Game lost its purpose. The barriers fall silent, and Kogane goes quiet for the last time.");
      lang.add(g + "end.merger.title", "The Great Merger");
      lang.add(g + "end.merger.subtitle", "Everyone is there, and not there.");
      lang.add(g + "end.merger", "No one stopped the ritual. For a moment the boundary between every person in the land disappears. The game ends, and something has changed.");
      lang.add(g + "bar.player", "Kogane  |  %1$s pts  |  %2$s  |  score must change in %3$s days");
      lang.add(g + "bar.outside", "outside the colonies");
      lang.add(g + "bar.marked", "Kogane  |  Declare at a colony within %s days");
      lang.add(g + "barrier.closed", "The barrier rejects you. No new players may join.");
      lang.add(g + "barrier.sealed", "The barrier will not let you leave.");
      lang.add(g + "joined.subtitle", "You are now a player of the Culling Game");
      lang.add(g + "rules.author", "  (added by %s)");
      lang.add(g + "players.entry", " %1$s / %2$s pts / %3$s rules / %4$s");
      lang.add(g + "colonies.entry", " %1$s: %2$s, %3$s (%4$s blocks away)");
      lang.add(g + "menu.rules", "Read the rules in force");
      lang.add(g + "menu.players", "List players (needs rule 9)");
      lang.add(g + "menu.colonies", "Where are the colonies?");
      lang.add(g + "menu.addrule", "Spend 100 points to add a rule");
      lang.add(g + "menu.transfer", "Send points to a player (needs the transfer rule)");
      lang.add(g + "menu.leave", "Spend 100 points to leave (needs the substitute rule)");
   }

   private static void npcs(LanguageProvider lang) {
      String n = "npc.cursed_domain.";
      lang.add(n + "generic.yield", "Okay, okay! Take my points, just let me go!");
      lang.add(n + "hiromi_higuruma.domain", "Domain Expansion: Deadly Sentencing");
      lang.add(n + "hiromi_higuruma.domain.sub", "Judgeman will hear the case.");
      lang.add(n + "hiromi_higuruma.confiscation", "Judgeman finds you guilty. Your cursed technique is confiscated.");
      lang.add(n + "hiromi_higuruma.death_penalty", "Guilty. The death penalty. The Executioner's Sword is in my hands.");
      lang.add(n + "hiromi_higuruma.not_guilty", "...Not guilty. Huh.");
      lang.add(n + "hiromi_higuruma.yield", "Enough. I knew who the real culprit was. I'll spend my points on your rule: points can be transferred.");
      lang.add(n + "hiromi_higuruma.yield_transfer", "Enough. I'm out. Take what I have left.");
      lang.add(n + "reggie_star.receipt", "Contract Repossession! Everything I ever paid for comes back.");
      lang.add(n + "reggie_star.yield", "You win. A deal's a deal; take the points.");
      lang.add(n + "iori_hazenoki.yield", "Fine! Have them, just stop haggling!");
      lang.add(n + "remi.yield", "Waaah, okay, I give up! Take them!");
      lang.add(n + "hanyu.yield", "Don't kill me! The points are yours!");
      lang.add(n + "haba.yield", "Alright, alright. I'm done.");
      lang.add(n + "hana_kurusu.yield", "Stop this. If it is points you want, take them.");
      lang.add(n + "hana_kurusu.ladder", "Jacob's Ladder. Be extinguished.");
      lang.add(n + "hana_kurusu.greet", "I am the Angel. My technique extinguishes all techniques. If you bring me the Prison Realm, I can open it.");
      lang.add(n + "hana_kurusu.vessel", "You carry the Fallen One. I will not help him... but I will help you, if you bring me the Prison Realm.");
      lang.add(n + "hana_kurusu.unseal", "Jacob's Ladder! The seal on the back gate is broken. He is free.");
      lang.add(n + "hana_kurusu.already", "The Prison Realm is already open.");
      lang.add(n + "hajime_kashimo.amber", "Mythical Beast Amber! I'll burn out with you, then!");
      lang.add(n + "hajime_kashimo.yield", "Ha! That was fun. My hundred points are yours. Now, where's Sukuna?");
      lang.add(n + "charles_bernard.dodge", "I saw that coming. G-Warstaff shows me the future!");
      lang.add(n + "charles_bernard.yield", "Fine... this chapter's over. Take my points.");
      lang.add(n + "ryu_ishigori.granite_blast", "Granite Blast!");
      lang.add(n + "ryu_ishigori.yield", "What a dessert! I'm stuffed. The points are yours.");
      lang.add(n + "takako_uro.yield", "Hmph. You're stronger than you look. Take them.");
      lang.add(n + "hagane_daido.yield", "Your blade is honest. I yield.");
      lang.add(n + "fumihiko_takaba.immune.1", "Ow! ...Just kidding, I'm fine. Comedians don't die mid-bit!");
      lang.add(n + "fumihiko_takaba.immune.2", "Is this a tsukkomi? Your timing needs work!");
      lang.add(n + "fumihiko_takaba.immune.3", "That would've hurt if it were funny!");
      lang.add(n + "fumihiko_takaba.joke.1", "Why did the curse cross the barrier? It couldn't! Rule 12 isn't in yet!");
      lang.add(n + "fumihiko_takaba.joke.2", "I joined the Culling Game for the exposure. Turns out it's mostly cursed energy exposure!");
      lang.add(n + "fumihiko_takaba.joke.3", "Five points for a sorcerer, one for a comedian? Even the game master's a critic!");
      lang.add(n + "fumihiko_takaba.joke.4", "My technique? It's called Comedian. Yes, that's the whole bit.");
      lang.add(n + "fumihiko_takaba.joke.5", "If you laughed, you lost. If you didn't laugh, I lost. Either way, nobody dies!");
      lang.add(n + "rokujushi_miyo.immune.1", "Haha! A good push, but sumo is about the feet!");
      lang.add(n + "rokujushi_miyo.immune.2", "Don't fight me, wrestle me!");
      lang.add(n + "rokujushi_miyo.immune.3", "Lower your hips!");
      lang.add(n + "rokujushi_miyo.lesson", "Let go of what holds you back. Feel everything at once... There! That's the zone.");
      lang.add(n + "rokujushi_miyo.again", "One lesson a day. Go use what you learned!");
      lang.add(n + "kenjaku.summon", "Cursed Spirit Manipulation.");
      lang.add(n + "kenjaku.uzumaki", "Maximum: Uzumaki.");
      lang.add(n + "kenjaku.antigravity", "Antigravity System.");
      lang.add(n + "sukuna.open", "Open.");
      lang.add(n + "sukuna.shrine", "Domain Expansion: Malevolent Shrine.");
      lang.add(n + "jogo.meteor", "Maximum: Meteor!");
      lang.add(n + "jogo.domain", "Domain Expansion: Coffin of the Iron Mountain!");
      lang.add(n + "hanami.roots", "You will return to the earth.");
      lang.add(n + "mahito.transfigure", "Let's play! Idle Transfiguration.");
      lang.add(n + "mahito.domain", "Domain Expansion: Self-Embodiment of Perfection.");
      lang.add(n + "kurourushi.swarm", "*the swarm pours out*");
      lang.add(n + "naoya.projection", "Too slow. Twenty-four frames a second.");
   }

   private static void shibuya(LanguageProvider lang) {
      String s = "shibuya.cursed_domain.";
      lang.add(s + "narrator", "[Shibuya]");
      lang.add(s + "title", "October 31st, 19:00");
      lang.add(s + "subtitle", "A curtain has fallen over Shibuya.");
      lang.add(s + "begin.1", "A curtain descends over the station district. Non-sorcerers inside can no longer get out.");
      lang.add(s + "begin.2", "From inside, a demand: bring Satoru Gojo here.");
      lang.add(s + "location", "The curtain stands around %s, %s.");
      lang.add(s + "curtain.sealed", "The curtain keeps non-sorcerers in.");
      lang.add(s + "active", "Sorcerers have entered the curtain. The curses inside start moving.");
      lang.add(s + "wave", "Curses pour into the streets (wave %s of 3)");
      lang.add(s + "disasters", "Special grade cursed spirits walk out of the crowd: Jogo, Hanami and Dagon.");
      lang.add(s + "mahito", "Mahito comes out to play. Bodies twist where he touches them.");
      lang.add(s + "prison_realm.title", "Prison Realm: Gate Open");
      lang.add(s + "prison_realm.subtitle", "Satoru Gojo has been sealed.");
      lang.add(s + "prison_realm", "Deep under the station, the strongest sorcerer is sealed away. And the one who planned it is here.");
      lang.add(s + "kenjaku.arrive", "It's been a while. With Gojo sealed, nothing stands between me and what comes next.");
      lang.add(s + "kenjaku.absorb", "You've done enough, Mahito. Your technique is mine now.");
      lang.add(s + "kenjaku.escape", "This was fun, but I have somewhere to be. Keep the box; you won't be able to open it. Here, a parting gift.");
      lang.add(s + "abandoned", "No one came to stop it. Gojo is sealed, and the one in Suguru Geto's body walks out of Shibuya untouched.");
      lang.add(s + "end.title", "The Shibuya Incident is over");
      lang.add(s + "end.subtitle", "Countless cursed spirits have been released into Tokyo.");
      lang.add(s + "end", "As he leaves, Kenjaku looses every curse he holds. The chain reaction spreads over the wards. Something larger is coming.");
      lang.add(s + "survived", "You survived Shibuya. Your grade evaluation improves.");
   }

   private static void commands(LanguageProvider lang) {
      String c = "commands.cursed_domain.culling.";
      lang.add(c + "shibuya.running", "The Shibuya Incident is already under way.");
      lang.add(c + "shibuya.started", "The curtain falls over Shibuya at %s, %s.");
      lang.add(c + "shibuya.status", "Shibuya: %1$s, stage %2$s, wave %3$s, %4$s tracked enemies, centre %5$s, %6$s.");
      lang.add(c + "shibuya.stopped", "The Shibuya Incident was called off.");
      lang.add(c + "shibuya.skipped", "Skipped to the next stage of the Shibuya Incident.");
      lang.add(c + "running", "The Culling Game is already running.");
      lang.add(c + "started", "The Culling Game has begun. Tokyo No. 1 Colony stands at %s, %s.");
      lang.add(c + "not_running", "The Culling Game is not running.");
      lang.add(c + "stopped", "The Culling Game was stopped.");
      lang.add(c + "reset", "The Culling Game was reset. It can be started again.");
      lang.add(c + "status", "Culling Game: %1$s, day %2$s, %3$s players, %4$s rules, endgame stage %5$s, Gojo freed: %6$s.");
      lang.add(c + "endgame.already", "The endgame has already begun.");
      lang.add(c + "endgame", "Kenjaku begins the endgame at the Lake Gosho Colony.");
      lang.add(c + "day", "The Culling Game is now on day %s.");
      lang.add(c + "points", "%s now has %s points.");
      lang.add(c + "not_player", "%s is not a player.");
      lang.add(c + "rule.unknown", "Unknown rule.");
      lang.add(c + "rule.exists", "That rule is already in force.");
      lang.add(c + "summon.unknown", "Unknown profile.");
      lang.add(c + "summoned", "Summoned %s.");
      lang.add(c + "locate.shibuya", "Shibuya is at %s, %s.");
      lang.add(c + "locate.colony", "The %s is at %s, %s (radius %s).");
      lang.add(c + "locate.unknown", "Unknown colony, or the game has never started.");
   }
}
