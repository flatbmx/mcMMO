package com.gmail.nossr50.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

import com.gmail.nossr50.chat.ChatMode;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;

import com.google.common.collect.ImmutableList;

public abstract class ChatCommand implements TabExecutor {
    protected ChatMode chatMode;
    private McMMOPlayer mcMMOPlayer;

    public ChatCommand(ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 0:
                if (CommandUtils.noConsoleUsage(sender)) {
                    return true;
                }

                mcMMOPlayer = UserManager.getPlayer(sender.getName());

                if (chatMode.isEnabled(mcMMOPlayer)) {
                    disableChatMode(sender);
                }
                else {
                    enableChatMode(sender);
                }

                return true;

            case 1:
                if (CommandUtils.shouldEnableToggle(args[0])) {
                    if (CommandUtils.noConsoleUsage(sender)) {
                        return true;
                    }

                    mcMMOPlayer = UserManager.getPlayer(sender.getName());

                    enableChatMode(sender);
                    return true;
                }

                if (CommandUtils.shouldDisableToggle(args[0])) {
                    if (CommandUtils.noConsoleUsage(sender)) {
                        return true;
                    }

                    mcMMOPlayer = UserManager.getPlayer(sender.getName());

                    disableChatMode(sender);
                    return true;
                }

                // Fallthrough

            default:
                handleChatSending(sender, args);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (args.length) {
            case 1:
                return StringUtil.copyPartialMatches(args[0], CommandUtils.TRUE_FALSE_OPTIONS, new ArrayList<String>(CommandUtils.TRUE_FALSE_OPTIONS.size()));
            default:
                return ImmutableList.of();
        }
    }

    protected String buildChatMessage(String[] args, int index) {
        StringBuilder builder = new StringBuilder();
        builder.append(args[index]);

        for (int i = index + 1; i < args.length; i++) {
            builder.append(" ");
            builder.append(args[i]);
        }

        return builder.toString();
    }

    protected abstract void handleChatSending(CommandSender sender, String[] args);

    private void enableChatMode(CommandSender sender) {
        if (chatMode == ChatMode.PARTY && mcMMOPlayer.getParty() == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return;
        }

        chatMode.enable(mcMMOPlayer);
        sender.sendMessage(chatMode.getEnabledMessage());
    }

    private void disableChatMode(CommandSender sender) {
        if (chatMode == ChatMode.PARTY && mcMMOPlayer.getParty() == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return;
        }

        chatMode.disable(mcMMOPlayer);
        sender.sendMessage(chatMode.getDisabledMessage());
    }
}
