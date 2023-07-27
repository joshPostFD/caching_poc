package com.fanduel.josh.cache;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKey {

    private CacheKey() {}

    // Cache names must be constants to work with annotations, so enum could not be used
    public static final String TEAMS_CACHE_NAME = "teams";
    public static final String VENUES_CACHE_NAME = "venues";
    public static final String NF_EVENTS_CACHE = "nfevents";
    public static final String NF_BET_RECORDS_CACHE = "nfbetrecords";
    public static final String NF_NBA_BET_RECORDS_CACHE = "nfnbabetrecords";
    public static final String NF_GAME_STATUS_CACHE = "nfgamestatus";
    public static final String NF_PARTNER_PLAYER_ID = "nfpartnerplayerid";
    public static final String NF_PLAYER_DETAILS = "nfplayerdetails";
    public static final String NF_PLAYER_MAPPING = "nfplayermapping";
    public static final String NF_TEAM_ID_MAP_CACHE = "nfteamidsmap";
    public static final String NF_NBA_TEAM_ID_MAP_CACHE = "nfnbateamidsmap";
    public static final String NF_CONTENT_ARTICLES_CACHE = "nfcontentarticles";
    public static final String NF_CONTENT_ARTICLES_FOR_PLAYERS_CACHE =
            "nfcontentarticlesForPlayers";
    public static final String NF_CONTENT_ARTICLE_CACHE = "nfcontentarticle";
    public static final String NF_NFL_POWER_RANKINGS_CACHE = "nfnflpowerrankings";
    public static final String NF_NBA_POWER_RANKINGS_CACHE = "nfnbapowerrankings";
    public static final String NF_MLB_POWER_RANKINGS_CACHE = "nfmlbpowerrankings";
    public static final String NF_ID_MAPPING_CACHE = "nfidmapping";
    public static final String NF_EXTRACTED_PLAYER_CACHE = "nfextractedplayerstats";
    public static final String NF_PLAYERS_STATS_CACHE = "nfplayersstats";
    public static final String NF_NFL_PLAYERS_STATS_CACHE = "nfnflplayersstats";
    public static final String NF_HISTORICAL_PLAYERS_STATS_CACHE = "nfhistoricalplayersstats";
    public static final String NF_PROJECTIONS_CACHE = "nfprojectionscache";
    public static final String NF_NBA_PROJECTIONS_CACHE = "nfnbaprojectionscache";
    public static final String NF_CONTENT_PLAYER_UPDATES_CACHE = "nfcontentplayerupdates";
    public static final String NF_CONTENT_PLAYER_UPDATES_FOR_PLAYERS_CACHE =
            "nfcontentplayerupdatesForPlayers";
    public static final String SPORTRADAR_RESPONSE_CACHE = "srresponse";
    public static final String SPORTRADAR_NBA_STANDINGS_CACHE = "srnbastandings";
    public static final String SPORTRADAR_MLB_STANDINGS_CACHE = "srmlbstandings";
    public static final String SPORTRADAR_MLB_SEASONAL_STATS_CACHE = "srmlbseasonalstats";
    public static final String SPORTRADAR_MLB_GAME_BOXSCORE_CACHE = "srmlbgameboxscore";
    public static final String SPORTRADAR_FINISHED_MLB_GAME_BOXSCORE_CACHE =
            "srfinishedmlbgameboxscore";
    public static final String SPORTRADAR_PLAYER_DETAILS_CACHE = "srnflplayerdetails";
    public static final String SPORTRADAR_POSTGAME_STANDINGS_NFL = "srpostgamestandingsnfl";
    public static final String SPORTRADAR_POSTGAME_STANDINGS_NCAAFB = "srpostgamestandingsncaafb";
    public static final String SPORTRADAR_RANKINGS_CURRENT_WEEK_NCAAFB =
            "srrankingscurrentweekncaafb";
    public static final String SPORTRADAR_NBA_RANKINGS_CACHE = "srnbarankings";
    public static final String SPORTRADAR_NBA_DAY_SCHEDULE_CACHE = "srnbadayschedule";
    public static final String SPORTRADAR_SCHEDULE_CACHE_NFL = "srschedule";
    public static final String SPORTRADAR_SCHEDULE_CACHE_NBA = "srnbaschedule";
    public static final String SPORTRADAR_SCHEDULE_CACHE_MLB = "srmlbschedule";
    public static final String SPORTRADAR_PLAYERS_CACHE = "srplayers";
    public static final String SPORTRADAR_NFL_PLAYERS_INJURIES_CACHE = "srplayersinjuriesbyteam";
    public static final String WEEK_SCHEDULE_CACHE = "scheduleweek";
    public static final String WEEKS_SCHEDULE_STRUCTURE_CACHE = "weeksScheduleStructure";
    public static final String SPORTRADAR_WEEK_GAMES_SCHEDULE_CACHE = "srnflscheduleweekgames";
    public static final String SPORTRADAR_LATEST_ACTIVE_SEASON_CACHE = "srnfllatestactiveseason";
    public static final String SCHEDULE_CACHE = "schedule";
    public static final String NFL_SCHEDULES_V2 = "nflschedulesV2";
    public static final String NBA_SCHEDULES_V2 = "nbaschedulesV2";
    public static final String MLB_SCHEDULES_V2 = "mlbschedulesV2";
    public static final String SPORTRADAR_NFL_PREVIOUS_AND_NEXT_GAME_CACHE =
            "nflsrpreviousandnextgame";
    public static final String SPORTRADAR_NBA_PREVIOUS_AND_NEXT_GAME_CACHE =
            "nbasrpreviousandnextgame";
    public static final String NBA_SCHEDULE_CACHE = "nbaschedule";
    public static final String NBA_DAY_SCHEDULE_CACHE = "nbadayschedule";

    /**
     * @deprecated use the individually configured cache for each sport. Keeping so we don't have to
     *     refactor V1 stuff.
     */
    @Deprecated(forRemoval = true, since = "ASG-80")
    public static final String SPORTRADAR_HIERARCHY_CACHE_V1_NFL = "srhierarchyv1nfl";
    /**
     * @deprecated use the individually configured cache for each sport. Keeping so we don't have to
     *     refactor V1 stuff.
     */
    @Deprecated(forRemoval = true, since = "ASG-80")
    public static final String SPORTRADAR_HIERARCHY_CACHE_V1_NBA = "srhierarchyv1nba";

    public static final String TEAMS_CACHE_V2 = "teamslistV2";
    public static final String OPPONENTS_SCHEDULE_CACHE = "scheduleopponent";
    public static final String NBA_OPPONENTS_SCHEDULE_CACHE = "nbascheduleopponent";
    public static final String RESULTS_SCHEDULE_CACHE = "scheduleresult";
    public static final String ODDS_CACHE = "odds";
    public static final String ODDS_FOR_AFTER_NEXT_GAMES_CACHE = "oddsForAfterNextGames";
    public static final String SPORTRADAR_NFL_STATS_CACHE = "stats";
    public static final String SPORTRADAR_NBA_CURRENT_IN_PROGRESS_YEAR_CACHE = "nbaInProgressYear";
    public static final String SPORTRADAR_NBA_STATS_CACHE = "nbaStats";
    public static final String SPORTRADAR_NBA_PLAYERS_CACHE = "srNbaPlayers";
    public static final String NBA_ODDS_CACHE = "nbaodds";
    public static final String NFL_TEAM_LEADER_DATA_LOADER = "nflTeamLeader";
    public static final String NBA_TEAM_LEADER_DATA_LOADER = "nbaTeamLeader";
    public static final String NF_TEAM_PLAYERS_CACHE = "nfTeamPlayers";
    public static final String NF_NFL_PLAYERS_CACHE = "nfnflplayers";
    public static final String NF_NFL_PLAYER_CACHE = "nfnflplayer";
    public static final String NF_NFL_TEAM_PLAYERS_CACHE = "nfnflteamplayers";
    public static final String NF_NBA_PLAYERS_CACHE = "nfnbaplayers";
    public static final String NF_NBA_TEAM_PLAYERS_CACHE = "nfnbateamplayers";
    public static final String NF_MLB_PLAYERS_CACHE = "nfmlbplayers";
    public static final String NF_MLB_PLAYER_CACHE = "nfmlbplayer";
    public static final String NF_MLB_TEAM_PLAYERS_CACHE = "nfmlbteamplayers";
    public static final String SPORT_DATA_TEAM_CACHE = "sdteam";
    public static final String SPORT_DATA_TEAM_PLAYERS_CACHE = "sdteamplayers";
    public static final String NF_NBA_PLAYER_MAPPING = "nfnbapartnerplayerid";
    public static final String NF_NBA_TEAM_PLAYERS_STATS = "nfteamplayerstats";
    public static final String NF_NBA_PLAYERS_STATS_V2 = "nfteamplayerstatsV2";
    public static final String NF_NBA_PLAYERS_HISTORICAL_STATS_V2 = "nfhistoricalplayerstatsV2";
    public static final String NF_MLB_TEAM_PLAYERS_STATS_CACHE = "nfmlbteamplayerstats";
    public static final String SPORTRADAR_ALL_SEASONS_V2_CACHE = "srallseasonsV2";
    public static final String NF_NBA_TEAM_HISTORICAL_STATS = "nfteamhistoricalstats";
    public static final String OF_TEAM_FUTURES = "ofteamfutures";
    public static final String OF_NEXT_GAME_ODDS = "ofnextgameodds";
    public static final String OF_PLAYER_PROPS = "ofplayerprops";
    public static final String OF_POPULAR_BETS_BY_SPORT = "ofplayerbetsbysport";
    public static final String NCAAFB_GAMES_V2 = "ncaafbgamesV2";
    public static final String NF_NBA_PLAYERS_HISTORICAL_STATS = "nfplayerhistoricalcareerstats";
    public static final String NF_NBA_PLAYERS_REG_SEASON_CAREER_STATS =
            "nfplayerregseasoncareerstats";

    public static final String PARTNER_PLAYER_MAPPING_CACHE = "partnerplayer";
    public static final String NF_PLAYER_IDS_V2_CACHE = "nfplayeridsV2";
    public static final String PARTNER_PLAYERS_MAPPING_CACHE = "partnerplayersmap";

    public static List<String> values() {
        final CacheKey instance = new CacheKey();
        return Arrays.stream(CacheKey.class.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .map(
                        field -> {
                            try {
                                return field.get(instance);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
