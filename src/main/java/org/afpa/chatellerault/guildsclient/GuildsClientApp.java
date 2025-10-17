package org.afpa.chatellerault.guildsclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.EnableCommand;


@EnableCommand(GuildsClientCommands.class)
@SpringBootApplication
public class GuildsClientApp {

    public static void main(String[] args) {
        SpringApplication.run(GuildsClientApp.class, args);
    }
}
