package shared;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType type;
    private MessageSource source;
    private Object payload;

    public Message() {}

    public Message(MessageType type, MessageSource source, Object payload) {
        this.type = type;
        this.source = source;
        this.payload = payload;
    }



    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageSource getSource() {
        return source;
    }

    public void setSource(MessageSource source) {
        this.source = source;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}