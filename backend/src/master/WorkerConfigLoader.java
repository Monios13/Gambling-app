package master;

import shared.WorkerInfo;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * WorkerConfigLoader — reads the worker list from a plain-text config file.
 *
 * File format (one worker per line, fields separated by whitespace):
 *   <id>  <host>  <port>
 *
 * Lines that start with '#' or are blank are ignored.
 *
 * Example:
 *   # Worker nodes
 *   0  localhost  6001
 *   1  192.168.1.10  6002
 *   2  192.168.1.11  6003
 */
public class WorkerConfigLoader {

    private WorkerConfigLoader() {}   // utility class

    public static List<WorkerInfo> load(String filePath) throws IOException {
        List<WorkerInfo> list = new ArrayList<>();

        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("\\s+");
            if (parts.length < 3) {
                System.err.println("[WorkerConfigLoader] Skipping malformed line: " + line);
                continue;
            }

            try {
                int    id   = Integer.parseInt(parts[0]);
                String host = parts[1];
                int    port = Integer.parseInt(parts[2]);
                list.add(new WorkerInfo(id, host, port));
            } catch (NumberFormatException e) {
                System.err.println("[WorkerConfigLoader] Skipping line with bad number: " + line);
            }
        }

        if (list.isEmpty()) {
            throw new IllegalStateException("Config file contains no valid worker entries: " + filePath);
        }

        System.out.println("[WorkerConfigLoader] Loaded " + list.size() + " worker(s) from " + filePath);
        return list;
    }
}