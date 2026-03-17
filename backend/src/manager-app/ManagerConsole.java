package manager_app;

import shared.Game;
import shared.Message;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Απλό console app για τον manager για το Παραδοτέο Α.
 * Επιτρέπει προσθήκη / αφαίρεση παιχνιδιών.
 */
public class ManagerConsole {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java manager_app.ManagerConsole <masterHost> <masterPort>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Manager Menu:");
                System.out.println("1. Add game");
                System.out.println("2. Remove game");
                System.out.println("0. Exit");
                System.out.print("Choice: ");
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice == 0) break;

                switch (choice) {
                    case 1:
                        System.out.print("JSON file path: ");
                        String path = scanner.nextLine();
                        Game game = loadGameFromJson(path);
                        out.writeObject(new Message("ADD_GAME", game));
                        out.flush();
                        System.out.println(((Message) in.readObject()).getPayload());
                        break;
                    case 2:
                        System.out.print("Game name to remove: ");
                        String name = scanner.nextLine();
                        out.writeObject(new Message("REMOVE_GAME", name));
                        out.flush();
                        System.out.println(((Message) in.readObject()).getPayload());
                        break;
                    default:
                        System.out.println("Invalid choice");
                }
            }
        }
    }

    /**
     * Πολύ απλός parser για το συγκεκριμένο JSON format της άσκησης
     * χωρίς χρήση εξωτερικής βιβλιοθήκης.
     */
    private static Game loadGameFromJson(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
        }
        String json = sb.toString();

        String gameName = extractString(json, "GameName");
        String providerName = extractString(json, "ProviderName");
        int stars = (int) extractNumber(json, "Stars");
        int noOfVotes = (int) extractNumber(json, "NoOfVotes");
        String gameLogo = extractString(json, "GameLogo");
        double minBet = extractNumber(json, "MinBet");
        double maxBet = extractNumber(json, "MaxBet");
        String riskLevel = extractString(json, "RiskLevel");
        String hashKey = extractString(json, "HashKey");

        return new Game(gameName, providerName, stars, noOfVotes,
                gameLogo, minBet, maxBet, riskLevel, hashKey);
    }

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        int colon = json.indexOf(":", idx);
        int firstQuote = json.indexOf("\"", colon + 1);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static double extractNumber(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0.0;
        int colon = json.indexOf(":", idx);
        int comma = json.indexOf(",", colon + 1);
        int end = comma == -1 ? json.indexOf("}", colon + 1) : comma;
        String numStr = json.substring(colon + 1, end).trim();
        return Double.parseDouble(numStr);
    }
}

