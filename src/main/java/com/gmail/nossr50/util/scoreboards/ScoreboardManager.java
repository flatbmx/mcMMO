package com.gmail.nossr50.util.scoreboards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.database.FlatfileDatabaseManager;
import com.gmail.nossr50.database.SQLDatabaseManager;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.runnables.scoreboards.ScoreboardChangeTask;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.skills.SkillUtils;

public class ScoreboardManager {
//    public static final OfflinePlayer ACROBATICS_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.ACROBATICS));
//    public static final OfflinePlayer ARCHERY_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.ARCHERY));
//    public static final OfflinePlayer AXES_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.AXES));
//    public static final OfflinePlayer EXCAVATION_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.EXCAVATION));
//    public static final OfflinePlayer FISHING_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.FISHING));
//    public static final OfflinePlayer HERBALISM_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.HERBALISM));
//    public static final OfflinePlayer MINING_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.MINING));
//    public static final OfflinePlayer REPAIR_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.REPAIR));
//    public static final OfflinePlayer SMELTING_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.SMELTING));
//    public static final OfflinePlayer SWORDS_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.SWORDS));
//    public static final OfflinePlayer TAMING_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.TAMING));
//    public static final OfflinePlayer UNARMED_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.UNARMED));
//    public static final OfflinePlayer WOODCUTTING_PLAYER = mcMMO.p.getServer().getOfflinePlayer(SkillUtils.getSkillName(SkillType.WOODCUTTING));

    public static final Map<String, Scoreboard> PLAYER_STATS_SCOREBOARDS = new HashMap<String, Scoreboard>();
    public static final Map<String, Scoreboard> PLAYER_RANK_SCOREBOARDS = new HashMap<String, Scoreboard>();

    public static Scoreboard globalStatsScoreboard;

    private static Objective playerStats;
    private static Objective playerRank;

    public final static String PLAYER_STATS_HEADER   = "mcMMO Stats";
    public final static String PLAYER_STATS_CRITERIA = "Player Skill Levels";

    public final static String PLAYER_RANK_HEADER   = "mcMMO Rankings";
    public final static String PLAYER_RANK_CRITERIA = "Player Skill Ranks";

    public final static String GLOBAL_STATS_POWER_LEVEL = "Power Level";

    public static void setupPlayerStatsScoreboard(String playerName) {
        setupPlayerScoreboard(playerName, PLAYER_STATS_SCOREBOARDS, playerStats, PLAYER_STATS_HEADER, PLAYER_STATS_CRITERIA);
    }

    public static void setupPlayerRankScoreboard(String playerName) {
        setupPlayerScoreboard(playerName, PLAYER_RANK_SCOREBOARDS, playerRank, PLAYER_RANK_HEADER, PLAYER_RANK_CRITERIA);
    }

    private static void setupPlayerScoreboard(String playerName, Map<String, Scoreboard> scoreboardMap, Objective objective, String header, String criteria) {
        if (scoreboardMap.containsKey(playerName)) {
            return;
        }

        Scoreboard scoreboard = mcMMO.p.getServer().getScoreboardManager().getNewScoreboard();

        objective = scoreboard.registerNewObjective(header, criteria);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        scoreboardMap.put(playerName, scoreboard);
    }

    public static void setupGlobalStatsScoreboard() {
        if (globalStatsScoreboard != null) {
            return;
        }

        globalStatsScoreboard = mcMMO.p.getServer().getScoreboardManager().getNewScoreboard();
    }

    public static void enablePlayerStatsScoreboard(McMMOPlayer mcMMOPlayer) {
        Player player = mcMMOPlayer.getPlayer();
        Scoreboard oldScoreboard = player.getScoreboard();
        Scoreboard newScoreboard = PLAYER_STATS_SCOREBOARDS.get(player.getName());

        if (oldScoreboard == newScoreboard) {
            return;
        }

        updatePlayerStatsScores(mcMMOPlayer);
        player.setScoreboard(newScoreboard);

        int displayTime = Config.getInstance().getMcstatsScoreboardTime();

        if (displayTime != -1) {
            new ScoreboardChangeTask(player, oldScoreboard).runTaskLater(mcMMO.p, displayTime * 20);
        }
    }

    public static void enablePlayerRankScoreboard(Player player) {
        Scoreboard oldScoreboard = player.getScoreboard();
        Scoreboard newScoreboard = PLAYER_RANK_SCOREBOARDS.get(player.getName());

        if (oldScoreboard == newScoreboard) {
            return;
        }

        updatePlayerRankScores(player);
        player.setScoreboard(newScoreboard);

        int displayTime = Config.getInstance().getMcrankScoreboardTime();

        if (displayTime != -1) {
            new ScoreboardChangeTask(player, oldScoreboard).runTaskLater(mcMMO.p, displayTime * 20);
        }
    }

    public static void enableGlobalStatsScoreboard(Player player, String skillName, int pageNumber) {
        Objective oldObjective = globalStatsScoreboard.getObjective(skillName);
        Scoreboard oldScoreboard = player.getScoreboard();

        if (oldObjective != null) {
            oldObjective.unregister();
        }

        Objective newObjective = globalStatsScoreboard.registerNewObjective(skillName, PLAYER_STATS_CRITERIA);
        newObjective.setDisplayName(ChatColor.GOLD + (skillName.equalsIgnoreCase("all") ? GLOBAL_STATS_POWER_LEVEL : SkillUtils.getSkillName(SkillType.getSkill(skillName))));

        updateGlobalStatsScores(player, newObjective, skillName, pageNumber);

        if (oldScoreboard == globalStatsScoreboard) {
            return;
        }

        player.setScoreboard(globalStatsScoreboard);

        int displayTime = Config.getInstance().getMctopScoreboardTime();

        if (displayTime != -1) {
            new ScoreboardChangeTask(player, oldScoreboard).runTaskLater(mcMMO.p, displayTime * 20);
        }
    }

    private static void updatePlayerStatsScores(McMMOPlayer mcMMOPlayer) {
        Player player = mcMMOPlayer.getPlayer();
        PlayerProfile profile = mcMMOPlayer.getProfile();
        Server server = mcMMO.p.getServer();

        for (SkillType skill : SkillType.values()) {
            if (skill.isChildSkill() || !Permissions.skillEnabled(player, skill)) {
                continue;
            }

            playerStats.getScore(server.getOfflinePlayer(SkillUtils.getSkillName(skill))).setScore(profile.getSkillLevel(skill));
        }

        playerStats.getScore(server.getOfflinePlayer(ChatColor.GOLD + "Power Level")).setScore(mcMMOPlayer.getPowerLevel());
    }

    private static void updatePlayerRankScores(Player player) {
        String playerName = player.getName();
        Server server = mcMMO.p.getServer();
        Integer rank;

        Map<String, Integer> skills = Config.getInstance().getUseMySQL() ? SQLDatabaseManager.readSQLRank(playerName) : FlatfileDatabaseManager.getPlayerRanks(playerName);

        for (SkillType skill : SkillType.values()) {
            if (skill.isChildSkill() || !Permissions.skillEnabled(player, skill)) {
                continue;
            }

            rank = skills.get(skill.name());

            if (rank != null) {
                playerStats.getScore(server.getOfflinePlayer(SkillUtils.getSkillName(skill))).setScore(rank);
            }
        }

        rank = skills.get("ALL");

        if (rank != null) {
            playerStats.getScore(server.getOfflinePlayer(ChatColor.GOLD + "Overall")).setScore(rank);
        }
    }

    private static void updateGlobalStatsScores(Player player, Objective objective, String skillName, int pageNumber) {
        int position = (pageNumber * 15) - 14;
        String startPosition = ((position < 10) ? "0" : "") + String.valueOf(position);
        String endPosition = String.valueOf(position + 14);
        Server server = mcMMO.p.getServer();

        if (Config.getInstance().getUseMySQL()) {
            String tablePrefix = Config.getInstance().getMySQLTablePrefix();
            String query = (skillName.equalsIgnoreCase("all") ? "taming+mining+woodcutting+repair+unarmed+herbalism+excavation+archery+swords+axes+acrobatics+fishing" : skillName);
            final HashMap<Integer, ArrayList<String>> userslist = SQLDatabaseManager.read("SELECT " + query + ", user, NOW() FROM " + tablePrefix + "users JOIN " + tablePrefix + "skills ON (user_id = id) WHERE " + query + " > 0 ORDER BY " + query + " DESC, user LIMIT " + ((pageNumber * 15) - 15) + ",15");

            for (ArrayList<String> stat : userslist.values()) {
                String playerName = stat.get(1);
                playerName = (playerName.equals(player.getName()) ? ChatColor.GOLD : "") + playerName;

                objective.getScore(server.getOfflinePlayer(playerName)).setScore(Integer.valueOf(stat.get(0)));
            }
        }
        else {
            for (PlayerStat stat : FlatfileDatabaseManager.retrieveInfo(skillName, pageNumber, 15)) {
                String playerName = stat.name;
                playerName = (playerName.equals(player.getName()) ? ChatColor.GOLD : "") + playerName;

                objective.getScore(server.getOfflinePlayer(playerName)).setScore(stat.statVal);
            }
        }

        objective.setDisplayName(objective.getDisplayName() + " (" + startPosition + " - " + endPosition + ")");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
}