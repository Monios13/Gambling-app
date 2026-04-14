package shared;

import java.io.Serializable;

public class EditGameRequest implements Serializable {
    private String gameName;
    private RiskLevel newRiskLevel;
    private Double newMinBet;
    private Double newMaxBet;

    public EditGameRequest() {}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public RiskLevel getNewRiskLevel() {
        return newRiskLevel;
    }

    public void setNewRiskLevel(RiskLevel newRiskLevel) {
        this.newRiskLevel = newRiskLevel;
    }

    public Double getNewMinBet() {
        return newMinBet;
    }

    public void setNewMinBet(Double newMinBet) {
        this.newMinBet = newMinBet;
    }

    public Double getNewMaxBet() {
        return newMaxBet;
    }

    public void setNewMaxBet(Double newMaxBet) {
        this.newMaxBet = newMaxBet;
    }
}
