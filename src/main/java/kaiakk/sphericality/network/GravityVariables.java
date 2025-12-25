package kaiakk.sphericality.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GravityVariables {
    private GravityVariables() {}

    public static double STEP = 0.05D;
    public static int RESET_COOLDOWN = 60;
    public static boolean PLAY_SOUNDS = true;

    public enum GravityDisplayPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT,
        DISABLED
    }
    public static GravityDisplayPosition DISPLAY_POSITION = GravityDisplayPosition.TOP_LEFT;

    public static double MIN_GRAVITY = -2.0D;
    public static double MAX_GRAVITY = 2.0D;
    public static double VANILLA_GRAVITY = 0.08D;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("gravitymachine.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }
            String content = Files.readString(CONFIG_PATH);
            JsonObject json = GSON.fromJson(content, JsonObject.class);
            
            if (json.has("step")) STEP = json.get("step").getAsDouble();
            if (json.has("reset_cooldown")) RESET_COOLDOWN = json.get("reset_cooldown").getAsInt();
            if (json.has("play_sounds")) PLAY_SOUNDS = json.get("play_sounds").getAsBoolean();
            if (json.has("min_gravity")) MIN_GRAVITY = json.get("min_gravity").getAsDouble();
            if (json.has("max_gravity")) MAX_GRAVITY = json.get("max_gravity").getAsDouble();
            if (json.has("vanilla_gravity")) VANILLA_GRAVITY = json.get("vanilla_gravity").getAsDouble();
            if (json.has("display_position")) DISPLAY_POSITION = GravityDisplayPosition.valueOf(json.get("display_position").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("step", STEP);
            json.addProperty("reset_cooldown", RESET_COOLDOWN);
            json.addProperty("play_sounds", PLAY_SOUNDS);
            json.addProperty("min_gravity", MIN_GRAVITY);
            json.addProperty("max_gravity", MAX_GRAVITY);
            json.addProperty("vanilla_gravity", VANILLA_GRAVITY);
            json.addProperty("display_position", DISPLAY_POSITION.name());
            Files.writeString(CONFIG_PATH, GSON.toJson(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
