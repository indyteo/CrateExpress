package fr.theoszanto.mc.crateexpress.managers;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.commands.CrateExpressCommand;
import fr.theoszanto.mc.crateexpress.listeners.CrateListener;
import fr.theoszanto.mc.crateexpress.models.gui.CrateGUI;
import fr.theoszanto.mc.crateexpress.utils.JavaUtils;
import fr.theoszanto.mc.crateexpress.utils.PluginObject;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpigotManager extends PluginObject implements Listener {
	private final @NotNull Map<@NotNull Player, @NotNull CrateGUI> activeGUIs = new HashMap<>();
	private final @NotNull Map<@NotNull Player, @NotNull ChatRequest> pendingChatRequests = new HashMap<>();
	private boolean resetting = false;

	private static final @NotNull Timer timer = new Timer();

	public SpigotManager(@NotNull CrateExpress plugin) {
		super(plugin);
	}

	public void init() {
		// Commands
		PluginCommand command = this.plugin.getCommand("crate");
		if (command == null)
			throw new IllegalStateException("Crate command not found in plugin.yml");
		command.setExecutor(new CrateExpressCommand(this.plugin));

		// Listeners
		List<? extends CrateListener> listeners = JavaUtils.instanciateSubClasses(CrateListener.class, "fr.theoszanto.mc.crateexpress.listeners", this.plugin);
		PluginManager manager = this.plugin.getServer().getPluginManager();
		for (CrateListener listener : listeners)
			manager.registerEvents(listener, this.plugin);
		manager.registerEvents(this, this.plugin);
	}

	public void showGUI(@NotNull Player player, @NotNull CrateGUI gui) {
		if (this.resetting)
			return;
		player.openInventory(gui.getInventory());
		CrateGUI previous = this.activeGUIs.put(player, gui);
		gui.onOpen(player, previous);
	}

	@EventHandler
	private void onGUIClick(@NotNull InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (entity instanceof Player) {
			CrateGUI gui = this.activeGUIs.get(entity);
			if (gui != null && gui.getInventory().equals(event.getInventory())) {
				Player player = (Player) entity;
				InventoryType.SlotType slotType = event.getSlotType();
				ClickType click = event.getClick();
				InventoryAction action = event.getAction();
				if (slotType == InventoryType.SlotType.OUTSIDE) {
					if (gui.onClickOutside(player, click, action))
						event.setCancelled(true);
				} else {
					Inventory clickedInventory = event.getClickedInventory();
					int slot = event.getSlot();
					if (gui.getInventory().equals(clickedInventory)) {
						CrateGUI.SlotData data = gui.getSlotData(slot);
						if (data != null && data.getName().equals(CrateGUI.CLOSE)) {
							event.setCancelled(true);
							this.run(player::closeInventory);
						} else if (gui.onClick(player, click, action, data))
							event.setCancelled(true);
					} else if (clickedInventory != null)
						if (gui.onClickInOtherInventory(player, clickedInventory, click, action, slot))
							event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	private void onGUIDrag(@NotNull InventoryDragEvent event) {
		HumanEntity entity = event.getWhoClicked();
		if (entity instanceof Player) {
			CrateGUI gui = this.activeGUIs.get(entity);
			if (gui != null && gui.getInventory().equals(event.getInventory())) {
				boolean impactGUI = false;
				boolean impactInv = false;
				for (int slot : event.getRawSlots()) {
					if (gui.getInventory().equals(event.getView().getInventory(slot)))
						impactGUI = true;
					else
						impactInv = true;
				}
				if ((impactGUI && gui.preventDragInside()) || (impactInv && gui.preventDragOutside()))
					event.setCancelled(true);
			}
		}
	}

	@EventHandler
	private void onGUIClose(@NotNull InventoryCloseEvent event) {
		HumanEntity entity = event.getPlayer();
		if (entity instanceof Player) {
			CrateGUI gui = this.activeGUIs.get(entity);
			if (gui != null && gui.getInventory().equals(event.getInventory())) {
				this.activeGUIs.remove(entity, gui);
				gui.onClose((Player) entity);
			}
		}
	}

	@Contract(pure = true)
	public @NotNull CompletableFuture<@NotNull String> requestChatMessage(@NotNull Player player, long timeoutDelay, TimeUnit unit) {
		if (this.resetting)
			return JavaUtils.cancelledCompletableFuture();
		CompletableFuture<String> future = new CompletableFuture<>();
		TimerTask timeout = new TimerTask() {
			@Override
			public void run() {
				future.completeExceptionally(new TimeoutException());
				pendingChatRequests.remove(player);
			}
		};
		timer.schedule(timeout, unit.toMillis(timeoutDelay));
		ChatRequest request = new ChatRequest(future, timeout);
		this.pendingChatRequests.put(player, request);
		return future;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		ChatRequest request = this.pendingChatRequests.remove(player);
		if (request != null) {
			request.fulfill(event.getMessage());
			event.setCancelled(true);
			try {
				event.getRecipients().clear();
			} catch (Throwable ignored) {}
		}
	}

	public void reset() {
		this.resetting = true;
		this.pendingChatRequests.forEach(this::resetChatRequest);
		this.pendingChatRequests.clear();
		new ArrayList<>(this.activeGUIs.keySet()).forEach(this::resetGUI);
		this.activeGUIs.clear();
		HandlerList.unregisterAll(this.plugin);
		this.resetting = false;
	}

	private void resetGUI(@NotNull Player player) {
		player.closeInventory();
		this.i18nMessage(player, "misc.cancelled-by-reload");
	}

	private void resetChatRequest(@NotNull Player player, @NotNull ChatRequest request) {
		request.abort();
		this.i18nMessage(player, "misc.cancelled-by-reload");
	}

	private static final class ChatRequest {
		private final @NotNull CompletableFuture<@NotNull String> future;
		private final @NotNull TimerTask timeout;

		public ChatRequest(@NotNull CompletableFuture<@NotNull String> future, @NotNull TimerTask timeout) {
			this.future = future;
			this.timeout = timeout;
		}

		public void fulfill(@NotNull String message) {
			this.future.complete(message);
			this.timeout.cancel();
		}

		public void abort() {
			this.future.cancel(false);
			this.timeout.cancel();
		}
	}
}
