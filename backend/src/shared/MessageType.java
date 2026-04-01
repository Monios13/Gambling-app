package shared;

public enum MessageType {
    // Manager -> Master
    ADD_GAME,
    REMOVE_GAME,
    EDIT_GAME_RISK,

    // Player/Dummy Client -> Master
    SEARCH_GAMES,
    PLAY_GAME,
    ADD_BALANCE,
    RATE_GAME,

    // Master -> Worker
    STORE_GAME,
    REMOVE_GAME_LOCAL,
    UPDATE_GAME_RISK_LOCAL,
    SEARCH_LOCAL,
    PLAY_LOCAL,
    RATE_GAME_LOCAL,

    // Worker -> SRG
    RANDOM_REQUEST,

    // SRG -> Worker
    RANDOM_RESPONSE,

    // Worker -> Master
    SEARCH_RESULT_PARTIAL,
    PLAY_RESULT,
    REPLICATE_GAME,
    LOGO_RESPONSE,

    // Generic responses
    ACK,
    ERROR,

    // Optional health check
    PING,
    PONG
}
