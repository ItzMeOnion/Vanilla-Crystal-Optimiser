package com.example.papergame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standalone reconstruction of the Paper Game helper.
 * Configure the item IDs in config/papergame.json. This version provides
 * configurable mappings and a HUD reference; it does not claim to reproduce
 * the original mod's private dispenser-rendering mixins.
 */
public final class PaperGameClient implements ClientModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Integer, String> DEFAULTS = new LinkedHashMap<>();
    private static final Map<Integer, String> MAPPINGS = new LinkedHashMap<>();
    private static Path configPath;
    private static boolean hudEnabled = true;
    private static KeyBinding toggleHud;

    static {
        DEFAULTS.put(1, "minecraft:paper");
        DEFAULTS.put(2, "minecraft:map");
        DEFAULTS.put(3, "minecraft:book");
        DEFAULTS.put(4, "minecraft:feather");
        DEFAULTS.put(5, "minecraft:amethyst_shard");
        DEFAULTS.put(6, "minecraft:prismarine_shard");
        DEFAULTS.put(7, "minecraft:gold_nugget");
        DEFAULTS.put(8, "minecraft:iron_nugget");
        DEFAULTS.put(9, "minecraft:emerald");
        MAPPINGS.putAll(DEFAULTS);
    }

    @Override
    public void onInitializeClient() {
        configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/papergame.json");
        loadConfig();
        toggleHud = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.papergame.toggle_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                "category.papergame"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHud.wasPressed()) hudEnabled = !hudEnabled;
        });
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(MinecraftClient.getInstance(), drawContext));
    }

    private static void renderHud(MinecraftClient client, net.minecraft.client.gui.DrawContext ctx) {
        if (!hudEnabled || client.player == null || client.options.hudHidden) return;
        int x = 8, y = 8;
        ctx.fill(x - 4, y - 4, x + 190, y + 12 + MAPPINGS.size() * 12, 0x99000000);
        ctx.drawText(client.textRenderer, "Paper Game item map (O toggles)", x, y, 0xFFFFFF, false);
        y += 14;
        for (Map.Entry<Integer, String> entry : MAPPINGS.entrySet()) {
            String itemId = entry.getValue();
            String itemName = resolveName(itemId);
            ctx.drawText(client.textRenderer, entry.getKey() + " = " + itemName + "  [" + itemId + "]", x, y, 0xE8E8E8, false);
            y += 12;
        }
    }

    private static String resolveName(String id) {
        try {
            Item item = Registries.ITEM.get(Identifier.of(id));
            return item.getName().getString();
        } catch (Exception ignored) { return "Unknown item"; }
    }

    /** Returns the mapped number for a stack, or -1 if it isn't configured. */
    public static int getNumberForStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;
        String id = Registries.ITEM.getId(stack.getItem()).toString();
        for (Map.Entry<Integer, String> e : MAPPINGS.entrySet()) if (e.getValue().equals(id)) return e.getKey();
        return -1;
    }

    private static void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            if (!Files.exists(configPath)) { saveConfig(); return; }
            try (Reader reader = Files.newBufferedReader(configPath)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (root == null) return;
                if (root.has("hudEnabled")) hudEnabled = root.get("hudEnabled").getAsBoolean();
                if (root.has("numbers") && root.get("numbers").isJsonObject()) {
                    JsonObject numbers = root.getAsJsonObject("numbers");
                    for (int n = 1; n <= 9; n++) {
                        String key = Integer.toString(n);
                        if (numbers.has(key)) MAPPINGS.put(n, numbers.get(key).getAsString());
                    }
                }
            }
        } catch (Exception e) { System.err.println("[Paper Game] Could not load config: " + e.getMessage()); }
    }

    private static void saveConfig() throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("hudEnabled", hudEnabled);
        JsonObject numbers = new JsonObject();
        MAPPINGS.forEach((n, id) -> numbers.addProperty(Integer.toString(n), id));
        root.add("numbers", numbers);
        try (Writer writer = Files.newBufferedWriter(configPath)) { GSON.toJson(root, writer); }
    }
}
