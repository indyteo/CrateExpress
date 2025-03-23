package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateRewardGiveEvent;
import fr.theoszanto.mc.crateexpress.models.reward.CrateRandomReward;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

public class RandomRewardsListener extends ExpressListener<CrateExpress> {
	public RandomRewardsListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	private void onCrateRewardGive(@NotNull CrateRewardGiveEvent event) {
		if (event.getReward() instanceof CrateRandomReward reward && reward.isRandom())
			event.setReward(reward.fixed());
	}
}
