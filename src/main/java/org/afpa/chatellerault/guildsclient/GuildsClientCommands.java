package org.afpa.chatellerault.guildsclient;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.SelectItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Command
public class GuildsClientCommands {
    private final ComponentFlow.Builder componentFlowBuilder;

    public GuildsClientCommands(ComponentFlow.Builder componentFlowBuilder) {
        this.componentFlowBuilder = componentFlowBuilder;
    }

    @Command(command = "send-caravan")
    public void sendCaravan() throws Exception {
        try (
                var guildsClient = new GuildsClient("localhost", 49394)
        ) {
            JsonNode response = guildsClient.sendCommand("list_caravans", Map.of());
//            System.out.println(response.toPrettyString());

            ArrayNode resultNode = response.withArrayProperty("result");
            List<SelectItem> caravanSelection = resultNode.valueStream()
                    .filter(caravanNode -> caravanNode.get("destination_id").asText() == null)
                    .map(caravanNode -> caravanNode.get("name").asText())
                    .map(caravanName -> SelectItem.of(caravanName, caravanName))
                    .toList();

            if (caravanSelection.isEmpty()) {
                log.info("No stationed caravans found");
                return;
            }

            ComponentFlow flow = componentFlowBuilder.clone().reset()
                    .withSingleItemSelector("caravanSelector")
                    .name("Select caravan:").selectItems(caravanSelection)
                    .and().build();

            ComponentContext<? extends ComponentContext<?>> resCtx = flow.run().getContext();

        }
    }


    @Command(command = "list-caravans")
    public void listCaravans() throws Exception {
        try (
                var guildsClient = new GuildsClient("localhost", 49394)
        ) {
            JsonNode response = guildsClient.sendCommand("list_caravans", Map.of());
            System.out.println(response.toPrettyString());
        }
    }

    @Command(command = "list-trading-posts")
    public void listTradingPosts() throws Exception {
        try (
                var guildsClient = new GuildsClient("localhost", 49394)
        ) {
            JsonNode response = guildsClient.sendCommand("list_trading_posts", Map.of());
//            System.out.println(response.toPrettyString());
        }
    }

    @Command(command = "new-caravan")
    public void createCaravan() throws Exception {
        try (
                var guildsClient = new GuildsClient("localhost", 49394)
        ) {
            JsonNode response = guildsClient.sendCommand("list_trading_posts", Map.of());
//            System.out.println(response.toPrettyString());
            ArrayNode resultNode = response.withArrayProperty("result");
            Map<String, String> tradingPostIdForName = resultNode.valueStream().collect(
                    Collectors.toMap(
                            tradingPostNode -> tradingPostNode.get("name").asText(),
                            tradingPostNode -> tradingPostNode.get("id").asText()
                    )
            );
            List<SelectItem> tradingPostSelection = tradingPostIdForName.keySet().stream()
                    .map(tradingPostName -> SelectItem.of(tradingPostName, tradingPostName))
                    .toList();

            if (tradingPostSelection.isEmpty()) {
                log.info("No trading posts found");
                return;
            }

            ComponentFlow flow = componentFlowBuilder.clone().reset()
                    .withStringInput("nameInput")
                    .name("Enter caravan name:")//.defaultValue("defaultField1Value")
                    .and()
                    .withSingleItemSelector("locationSelector")
                    .name("Select starting trading post:").selectItems(tradingPostSelection)
                    .and().build();

            ComponentContext<? extends ComponentContext<?>> resCtx = flow.run().getContext();

            String newCaravanName = resCtx.get("nameInput", String.class);
            String locationName = resCtx.get("locationSelector", String.class);
            String tradingPostId = tradingPostIdForName.get(locationName);

            response = guildsClient.sendCommand("create_caravan", Map.of(
                    "name", newCaravanName,
                    "trading_post", tradingPostId
            ));
            System.out.println(response.toPrettyString());
        }
    }

    @Command(command = "echo")
    public void talkToServer() {
        try (
                var guildsClient = new GuildsClient("localhost", 49394)
        ) {
            var stdinReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while (true) {
                System.out.println("say something:");
                userInput = stdinReader.readLine();
                if (userInput == null) break;
                JsonNode response = guildsClient.sendCommand(
                        "echo", Map.of("message", userInput)
                );
                System.out.println(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("bye");
    }
}
