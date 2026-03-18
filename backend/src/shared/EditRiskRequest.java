package shared;

import java.io.Serializable;

public class EditRiskRequest implements Serializable {
    private String gameName;
    private RiskLevel newRiskLevel;

    public EditRiskRequest() {}

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
}