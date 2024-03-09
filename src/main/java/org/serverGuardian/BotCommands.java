package org.serverGuardian;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;




public class BotCommands extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) throws IndexOutOfBoundsException {

        switch (event.getName()) {
            case "dupa" -> event.reply("Sam jesteś dupa, matka wie jak się zachowujesz w internecie?").queue();
            case "showguilds" -> System.out.println(event.getJDA().getGuilds());
            case "backup" -> new SaveGuildCommand().saveChannel(event, event.getOption("sciezka").getAsString() );



        }

        if(event.getName() == "backup"){

        }

    }







}