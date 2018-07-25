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
}
