package reducer;

import master.ReduceSearchRequest;
import shared.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ReducerHandler implements Runnable {

    private final Socket           socket;
    private final ReduceAggregator aggregator;

    public ReducerHandler(Socket socket, ReduceAggregator aggregator) {
        this.socket     = socket;
        this.aggregator = aggregator;
    }

    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream  in  = new ObjectInputStream(socket.getInputStream())) {

            out.flush();
            Object raw = in.readObject();
            if (!(raw instanceof Message)) return;

            Message msg = (Message) raw;

            switch (msg.getType()) {

                case SEARCH_RESULT_PARTIAL: {
                    MapSearchResult result = (MapSearchResult) msg.getPayload();
                    aggregator.addSearchPartial(result.getJobId(), result.getGames());
                    out.writeObject(new AckResponse(true, "Partial received."));
                    out.flush();
                    break;
                }

                case STATS_RESULT_PARTIAL: {
                    MapStatsResult result = (MapStatsResult) msg.getPayload();
                    aggregator.addStatsPartial(result.getJobId(), result.getStats());
                    out.writeObject(new AckResponse(true, "Stats partial received."));
                    out.flush();
                    break;
                }

                case SEARCH_GAMES: {
                    ReduceSearchRequest req = (ReduceSearchRequest) msg.getPayload();
                    String jobId = req.getJobId();
                    List<GameSummary> merged = aggregator.reduceSearch(jobId, req.getExpectedWorkers());
                    out.writeObject(new SearchResultResponse(merged));
                    out.flush();
                    break;
                }

                case GET_GAME_STATS:
                case GET_PLAYER_STATS: {
                    ReduceStatsRequest req = (ReduceStatsRequest) msg.getPayload();
                    StatsResponse response = new StatsResponse(
                            msg.getType() == MessageType.GET_GAME_STATS ? "Game Stats" : "Player Stats",
                            aggregator.reduceStats(req.getJobId(), req.getExpectedWorkers())
                    );
                    out.writeObject(response);
                    out.flush();
                    break;
                }

                default: {
                    out.writeObject(new ErrorResponse("UNSUPPORTED", "Reducer: unsupported type " + msg.getType()));
                    out.flush();
                }
            }

        } catch (Exception e) {
            System.err.println("[ReducerHandler] Error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
