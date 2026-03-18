package shared;

import java.io.Serializable;
import java.util.List;

public class SearchResultResponse implements Serializable {
    private List<GameSummary> games;

    public SearchResultResponse() {}

    public SearchResultResponse(List<GameSummary> games) {
        this.games = games;
    }

    public List<GameSummary> getGames() {
        return games;
    }

    public void setGames(List<GameSummary> games) {
        this.games = games;
    }
}