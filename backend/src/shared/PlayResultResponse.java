package shared;

import java.io.Serializable;

public class PlayResultResponse implements Serializable {
    private boolean win;
    private double payout;
    private double newBalance;
    private String message;

    public PlayResultResponse() {}

    public PlayResultResponse(boolean win, double payout, double newBalance, String message) {
        this.win = win;
        this.payout = payout;
        this.newBalance = newBalance;
        this.message = message;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public double getPayout() {
        return payout;
    }

    public void setPayout(double payout) {
        this.payout = payout;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
