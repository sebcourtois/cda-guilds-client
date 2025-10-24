package org.afpa.chatellerault.guildsclient;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

@Log4j2
public class GuildsClient implements Closeable, AutoCloseable {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public GuildsClient(String hostname, int port) throws IOException {
        this.socket = new Socket(InetAddress.getByName(hostname), port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public JsonNode sendCommand(String command, Map<String, Object> params) throws IOException {
        var jsonObjMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var payload = Map.of("command", command, "params", params);
        String request = jsonObjMapper.writeValueAsString(payload);
        String response = this.sendRequest(request);
        return jsonObjMapper.readTree(response);
    }

    public String sendRequest(String request) throws IOException {
        this.writer.println(request);

        var responseBuilder = new StringBuilder();
        boolean hasNext = true;
        while (hasNext) {
            String someLine = this.reader.readLine();
            if (someLine == null) {
                break;
            }
            responseBuilder.append("%s%n".formatted(someLine));
            hasNext = this.reader.ready();
        }
        return responseBuilder.toString();
    }

    @Override
    public void close() {
        List<Closeable> thingsToClose = List.of(this.reader, this.writer, this.socket);
        for (var each : thingsToClose) {
            try {
                each.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
