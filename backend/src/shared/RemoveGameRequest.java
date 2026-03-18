package shared;

import java.io.Serializable;

public class RemoveGameRequest implements Serializable {
    private String gameName;

    public RemoveGameRequest() {}

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}