package kaiakk.sphericality.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import kaiakk.sphericality.network.GravityVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen((Screen) parent);
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.gravitymachine.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.gravitymachine"));

        general.addEntry(
            entryBuilder.startDoubleField(Component.translatable("config.gravitymachine.step"), GravityVariables.STEP)
                        .setDefaultValue(0.05D)
                        .setMin(0.001D)
                        .setMax(1.0D)
                        .setSaveConsumer(newValue -> {
                            GravityVariables.STEP = newValue;
                            GravityVariables.save();
                        })
                        .build()
        );

        general.addEntry(
            entryBuilder.startIntField(Component.translatable("config.gravitymachine.reset_cooldown"), GravityVariables.RESET_COOLDOWN)
                        .setDefaultValue(60)
                        .setMin(1)
                        .setMax(3600)
                        .setSaveConsumer(newValue -> {
                            GravityVariables.RESET_COOLDOWN = newValue;
                            GravityVariables.save();
                        })
                        .build()
        );

                general.addEntry(
                    entryBuilder.startBooleanToggle(Component.translatable("config.gravitymachine.play_sounds"), GravityVariables.PLAY_SOUNDS)
                            .setSaveConsumer(newValue -> {
                                GravityVariables.PLAY_SOUNDS = newValue;
                                GravityVariables.save();
                            })
                            .build()
            );

        general.addEntry(
            entryBuilder.startDoubleField(Component.translatable("config.gravitymachine.minimum"), GravityVariables.MIN_GRAVITY)
                        .setDefaultValue(-2.0D)
                        .setMin(-100.0D)
                        .setMax(0.0D)
                        .setSaveConsumer(newValue -> {
                            GravityVariables.MIN_GRAVITY = newValue;
                            if (GravityVariables.MIN_GRAVITY > GravityVariables.MAX_GRAVITY) {
                                GravityVariables.MAX_GRAVITY = GravityVariables.MIN_GRAVITY;
                            }
                            GravityVariables.save();
                        })
                        .build()
        );

        general.addEntry(
            entryBuilder.startDoubleField(Component.translatable("config.gravitymachine.maximum"), GravityVariables.MAX_GRAVITY)
                        .setDefaultValue(2.0D)
                        .setMin(0.0D)
                        .setMax(100.0D)
                        .setSaveConsumer(newValue -> {
                            GravityVariables.MAX_GRAVITY = newValue;
                            if (GravityVariables.MAX_GRAVITY < GravityVariables.MIN_GRAVITY) {
                                GravityVariables.MIN_GRAVITY = GravityVariables.MAX_GRAVITY;
                            }
                            GravityVariables.save();
                        })
                        .build()
        );

        general.addEntry(
            entryBuilder.startDoubleField(Component.translatable("config.gravitymachine.default"), GravityVariables.VANILLA_GRAVITY)
                        .setDefaultValue(0.08D)
                        .setMin(-10.0D)
                        .setMax(10.0D)
                        .setSaveConsumer(newValue -> {
                            GravityVariables.VANILLA_GRAVITY = newValue;
                            GravityVariables.save();
                        })
                        .build()
        );

            general.addEntry(
                    entryBuilder.startEnumSelector(Component.translatable("config.gravitymachine.display_position"), GravityVariables.GravityDisplayPosition.class, GravityVariables.DISPLAY_POSITION)
                            .setDefaultValue(GravityVariables.GravityDisplayPosition.TOP_LEFT)
                            .setEnumNameProvider(pos -> {
                                String key;
                                if (pos == GravityVariables.GravityDisplayPosition.TOP_LEFT) key = "config.gravitymachine.position.top_left";
                                else if (pos == GravityVariables.GravityDisplayPosition.TOP_RIGHT) key = "config.gravitymachine.position.top_right";
                                else if (pos == GravityVariables.GravityDisplayPosition.BOTTOM_RIGHT) key = "config.gravitymachine.position.bottom_right";
                                else if (pos == GravityVariables.GravityDisplayPosition.BOTTOM_LEFT) key = "config.gravitymachine.position.bottom_left";
                                else key = "config.gravitymachine.position.disabled";
                                return Component.translatable(key);
                            })
                            .setSaveConsumer(newValue -> {
                                GravityVariables.DISPLAY_POSITION = newValue;
                                GravityVariables.save();
                            })
                            .build()
            );

        return builder.build();
    }
}
