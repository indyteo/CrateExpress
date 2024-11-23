package fr.theoszanto.mc.crateexpress.models.gui;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.CrateKey;
import fr.theoszanto.mc.crateexpress.models.reward.HistoricalReward;
import fr.theoszanto.mc.crateexpress.utils.CratePermission;
import fr.theoszanto.mc.crateexpress.utils.Pair;
import fr.theoszanto.mc.crateexpress.utils.TimeUtils;
import fr.theoszanto.mc.express.gui.ExpressGUI;
import fr.theoszanto.mc.express.utils.ItemBuilder;
import fr.theoszanto.mc.express.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CrateHistoryGUI extends ExpressGUI<CrateExpress> {
	private final @NotNull OfflinePlayer player;
	private @NotNull Date date;
	private @Nullable Map<@NotNull Crate, @NotNull List<@NotNull HistoricalReward>> history;
	private @Nullable CompletableFuture<?> future;
	private boolean error;
	private boolean shouldFetchData;

	private boolean opened;
	private int vScroll;
	private int vMax;
	private int[] hScrolls;
	private int[] hMaxes;

	private final @NotNull Date today;
	private final @Nullable Date limit;

	private static final int[] BORDERS = {
			  0,  1,  2,  3,/**/  5,  6,  7,  8,
			/**/ 10,/**//**//**//**//**//**/ 17,
			/**/ 19,/**//**//**//**//**//**/ 26,
			/**/ 28,/**//**//**//**//**//**/ 35,
			/**/ 37,/**//**//**//**//**//**/ 44,
			 45, 46, 47, 48,/**/ 50, 51, 52,/**/
	};

	private static final long MAX_DAYS_IN_PAST = 30;

	private static final @NotNull SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final @NotNull SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public CrateHistoryGUI(@NotNull CrateExpress plugin, @NotNull OfflinePlayer player, @NotNull Date date, boolean unlimited) {
		super(plugin, 6, "menu.history.title");
		this.player = player;
		this.date = date;
		this.today = TimeUtils.today();
		this.limit = unlimited ? null : TimeUtils.add(this.today, -MAX_DAYS_IN_PAST, TimeUnit.DAYS);
		this.shouldFetchData = true;
	}

	private void fetchData(@NotNull Player viewer) {
		this.history = null;
		this.error = false;
		this.cancelDataFetching();
		this.future = this.plugin.stats().loadHistory(this.player, this.date).whenComplete((history, error) -> {
			this.future = null;
			this.history = history;
			this.error = error != null;
			if (!this.error) {
				boolean hideNoPreview = !viewer.hasPermission(CratePermission.BYPASS_NO_PREVIEW);
				int size = this.history.size();
				if (hideNoPreview)
					size -= (int) this.history.keySet().stream().filter(Crate::isNoPreview).count();
				this.vScroll = 0;
				this.vMax = Math.max(0, size - 4);
				this.hScrolls = new int[size];
				this.hMaxes = new int[size];
				int i = 0;
				for (Map.Entry<Crate, List<HistoricalReward>> entry : this.history.entrySet()) {
					if (hideNoPreview && entry.getKey().isNoPreview())
						continue;
					this.hMaxes[i++] = Math.max(0, computeSpacedWidth(entry.getValue()) - 6);
				}
			}
			if (this.opened)
				this.refresh(viewer);
		});
	}

	private void cancelDataFetching() {
		if (this.future != null && !this.future.isCancelled()) {
			this.future.cancel(true);
			this.future = null;
		}
	}

	@Override
	public void onOpen(@NotNull Player player, @Nullable ExpressGUI<CrateExpress> ignored) {
		this.opened = true;
		if (this.shouldFetchData) {
			this.fetchData(player);
			this.shouldFetchData = false;
		}

		boolean self = player.getUniqueId().equals(this.player.getUniqueId());

		// Borders
		for (int s = 0; s < 54; s++)
			this.set(s, BORDER);
		ItemStack borderDark = ItemUtils.hideTooltip(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
		for (int s : BORDERS)
			this.set(s, borderDark);

		// Previous day button
		if (this.limit == null || TimeUtils.compareIgnoringTime(this.date, this.limit) > 0) {
			Date previous = TimeUtils.add(this.date, -1, TimeUnit.DAYS);
			this.set(slot(0, 2), new ItemBuilder(Material.SPECTRAL_ARROW, 1, this.i18n("menu.history.day.previous.name"), this.i18nLines("menu.history.day.previous.lore", "date", DATE_FORMAT.format(previous))), "day", previous);
		}

		// Header
		this.set(slot(0, 4), new ItemBuilder(Material.FILLED_MAP, 1, this.i18n("menu.history.header.name"), this.i18nLines("menu.history.header.lore." + (self ? "self" : "other"),
				"player", this.player.getName(),
				"date", DATE_FORMAT.format(this.date),
				"crates", this.history == null ? 0 : this.history.size(),
				"rewards", this.history == null ? 0 : this.history.values().stream().mapToInt(List::size).sum())).addFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));

		// Next day button
		if (TimeUtils.compareIgnoringTime(this.date, this.today) < 0) {
			Date next = TimeUtils.add(this.date, 1, TimeUnit.DAYS);
			this.set(slot(0, 6), new ItemBuilder(Material.SPECTRAL_ARROW, 1, this.i18n("menu.history.day.next.name"), this.i18nLines("menu.history.day.next.lore", "date", DATE_FORMAT.format(next))), "day", next);
		}

		// Today button
		if (TimeUtils.compareIgnoringTime(this.date, this.today) != 0)
			this.set(slot(0, 8), new ItemBuilder(Material.CLOCK, 1, this.i18n("menu.history.day.today.name"), this.i18nLines("menu.history.day.today.lore", "date", DATE_FORMAT.format(this.today))), "day", this.today);

		// Close button
		this.setCloseButton(slot(5, 4));

		if (self)
			// Help text
			this.set(slot(5, 8), new ItemBuilder(Material.WRITTEN_BOOK, 1, this.i18n("menu.history.help.name"), this.i18nLines("menu.history.help.lore")).addFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));
		else {
			// Player indicator
			ItemStack head = new ItemBuilder(Material.PLAYER_HEAD, 1, this.i18n("menu.history.player.name", "player", this.player.getName()), this.i18nLines("menu.history.player.lore")).build();
			ItemMeta meta = head.getItemMeta();
			if (meta instanceof SkullMeta)
				((SkullMeta) meta).setOwningPlayer(this.player);
			head.setItemMeta(meta);
			this.set(slot(5, 8), head);
		}

		// Content
		if (this.error)
			// Error indicator / Retry button
			this.set(slot(2, 4), new ItemBuilder(Material.TNT, 1, this.i18n("menu.history.error.name"), this.i18nLines("menu.history.error.lore")), "day", this.date);
		else if (this.history == null)
			// Loading indicator
			this.set(slot(2, 4), new ItemBuilder(Material.STRUCTURE_BLOCK, 1, this.i18n("menu.history.loading")));
		else if (this.history.isEmpty())
			// Empty indicator
			this.set(slot(2, 4), new ItemBuilder(Material.STRUCTURE_VOID, 1, this.i18n("menu.history.empty")));
		else {
			// Scroll up
			if (this.vScroll > 0)
				this.set(slot(0, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.history.scroll.up")), "vScroll", this.vScroll - 1);

			boolean hideNoPreview = !player.hasPermission(CratePermission.BYPASS_NO_PREVIEW);
			Iterator<Map.Entry<Crate, List<HistoricalReward>>> iterator = this.history.entrySet().iterator();
			for (int i = 0; i < this.vScroll && iterator.hasNext(); i++) {
				Map.Entry<Crate, List<HistoricalReward>> entry = iterator.next();
				if (hideNoPreview && entry.getKey().isNoPreview())
					i--;
			}
			for (int i = 0; i < 4 && iterator.hasNext(); i++) {
				Map.Entry<Crate, List<HistoricalReward>> entry = iterator.next();
				Crate crate = entry.getKey();
				if (hideNoPreview && entry.getKey().isNoPreview()) {
					i--;
					continue;
				}
				List<HistoricalReward> rewards = entry.getValue();

				int row = 1 + i;
				int offset = this.vScroll + i;
				int hScroll = this.hScrolls[offset];
				int hMax = this.hMaxes[offset];

				CrateKey key = crate.getKey();
				ItemStack item;
				if (key == null)
					item = new ItemBuilder(Material.CHEST, 1, crate.getName()).build();
				else
					item = key.getItem().clone();
				ItemUtils.addLore(item, this.i18nLines("menu.history.crate", "count", rewards.size(), "time", TIME_FORMAT.format(rewards.getFirst().date())));
				this.set(slot(row, 0), item);

				// Scroll left
				if (hScroll > 0)
					this.set(slot(row, 1), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.history.scroll.left")), "hScroll", new Pair<>(offset, hScroll - 1));

				List<HistoricalReward> spacedRewards = new ArrayList<>(hMax + 6);
				for (int j = 0; j < rewards.size(); j++) {
					HistoricalReward reward = rewards.get(j);
					if (j > 0 && !reward.date().equals(rewards.get(j - 1).date()))
						spacedRewards.add(null);
					spacedRewards.add(reward);
				}
				double crateWeight = crate.totalWeight();
				for (int j = 0; j < 6 && hScroll + j < spacedRewards.size(); j++) {
					HistoricalReward reward = spacedRewards.get(hScroll + j);
					ItemStack icon;
					if (reward == null)
						icon = BORDER;
					else {
						icon = reward.reward().getIconWithChance(crateWeight);
						ItemUtils.addLore(icon, this.i18nLines("menu.history.reward", "time", TIME_FORMAT.format(reward.date())));
					}
					this.set(slot(row, 2 + j), icon);
				}

				// Scroll right
				if (hScroll < hMax)
					this.set(slot(row, 8), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.history.scroll.right")), "hScroll", new Pair<>(offset, hScroll + 1));
			}

			// Scroll down
			if (this.vScroll < this.vMax)
				this.set(slot(5, 0), new ItemBuilder(Material.ARROW, 1, this.i18n("menu.history.scroll.down")), "vScroll", this.vScroll + 1);
		}
	}

	@Override
	public boolean onClick(@NotNull Player player, @NotNull ClickType click, @NotNull InventoryAction action, @Nullable SlotData data) {
		if (data == null)
			return true;
		switch (data.getName()) {
		case "day":
			this.date = data.getUserData();
			this.fetchData(player);
			break;
		case "vScroll":
			this.vScroll = data.getUserData();
			this.refresh(player);
			break;
		case "hScroll":
			Pair<Integer, Integer> pair = data.getUserData();
			this.hScrolls[pair.first()] = pair.second();
			this.refresh(player);
			break;
		}
		return true;
	}

	@Override
	public void onClose(@NotNull Player player) {
		this.cancelDataFetching();
		this.opened = false;
	}

	private static int computeSpacedWidth(@NotNull List<@NotNull HistoricalReward> rewards) {
		int n = 0;
		for (int j = 0; j < rewards.size(); j++, n++)
			if (j > 0 && !rewards.get(j).date().equals(rewards.get(j - 1).date()))
				n++;
		return n;
	}
}
