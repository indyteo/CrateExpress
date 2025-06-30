package fr.theoszanto.mc.crateexpress.listeners;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.events.CrateRewardGiveEvent;
import fr.theoszanto.mc.crateexpress.models.reward.CrateMoneyReward;
import fr.theoszanto.mc.express.listeners.ExpressListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class RoundMoneyRewardsListener extends ExpressListener<CrateExpress> {
	public RoundMoneyRewardsListener(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	@EventHandler(ignoreCancelled = true)
	private void onCrateRewardGive(@NotNull CrateRewardGiveEvent event) {
		if (event.getReward() instanceof CrateMoneyReward reward && this.plugin.money().isRound())
			event.setReward(new CrateMoneyReward(this.plugin, reward.getId(), reward.getWeight(), Math.round(reward.getAmount())));
	}
}
