package shared;

import java.io.Serializable;
import java.util.Objects;

public class SearchRequest implements Serializable {
    private String playerId;
    private Double minStars;
    private RiskLevel riskLevel;
    private BetCategory betCategory;

    public SearchRequest() {}

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Double getMinStars() {
        return minStars;
    }

    public void setMinStars(Double minStars) {
        this.minStars = minStars;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public BetCategory getBetCategory() {
        return betCategory;
    }

    public void setBetCategory(BetCategory betCategory) {
        this.betCategory = betCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchRequest)) return false;
        SearchRequest that = (SearchRequest) o;
        return Objects.equals(playerId, that.playerId)
                && Objects.equals(minStars, that.minStars)
                && riskLevel == that.riskLevel
                && betCategory == that.betCategory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, minStars, riskLevel, betCategory);
    }
}
