package processing.test.skropclient.game.state;

public enum GameState {
    NOT_CONNECTING,

    CONNECTING,
    CONNECTION_FAILED,
    WAITING_FOR_CONNECTION_2,
    WAITING_FOR_GAME_BEGIN,
    IN_GAME,
    END_GAME
}
