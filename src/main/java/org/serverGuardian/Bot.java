package org.serverGuardian;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


import java.util.Arrays;
import java.util.List;

public class Bot {

    private static final GatewayIntent[] gatewayIntents = {GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS};


    public static void main(String[] args) throws InterruptedException {
        JDABuilder builder = JDABuilder.create(args[0], Arrays.asList(gatewayIntents))
         .addEventListeners(new BotCommands());

        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS, CacheFlag.STICKER);

        builder.setBulkDeleteSplittingEnabled(false);

        builder.setActivity(Activity.listening("Z XVI wiecznym portretem trumiennym – rozmowa Jacek Kaczmarski - https://www.youtube.com/watch?v=dPV-ewUzEoE"));


        JDA jda = builder.build().awaitReady();


        List<Guild> guilds = jda.getGuilds();


        for(Guild guild : guilds){
            guild.upsertCommand("dupa", "dupa").queue();
            guild.upsertCommand("backup", "backup")
                    .addOption(OptionType.STRING, "sciezka", "Ścieżka na którą zostanie zapisany backup")
        .queue();
            guild.upsertCommand("showguilds", "Wypisuje guildy").queue();
        }


    }
}