/*
 * Concord - Copyright (c) 2020 SciWhiz12
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tk.sciwhiz12.concord.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import tk.sciwhiz12.concord.Concord;
import tk.sciwhiz12.concord.ConcordConfig;
import tk.sciwhiz12.concord.msg.Messaging;
import tk.sciwhiz12.concord.util.Messages;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SayCommandHook {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!ConcordConfig.SAY_COMMAND_HOOK.get()) return;

        LOGGER.debug("Hooking into /say command");
        event.getDispatcher().register(literal("say")
                .requires((ctx) -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argument("message", MessageArgument.message())
                        .executes(SayCommandHook::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        MessageArgument.ChatMessage message = MessageArgument.getChatMessage(ctx, "message");
        CommandSourceStack source = ctx.getSource();
        PlayerList playerList = source.getServer().getPlayerList();
        message.resolve(source, chatMessage -> {
            playerList.broadcastChatMessage(chatMessage, source, ChatType.bind(ChatType.SAY_COMMAND, source));
            sendMessage(ctx, chatMessage);
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void sendMessage(CommandContext<CommandSourceStack> ctx, PlayerChatMessage message) {
        try {
            if (Concord.isEnabled() && ConcordConfig.COMMAND_SAY.get()) {
                Messaging.sendToChannel(Concord.getBot().getDiscord(),
                        Messages.SAY_COMMAND.component(ctx.getSource().getDisplayName(), message.serverContent()).getString());
            }
        } catch (Exception e) {
            LOGGER.warn("Exception from command hook; ignoring to continue command execution", e);
        }
    }
}
