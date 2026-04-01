package manager_app;

import com.google.gson.Gson;
import shared.AckResponse;
import shared.ErrorResponse;
import shared.Game;
import shared.Message;
import shared.MessageSource;
import shared.MessageType;
import shared.RemoveGameRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerConsole {

    private static final Gson GSON = new Gson();
    private static final Path DEFAULT_GAMES_DIR = Paths.get("backend", "GamesData");

    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 8080;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Manager Menu ---");
                System.out.println("1. Add game from JSON text");
                System.out.println("2. Remove game");
                System.out.println("3. Load all JSON files from backend/GamesData");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 0) {
                    System.out.println("Exiting...");
                    break;
                }

                switch (choice) {
                    case 1:
                        System.out.println("Enter Game JSON string:");
                        String jsonInput = scanner.nextLine();
                        Game gameFromText = parseJsonToGame(jsonInput);
                        sendRequest(host, port, new Message(MessageType.ADD_GAME, MessageSource.MANAGER, gameFromText));
                        break;
                    case 2:
                        System.out.print("Enter game name to remove: ");
                        String gameName = scanner.nextLine();
                        RemoveGameRequest removeRequest = new RemoveGameRequest();
                        removeRequest.setGameName(gameName);
                        sendRequest(host, port, new Message(MessageType.REMOVE_GAME, MessageSource.MANAGER, removeRequest));
                        break;
                    case 3:
                        loadGamesFromDirectory(host, port, DEFAULT_GAMES_DIR);
                        break;
                    default:
                        System.out.println("Invalid choice");
                        break;
                }
            }
        }
    }

    private static void loadGamesFromDirectory(String host, int port, Path gamesDir) {
        List<Path> gameFiles = listJsonFiles(gamesDir);
        if (gameFiles.isEmpty()) {
            System.out.println("No JSON files found in " + gamesDir);
            return;
        }

        for (Path file : gameFiles) {
            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                Game game = parseJsonToGame(json);
                sendRequest(host, port, new Message(MessageType.ADD_GAME, MessageSource.MANAGER, game));
            } catch (Exception e) {
                System.out.println("[LOAD ERROR] " + file.getFileName() + ": " + e.getMessage());
            }
        }
    }

    private static List<Path> listJsonFiles(Path gamesDir) {
        List<Path> files = new ArrayList<>();
        if (!Files.isDirectory(gamesDir)) {
            return files;
        }

        try {
            Files.list(gamesDir)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted()
                    .forEach(files::add);
        } catch (IOException e) {
            System.out.println("[LOAD ERROR] Cannot read directory " + gamesDir + ": " + e.getMessage());
        }
        return files;
    }

    private static void sendRequest(String host, int port, Message requestMessage) {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(requestMessage);
            out.flush();

            Object response = in.readObject();
            System.out.println("[MASTER RESPONSE]: " + formatResponse(response));
        } catch (Exception e) {
            System.out.println("Error connecting to Master: " + e.getMessage());
        }
    }

    private static Game parseJsonToGame(String json) {
        Game game = GSON.fromJson(json, Game.class);
        if (game == null) {
            throw new IllegalArgumentException("Invalid game JSON.");
        }
        game.recalculateDerivedFields();
        return game;
    }

    private static String formatResponse(Object response) {
        if (response instanceof Message) {
            return String.valueOf(((Message) response).getPayload());
        }

        if (response instanceof AckResponse) {
            AckResponse ack = (AckResponse) response;
            return String.valueOf(ack.getMessage());
        }

        if (response instanceof ErrorResponse) {
            ErrorResponse error = (ErrorResponse) response;
            return error.getErrorCode() + ": " + error.getMessage();
        }

        return String.valueOf(response);
    }
}
