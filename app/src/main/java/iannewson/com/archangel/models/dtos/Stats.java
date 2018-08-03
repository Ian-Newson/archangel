package iannewson.com.archangel.models.dtos;

import java.util.Date;

public class Stats {

    public Date lastStatisticsTime;
    public int total;
    public long averageWaitTime;
    public Statuses statuses;

    public class Statuses {
        public int SEARCHING;
    }

}
