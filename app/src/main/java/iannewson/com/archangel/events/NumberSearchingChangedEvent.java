package iannewson.com.archangel.events;

public class NumberSearchingChangedEvent {

    public NumberSearchingChangedEvent(Integer oldNumberSearching, int newNumberSearching) {
        this.oldNumberSearching = oldNumberSearching;
        this.newNumberSearching = newNumberSearching;
    }

    private Integer oldNumberSearching;
    private int newNumberSearching;

    public Integer getOldNumberSearching() {
        return oldNumberSearching;
    }

    public int getNewNumberSearching() {
        return newNumberSearching;
    }
}
