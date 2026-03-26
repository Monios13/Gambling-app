package manager_app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ManagerConsole {

    public static void main(String[] args) throws Exception {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 8080;

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Manager Menu ---");
                System.out.println("1. Add game (Enter JSON)");
                System.out.println("2. Remove game");
                System.out.println("0. Exit");
                System.out.print("Choice: ");
                
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 0) {
                    System.out.println("Exiting...");
                    break;
                }

                Message requestMessage = null;

                switch (choice) {
                    case 1:
                        System.out.println("Enter Game JSON string:");
                        String jsonInput = scanner.nextLine();
                        Game newGame = parseJsonToGame(jsonInput); // Η δική σου μέθοδος
                        requestMessage = new Message("ADD_GAME", newGame);
                        break;
                    case 2:
                        System.out.print("Enter game name to remove: ");
                        String gameName = scanner.nextLine();
                        requestMessage = new Message("REMOVE_GAME", gameName);
                        break;
                    default:
                        System.out.println("Invalid choice");
                        continue;
                }

                // --- ΛΟΓΙΚΗ ΕΡΓΑΣΤΗΡΙΟΥ ---
                try {
                    Socket socket = new Socket(host, port);
                    
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(requestMessage); 
                    out.flush();
                    
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    Message response = (Message) in.readObject();
                    
                    socket.close();

                    System.out.println("[MASTER RESPONSE]: " + response.getPayload());

                } catch (Exception e) {
                    System.out.println("Error connecting to Master: " + e.getMessage());
                }
            }
        }
    }

    // Κράτησα τον δικό σου parser ακριβώς όπως τον είχες γράψει!
    private static Game parseJsonToGame(String json) {
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
        if (comma == -1) comma = json.indexOf("}", colon + 1);
        String numStr = json.substring(colon + 1, comma).trim();
        return Double.parseDouble(numStr);
    }
}