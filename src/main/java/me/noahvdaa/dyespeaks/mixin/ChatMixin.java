package me.noahvdaa.dyespeaks.mixin;

import me.noahvdaa.dyespeaks.DyeSound;
import me.noahvdaa.dyespeaks.DyeSpeaks;
import me.noahvdaa.dyespeaks.util.ServerCheckUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatHudListener.class)
public class ChatMixin {
	private static final Logger LOGGER = LogManager.getLogger();

	private long lastSpokenAt = 0;
	private DyeSound lastSpokenSound = null;
	// Ignore sound effects until this time. Set when chat is being rewound.
	private long ignoreUntil = 0L;

	@Inject(method = {"onChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V"}, at = {@At("HEAD")}, cancellable = true)
	public void postSay(MessageType type, Text textComponent, UUID uuid, CallbackInfo ci) {
		// Not on Dyescape.
		if (!ServerCheckUtil.isOnDyescape()) {
			return;
		}

		// Chat is still being rewound, ignore message.
		if (System.currentTimeMillis() < ignoreUntil) {
			LOGGER.info("Ignoring message, chat is being rewound!");
			return;
		}

		// Get clean version of message.
		String message = textComponent.getString().replaceAll("\n", "").trim().replaceAll(" +", " ");

		if (message.equals("")) {
			// Chat is being rewound! Ignore all incoming messages for the next 50ms.
			LOGGER.info("Chat rewind detected, all messages will be ignored for the next 50ms!");
			ignoreUntil = System.currentTimeMillis() + 50L;

			// Dialogue has ended, it's important that we make sure to reset this.
			lastSpokenAt = 0;
			lastSpokenSound = null;
			return;
		}

		for (DyeSound sound : DyeSpeaks.getInstance().sounds) {
			if (message.matches(sound.getTrigger())) {
				if (lastSpokenSound == sound && lastSpokenAt > System.currentTimeMillis() - 10000) {
					LOGGER.info("Found a match, but exact sound was already played <10 sec ago! Skipping: " + sound.getSoundEvent().getId());
					lastSpokenAt = System.currentTimeMillis();
					return;
				}
				// Stop previous sound so sounds aren't playing at the same time.
				if (lastSpokenSound != null) {
					MinecraftClient.getInstance().getSoundManager().stopSounds(lastSpokenSound.getSoundEvent().getId(), SoundCategory.VOICE);
				}

				LOGGER.info("Found a match! Playing: " + sound.getSoundEvent().getId());
				MinecraftClient.getInstance().player.playSound(sound.getSoundEvent(), SoundCategory.VOICE, 1f, 1f);

				lastSpokenAt = System.currentTimeMillis();
				lastSpokenSound = sound;
				// Return so we don't play two sound effects at once.
				return;
			}
		}

		// A line hasn't matched, so we can safely reset this.
		lastSpokenAt = 0;
	}
}
