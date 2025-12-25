package kaiakk.sphericality;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import kaiakk.sphericality.network.GravityChangePayload;
import kaiakk.sphericality.network.GravityVariables;
import kaiakk.sphericality.item.GravityHookItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sphericality implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("gravitymachine");
    public static final String MOD_ID = "gravitymachine";

    /* 
    public static final ResourceKey<Item> GRAVITY_HOOK_KEY = ResourceKey.create(
            Registries.ITEM, 
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "gravity_hook")
    );

    public static final Item GRAVITY_HOOK = Registry.register(
            BuiltInRegistries.ITEM,
            GRAVITY_HOOK_KEY,
            new GravityHookItem(new Item.Properties().stacksTo(1).setId(GRAVITY_HOOK_KEY))
    ); 
    */

    @Override
    public void onInitialize() {
        LOGGER.info("The Gravity Machine is initializing...");
        
        GravityVariables.load();

        /*
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(GRAVITY_HOOK);
        });

        */

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (player != null) {
                double vanilla = Mth.clamp(GravityVariables.VANILLA_GRAVITY, GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                var attr = player.getAttribute(Attributes.GRAVITY);
                if (attr != null) {
                    attr.setBaseValue(vanilla);
                }
                try {
                    player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new GravityChangePayload.ServerHandshakePayload()));
                } catch (Exception ignored) {}
            }
        });
        
        PayloadTypeRegistry.playC2S().register(GravityChangePayload.TYPE, GravityChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GravityChangePayload.TYPE, GravityChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GravityChangePayload.ServerHandshakePayload.TYPE, GravityChangePayload.ServerHandshakePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GravityChangePayload.ClientProbePayload.TYPE, GravityChangePayload.ClientProbePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(GravityChangePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player != null) {
                    var attr = player.getAttribute(Attributes.GRAVITY);
                    if (attr != null) {
                        double current = attr.getBaseValue();
                        double target = Mth.clamp(current + payload.change(), GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                        attr.setBaseValue(target);
                        try {
                            double radius = 128.0D;
                            var server = player.getServer();
                            if (server != null) {
                                server.getPlayerList().getPlayers().stream()
                                        .filter(p -> p.position().distanceToSqr(player.position()) <= radius * radius)
                                        .forEach(p -> {
                                            if (p instanceof ServerPlayer sp && sp.connection != null) {
                                                try {
                                                    sp.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new GravityChangePayload(target)));
                                                } catch (Exception ignored) {}
                                            }
                                        });
                            }
                        } catch (Exception ignored) {}
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(GravityChangePayload.ClientProbePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player != null && player.connection != null) {
                    try {
                        player.connection.send(new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new GravityChangePayload.ServerHandshakePayload()));
                    } catch (Exception ignored) {}
                }
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("gravity")
                .then(Commands.literal("set")
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY))
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player != null) {
                                double val = DoubleArgumentType.getDouble(context, "value");
                                double clamped = Mth.clamp(val, GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                                player.getAttribute(Attributes.GRAVITY).setBaseValue(clamped);
                                context.getSource().sendSystemMessage(Component.literal("§bNew gravity set to: " + clamped));
                                return 1;
                            }
                            return 0;
                        }))));
        });
    }
}
