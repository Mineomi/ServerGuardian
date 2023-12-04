package org.serverGuardian;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class BotCommands extends ListenerAdapter {
    RestAction<?> latestUpdate = null;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) throws IndexOutOfBoundsException {

        switch (event.getName()) {
            case "dupa" -> event.reply("Sam jesteś dupa, matka wie jak się zachowujesz w internecie?").queue();
            case "showguilds" -> System.out.println(event.getJDA().getGuilds());
            case "backup" -> {
                event.getGuild().retrieveActiveThreads();
                List<TextChannel> channels = Objects.requireNonNull(event.getGuild()).getTextChannels();
                for (TextChannel channel : channels) {

                    handleChannelSaveConversation(channel, event.getChannel().asTextChannel(), "");
                }
            }
        }

    }



    private void handleChannelSaveConversation(MessageChannel channel, TextChannel runChannel, String parentPath) {
        String folderName = channel.getName();
        String fileName = channel.getName() + ".txt";

        if(channel instanceof TextChannel){
            parentPath = "C:\\Users\\mineo\\Desktop\\ServerGuardian\\backup\\" + channel.getHistory().getChannel().asTextChannel().getGuild().getName(); //Interface MessageChannel doesn't have method getGuild() so I have to do get this that way
        }
        String finalParentPath = parentPath;

        createNewFolder(finalParentPath, folderName, fileName);


        final FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(finalParentPath + "\\" + folderName + "\\" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            channel.sendMessage("Failed to save message due to error getting file writer: " + e.getMessage()).queue();
            return;
        }



        Message progressMessage;
        try {
            progressMessage = runChannel.sendMessage(getProgressInfo(parentPath, 0, channel)).complete();
        }
        catch (Exception e) {
            e.printStackTrace();
            silentClose(fileWriter);
            return;
        }



        int MAX_DESIRED = 10000;
        List<Message> messages = new ArrayList<>();

        channel.getIterableHistory()
                .forEachAsync(message -> {
                    messages.add(message);
                    handleProgressUpdate(progressMessage, finalParentPath, messages.size(), channel);
                    return messages.size() < MAX_DESIRED;
                })
                .thenAccept(_ignored -> {
                    Collections.reverse(messages);
                    for (Message message : messages) {
                        try {
                            if (message.getIdLong() == progressMessage.getIdLong()) {
                                continue;
                            }

                            List<Message.Attachment> attachments = message.getAttachments();

                            for (int i = 0; i < attachments.size(); i++) {
                                Message.Attachment attachment = attachments.get(i);

                                attachment.getProxy().downloadToFile(new File(finalParentPath + "\\" + folderName + "\\" + message.getId() + "(" + i  + ")" + "." + attachment.getFileExtension())).thenAccept(file -> {});

                            }

                            fileWriter.write(message.getId() + "\n"  + "Author: " + message.getAuthor().getName() + "\n" + "Content: " + message.getContentRaw() + "\n" +  "Number of attachments: " + attachments.size() + "\n\n\n");
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    System.out.println("Channel was successfully saved: " + channel.getName());
                })
                .exceptionally(error -> {
                    error.printStackTrace();
                    channel.sendMessage("Conversation saving failed due to error: " + error.getMessage()).queue();
                    return null;
                })
                .whenComplete((_ignored, _ignored2) -> silentClose(fileWriter));



        //Saving Threads

        if(channel instanceof TextChannel){
            for(ThreadChannel threadChannel : ((TextChannel) channel).retrieveArchivedPublicThreadChannels().complete().stream().toList()){ //Saves active threads
                handleChannelSaveConversation(threadChannel, runChannel, finalParentPath + "\\" + channel.getName());
            }

            for(ThreadChannel threadChannel : ((TextChannel) channel).getThreadChannels()){ //Saves archived threads
                handleChannelSaveConversation(threadChannel, runChannel, finalParentPath + "\\" + channel.getName());
            }

        }
    }





    private void createNewFolder(String filePath, String folderName, String fileName){
        File file = new File(filePath + "\\" + folderName + "\\" + fileName);
        file.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(file);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void handleProgressUpdate(Message progressMessage, String filePath, int totalMessages, MessageChannel channel) {
        RestAction<?> action = progressMessage.editMessage(getProgressInfo(filePath, totalMessages, channel));
        latestUpdate = action;

        action.setCheck(() -> {
            return action == latestUpdate;
        });
        action.submit().whenComplete((_ignored, _ignored2) -> {
            if (latestUpdate == action) {
                latestUpdate = null;
            }
        });
    }

    public static void silentClose(AutoCloseable closeable)
    {
        try
        {
            closeable.close();
        }
        catch (Exception ignored) {}
    }

    private String getProgressInfo(String filePath, int totalMessages, MessageChannel channel) {
        String message = "";
        message += "Processing channel: " + channel.getName() + "\n";
        message += "`" + filePath + "`\n\n";
        message += "Total messages retrieved thus far: " + totalMessages;

        return message;
    }


}