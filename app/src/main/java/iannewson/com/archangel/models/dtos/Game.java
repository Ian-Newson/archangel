package iannewson.com.archangel.models.dtos;

import java.util.Date;
import java.util.UUID;

public class Game {
    public int version;
    public UUID uid;
    public String gameVersion;
    public int numPlayersDesired;
    public UUID[] playerIds;
    public Date startTime;
    public String status;
    public int maximumTeamSize;
    public Date dateModified;
    public Boolean allowStepDown;

    public SearchParameters[] searchParameters = new SearchParameters[0];

    public GameTypes getGameType() {
        if (0 == searchParameters.length) return null;
        SearchParameters parameter = null;
        for (int i = 0; i < searchParameters.length; ++i) {
            if ("GAME_MODE".equals(searchParameters[i].parameter.name)) {
                parameter = searchParameters[i];
                break;
            }
        }
        if (null == parameter) return null;
        return parameter.getGameType();
    }

    public Boolean allows1v1() {
        GameTypes gameType = getGameType();
        Boolean isDm = null != gameType && GameTypes.Deathmatch == gameType;
        return isDm &&
                allowStepDown;
    }

    public static class SearchParameters {

        public Parameter parameter = new Parameter();
        public String comparisonType;

        public GameTypes getGameType() {
            if ("1b815e56-0f23-4958-aaf3-88940310ddfa".equals(parameter.value)) return GameTypes.Deathmatch;
            return GameTypes.Coop;
        }
    }

    public static class Parameter {
        public String name, type, value;
    }

    public enum GameTypes {
        Deathmatch,
        Coop
    }
}
