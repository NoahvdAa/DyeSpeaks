package me.noahvdaa.dyespeaks;

import net.minecraft.sound.SoundEvent;

public class DyeSound {
	private final String trigger;
	private final SoundEvent soundEvent;

	public DyeSound(String trigger, SoundEvent soundEvent) {
		this.trigger = trigger;
		this.soundEvent = soundEvent;
	}

	public String getTrigger() {
		return this.trigger;
	}

	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}
}
