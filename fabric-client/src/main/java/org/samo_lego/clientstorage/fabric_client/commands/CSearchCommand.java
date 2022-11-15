package org.samo_lego.clientstorage.fabric_client.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.samo_lego.clientstorage.fabric_client.util.ESPRender;
import org.samo_lego.clientstorage.fabric_client.util.StorageCache;

import java.util.concurrent.atomic.AtomicInteger;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static org.samo_lego.clientstorage.fabric_client.ClientStorageFabric.config;

public class CSearchCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
        dispatcher.register(literal("csearch")
                .then(argument("item", ItemArgument.item(context))
                        .then(argument("radius", DoubleArgumentType.doubleArg(0))
                                .executes(CSearchCommand::searchItem))
                        .executes(CSearchCommand::searchItem)));
    }

    private static int searchItem(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ItemInput item = ItemArgument.getItem(context, "item");
        ItemStack stack = item.createItemStack(1, false);
        LocalPlayer player = context.getSource().getPlayer();
        double radius = config.maxDist;
        try {
            radius = DoubleArgumentType.getDouble(context, "radius");
        } catch (Exception ignored) {
        }

        final double finalRadius = radius;

        MutableComponent message = Component.translatable("command.clientstorage.csearch", stack.getHoverName(), finalRadius)
                .withStyle(ChatFormatting.GOLD);

        AtomicInteger totalCount = new AtomicInteger();
        ESPRender.reset();
        StorageCache.CACHED_INVENTORIES.forEach(container -> {
            BlockPos blockPos = ((BlockEntity) container).getBlockPos();
            if (blockPos.closerThan(player.blockPosition(), finalRadius)) {
                int count = 0;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack itemStack = container.getItem(i);
                    if (itemStack.is(stack.getItem())) {
                        ESPRender.addPos(blockPos);
                        count += itemStack.getCount();
                        totalCount.addAndGet(itemStack.getCount());
                    }
                }

                if (count > 0) {
                    message.append(Component.translatable("\n%s items @ %s",
                                    Component.literal(String.valueOf(count)).withStyle(ChatFormatting.GREEN),
                                    Component.literal(blockPos.toShortString()).withStyle(ChatFormatting.DARK_GREEN))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        });
        if (totalCount.get() > 0) {
            player.sendSystemMessage(message.append(Component.translatable("\n%s total.", totalCount.get())));
        } else {
            MutableComponent errorMsg = Component.translatable("command.clientstorage.csearch.fail", stack.getHoverName(), finalRadius)
                    .withStyle(ChatFormatting.RED);
            player.sendSystemMessage(errorMsg);
        }

        return 1;
    }
}
