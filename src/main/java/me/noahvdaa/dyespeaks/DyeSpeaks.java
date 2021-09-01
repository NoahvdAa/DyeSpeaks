package me.noahvdaa.dyespeaks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DyeSpeaks implements ClientModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();
	private static DyeSpeaks instance;
	public List<DyeSound> sounds = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		instance = this;

		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("dyespeaks", "");
			}

			@Override
			public void reload(ResourceManager manager) {
				JsonParser parser = new JsonParser();
				JsonObject parsedSounds;

				try {
					// This feels a bit hacky, is there another way of doing this?
					InputStream soundsStream = manager.getResource(new Identifier("dyespeaks:sounds.json")).getInputStream();
					String soundsText = IOUtils.toString(soundsStream, StandardCharsets.UTF_8.name());
					parsedSounds = parser.parse(soundsText).getAsJsonObject();
				} catch (IOException e) {
					// Halt mod loading as we need sounds.json.
					LOGGER.error("Failed to load sounds.json file for DyeSpeaks:");
					e.printStackTrace();
					return;
				}

				List<DyeSound> sounds = new ArrayList<>();

				for (Map.Entry<String, JsonElement> sound : parsedSounds.entrySet()) {
					Identifier identifier = new Identifier("dyespeaks:" + sound.getKey());
					SoundEvent event = new SoundEvent(identifier);
					String trigger = sound.getValue().getAsJsonObject().get("trigger").getAsString();
					DyeSound processedSound = new DyeSound(trigger, event);

					LOGGER.info("Loaded sound " + event.getId() + " with trigger '" + trigger + "'.");

					sounds.add(processedSound);
				}

				// Just for safety, make sure the list is unmodifiable.
				DyeSpeaks.getInstance().sounds = Collections.unmodifiableList(sounds);
			}
		});
	}

	public static DyeSpeaks getInstance() {
		return instance;
	}
}
