package ic2.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResourceAndModelVerificationTest {

    private static final Path RESOURCES_PATH = Paths.get("src", "main", "resources");
    private static final Path ASSETS_PATH = RESOURCES_PATH.resolve(Paths.get("assets", "ic2"));
    private static final Gson GSON = new Gson();

    @Test
    @DisplayName("Verify all texture_package.json references point to existing png files")
    public void testTexturePackagesIntegrity() throws Exception {
        Assertions.assertTrue(Files.exists(ASSETS_PATH), "Assets path must exist");
        List<Path> texturePackages = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(ASSETS_PATH)) {
            stream.filter(p -> p.getFileName().toString().equals("texture_package.json"))
                  .forEach(texturePackages::add);
        }

        Assertions.assertFalse(texturePackages.isEmpty(), "texture_package.json files should be present");

        List<String> missingFiles = new ArrayList<>();
        for (Path pkg : texturePackages) {
            try (FileReader reader = new FileReader(pkg.toFile())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                for (String key : json.keySet()) {
                    String ref = json.get(key).getAsString();
                    String path = ref;
                    String namespace = "ic2";
                    int colon = ref.indexOf(':');
                    if (colon >= 0) {
                        namespace = ref.substring(0, colon);
                        path = ref.substring(colon + 1);
                    }
                    Path expectedPng = RESOURCES_PATH.resolve("assets").resolve(namespace).resolve("textures").resolve(path + ".png");
                    if (!Files.exists(expectedPng)) {
                        missingFiles.add(pkg.toString() + " -> " + ref + " (" + expectedPng + ")");
                    }
                }
            }
        }

        Assertions.assertTrue(missingFiles.isEmpty(), "Missing textures referenced in texture packages: " + missingFiles);
    }

    @Test
    @DisplayName("Verify block atlas includes IC2 directories")
    public void testBlockAtlasConfiguration() throws Exception {
        Path atlasPath = RESOURCES_PATH.resolve(Paths.get("assets", "minecraft", "atlases", "blocks.json"));
        Assertions.assertTrue(Files.exists(atlasPath), "assets/minecraft/atlases/blocks.json must exist");

        try (FileReader reader = new FileReader(atlasPath.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            Assertions.assertTrue(json.has("sources"), "Atlas JSON must have 'sources'");
            JsonArray sources = json.getAsJsonArray("sources");

            boolean hasModels = false;
            boolean hasGuiSprites = false;
            boolean hasMobEffect = false;
            boolean hasMisc = false;

            for (JsonElement el : sources) {
                JsonObject obj = el.getAsJsonObject();
                String src = obj.get("source").getAsString();
                if ("models".equals(src)) hasModels = true;
                if ("gui_sprites".equals(src)) hasGuiSprites = true;
                if ("mob_effect".equals(src)) hasMobEffect = true;
                if ("misc".equals(src)) hasMisc = true;
            }

            Assertions.assertTrue(hasModels, "Atlas must include models directory");
            Assertions.assertTrue(hasGuiSprites, "Atlas must include gui_sprites directory");
            Assertions.assertTrue(hasMobEffect, "Atlas must include mob_effect directory");
            Assertions.assertTrue(hasMisc, "Atlas must include misc directory");
        }

        Path rotorAtlas = RESOURCES_PATH.resolve(Paths.get("assets", "ic2", "atlases", "rotor_blades.json"));
        if (Files.exists(rotorAtlas)) {
            try (FileReader reader = new FileReader(rotorAtlas.toFile())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                JsonArray sources = json.getAsJsonArray("sources");
                for (JsonElement el : sources) {
                    String src = el.getAsJsonObject().get("source").getAsString();
                    Assertions.assertFalse(src.contains(":"), "Atlas source must be relative path without namespace prefix: " + src);
                }
            }
        }
    }

    @Test
    @DisplayName("Verify energy storage GUI textures exist")
    public void testEnergyStorageTextures() {
        Path storageDir = ASSETS_PATH.resolve(Paths.get("textures", "gui_sprites", "blocks", "storage"));
        Assertions.assertTrue(Files.exists(storageDir), "Storage GUI textures dir must exist");

        String[] requiredTextures = {
            "gui_electric_block.png",
            "gui_batbox.png",
            "gui_mfe.png",
            "gui_mfsu.png",
            "gui_esu.png",
            "gui_isu.png",
            "gui_pesu.png"
        };

        for (String tex : requiredTextures) {
            Path texPath = storageDir.resolve(tex);
            Assertions.assertTrue(Files.exists(texPath), "Storage GUI texture missing: " + tex);
        }
    }

    @Test
    @DisplayName("Verify windmill rotor textures exist")
    public void testWindmillRotorTextures() {
        Path rotorDir = ASSETS_PATH.resolve(Paths.get("textures", "models", "windmill"));
        Assertions.assertTrue(Files.exists(rotorDir), "Windmill textures dir must exist");

        String[] requiredRotors = {
            "wood_rotor.png",
            "wool_rotor.png",
            "iron_rotor.png",
            "alloy_rotor.png",
            "carbon_rotor.png",
            "iridium_rotor.png"
        };

        for (String r : requiredRotors) {
            Path p = rotorDir.resolve(r);
            Assertions.assertTrue(Files.exists(p), "Windmill rotor texture missing: " + r);
        }
    }

    @Test
    @DisplayName("Verify rubber tree feature resources exist")
    public void testRubberTreeResources() {
        Path woodDir = ASSETS_PATH.resolve(Paths.get("textures", "block", "resources", "rubberwood"));
        Assertions.assertTrue(Files.exists(woodDir), "Rubber wood texture dir must exist");

        Assertions.assertTrue(Files.exists(woodDir.resolve("log_sides.png")), "log_sides.png missing");
        Assertions.assertTrue(Files.exists(woodDir.resolve("log_top.png")), "log_top.png missing");
        Assertions.assertTrue(Files.exists(woodDir.resolve("log_resin_empty.png")), "log_resin_empty.png missing");
        Assertions.assertTrue(Files.exists(woodDir.resolve("log_resin_full.png")), "log_resin_full.png missing");
        Assertions.assertTrue(Files.exists(woodDir.resolve("sapling.png")), "sapling.png missing");
        Assertions.assertTrue(Files.exists(woodDir.resolve("planks.png")), "planks.png missing");
    }

    @Test
    @DisplayName("Verify mixin configuration contains BlockModelShaperMixin")
    public void testMixinConfiguration() throws Exception {
        Path mixinConfig = RESOURCES_PATH.resolve("ic2.mixins.json");
        Assertions.assertTrue(Files.exists(mixinConfig), "ic2.mixins.json must exist");

        try (FileReader reader = new FileReader(mixinConfig.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            Assertions.assertTrue(json.has("client"), "Mixins JSON must have 'client'");
            JsonArray clientMixins = json.getAsJsonArray("client");

            boolean foundAccessor = false;
            boolean foundMixin = false;

            for (JsonElement el : clientMixins) {
                String name = el.getAsString();
                if ("client.BlockModelShaperAccessor".equals(name)) foundAccessor = true;
                if ("client.BlockModelShaperMixin".equals(name)) foundMixin = true;
            }

            Assertions.assertTrue(foundAccessor, "BlockModelShaperAccessor must be registered in ic2.mixins.json");
            Assertions.assertTrue(foundMixin, "BlockModelShaperMixin must be registered in ic2.mixins.json");
        }
    }

    @Test
    @DisplayName("Verify IC2 creative mode tab background location contracts")
    public void testCreativeTabBackgroundContract() {
        Assertions.assertNotNull(ic2.core.utils.IC2ItemGroup.DEFAULT_BACKGROUND);
        Assertions.assertNotNull(ic2.core.utils.IC2ItemGroup.DEFAULT_TABS_IMAGE);
        Assertions.assertEquals("textures/gui/container/creative_inventory/tab_items.png", ic2.core.utils.IC2ItemGroup.DEFAULT_BACKGROUND.getPath());
        Assertions.assertEquals("textures/gui/container/creative_inventory/tabs.png", ic2.core.utils.IC2ItemGroup.DEFAULT_TABS_IMAGE.getPath());
    }
}
