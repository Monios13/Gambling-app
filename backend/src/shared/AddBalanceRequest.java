package shared;

import java.io.Serializable;

public class AddBalanceRequest implements Serializable {
    private String playerId;
    private double amount;

    public AddBalanceRequest() {}

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}