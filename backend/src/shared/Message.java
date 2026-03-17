package shared;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;    // π.χ. "ADD_GAME", "SEARCH", "PLAY" [cite: 22, 23]
    private Object payload; // Το περιεχόμενο (π.χ. ένα αντικείμενο Game)

    public Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public Object getPayload() { return payload; }
}