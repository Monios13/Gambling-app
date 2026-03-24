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

                // Worker pushes its local Map result
                case SEARCH_RESULT_PARTIAL: {
                    MapSearchResult result = (MapSearchResult) msg.getPayload();
                    aggregator.addSearchPartial(result.getJobId(), result.getGames());
                    out.writeObject(new AckResponse(true, "Partial received."));
                    out.flush();
                    break;
                }

                // Master triggers the Reduce phase
                case SEARCH_GAMES: {
                    ReduceSearchRequest req = (ReduceSearchRequest) msg.getPayload();
                    String jobId = "search:" + req.getOriginalRequest().hashCode();
                    List<GameSummary> merged = aggregator.reduceSearch(jobId, req.getExpectedWorkers());
                    out.writeObject(new SearchResultResponse(merged));
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