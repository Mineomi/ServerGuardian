package org.serverGuardian;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SaveGuildCommand {

    public void saveChannel(SlashCommandInteractionEvent event, String basePath){
//        int numberOfChannels;
//        int numberOfSavedChannels = 0;

        //event.getGuild().retrieveActiveThreads();
        List<TextChannel> channels = Objects.requireNonNull(event.getGuild()).getTextChannels();

        for (TextChannel channel : channels) {
            handleChannelSaveConversation(channel, event.getChannel().asTextChannel(), "", basePath);
        }
    }


    private void handleChannelSaveConversation(MessageChannel channel, TextChannel backupChannel, String parentPath, String basePath) {
        String folderName = channel.getName();
        String fileName = channel.getName() + ".txt";


        if(basePath.toCharArray()[basePath.length() - 1] != '\\'){
            basePath += '\\';
        }


        if(channel instanceof TextChannel){
            parentPath = basePath + channel.getHistory().getChannel().asTextChannel().getGuild().getName();
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


        int MAX_DESIRED = 10000;
        List<Message> messages = new ArrayList<>();

        channel.getIterableHistory()
                .forEachAsync(message -> {
                    messages.add(message);
//                    progressMessage.editMessage(getProgressInfo(channel)).queue();
                    return messages.size() < MAX_DESIRED;
                })
                .thenAccept(_ignored -> {
                    Collections.reverse(messages);
                    for (Message message : messages) {
                        try {
                            /*if (message.getIdLong() == progressMessage.getIdLong()) {
                                continue;
                            }*/

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
                    //numberOfSavedChannels++;
                })
                .exceptionally(error -> {
                    error.printStackTrace();
                    channel.sendMessage("Conversation saving failed due to error: " + error.getMessage()).queue();
                    return null;
                })
                .whenComplete((_ignored, _ignored2) -> silentClose(fileWriter));



        //Saving Threads

        if(channel instanceof TextChannel){
            for(ThreadChannel threadChannel : ((TextChannel) channel).retrieveArchivedPublicThreadChannels().complete().stream().toList()){ //Saves archived threads
                //handleChannelSaveConversation(threadChannel, backupChannel, finalParentPath + "\\" + channel.getName(), basePath); //when retrieveArchivedThread is called archived threads can are available to getThreadChannels method
            }

            for(ThreadChannel threadChannel : ((TextChannel) channel).getThreadChannels()){ //Saves active threads
                handleChannelSaveConversation(threadChannel, backupChannel, finalParentPath + "\\" + channel.getName(), basePath);
            }

        }
    }


    /*private Message createProgressMessage(TextChannel backupChannel, TextChannel channel){


        Message progressMessage;
        try {
            progressMessage = backupChannel.sendMessage(getProgressInfo(channel)).complete();
            return progressMessage;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }*/


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



    public static void silentClose(AutoCloseable closeable)
    {
        try
        {
            closeable.close();
        }
        catch (Exception ignored) {}
    }

    /*private String getProgressInfo(MessageChannel channel) {
        String message = "";
        message += "Processing channel: " + channel.getName() + "\n";
        message += "Total channels retrieved thus far: " + numberOfChannels + "/" + numberOfSavedChannels;

        return message;
    }*/
}
