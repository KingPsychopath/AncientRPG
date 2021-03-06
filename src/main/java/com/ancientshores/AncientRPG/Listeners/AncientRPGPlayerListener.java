package com.ancientshores.AncientRPG.Listeners;

import com.ancientshores.AncientRPG.AncientRPG;
import com.ancientshores.AncientRPG.Classes.AncientRPGClass;
import com.ancientshores.AncientRPG.Classes.BindingData;
import com.ancientshores.AncientRPG.Classes.Commands.ClassCastCommand;
import com.ancientshores.AncientRPG.Classes.Commands.ClassResetCommand;
import com.ancientshores.AncientRPG.Guild.AncientRPGGuild;
import com.ancientshores.AncientRPG.HP.DamageConverter;
import com.ancientshores.AncientRPG.Party.AncientRPGParty;
import com.ancientshores.AncientRPG.PlayerData;
import com.ancientshores.AncientRPG.Race.AncientRPGRace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AncientRPGPlayerListener implements Listener {
    public static final Collection<Player> toggleguildlist = Collections.newSetFromMap(
            new ConcurrentHashMap<Player, Boolean>());
    public static final Collection<Player> togglepartylist = Collections.newSetFromMap(
            new ConcurrentHashMap<Player, Boolean>());
    public static final HashMap<Player, Integer> invisibleList = new HashMap<Player, Integer>();
    public static final HashMap<Entity, Player> summonedCreatures = new HashMap<Entity, Player>();
    public static EventPriority guildSpawnPriority = EventPriority.HIGHEST;
    public static EventPriority raceSpawnPriority = EventPriority.HIGHEST;
    public static AncientRPG plugin;
    public static int invisId = 0;

    public AncientRPGPlayerListener(AncientRPG instance) {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static int addInvis(Player p) {
        invisibleList.put(p, invisId);
        int oldinvisid = invisId;
        invisId++;
        if (invisId > Integer.MAX_VALUE / 2) {
            invisId = 0;
        }
        return oldinvisid;
    }

    public static void setAllVisible(Player p) {
        for (Player pa : invisibleList.keySet()) {
            p.showPlayer(pa);
        }
    }

    public static void setVisibleToAll(Player p) {
        for (Player pa : AncientRPG.plugin.getServer().getOnlinePlayers()) {
            pa.showPlayer(p);
        }
    }

    public static boolean removeInvis(Player p, int id) {
        if (invisibleList.get(p) != null && invisibleList.get(p) == id) {
            invisibleList.remove(p);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        PlayerData pd = PlayerData.getPlayerData(event.getPlayer().getName());
        AncientRPGClass c = AncientRPGClass.classList.get(pd.getClassName().toLowerCase());
        if (c != null) {
            AncientRPGClass stance = c.stances.get(pd.getStance());
            if (!c.isWorldEnabled(event.getPlayer())) {
                AncientRPGClass oldClass = AncientRPGClass.classList.get(pd.getClassName().toLowerCase());
                ClassResetCommand.reset(event.getPlayer(), oldClass, pd);
            }
            if (stance != null && !stance.isWorldEnabled(event.getPlayer())) {
                pd.setStance("");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnect(PlayerJoinEvent event) {
        for (Player p : invisibleList.keySet()) {
            event.getPlayer().hidePlayer(p);
        }
        AncientRPGGuild.setTag(event.getPlayer());
        AncientRPGClass mClass = AncientRPGClass.classList.get(
                PlayerData.getPlayerData(event.getPlayer().getName()).getClassName().toLowerCase());
        if (mClass != null && mClass.permGroup != null && !mClass.permGroup.equals("")) {
            if (AncientRPG.permissionHandler != null) {
                try {
                    AncientRPG.permissionHandler.playerAddGroup(event.getPlayer(), mClass.permGroup);
                    for (Map.Entry<String, AncientRPGClass> entry : mClass.stances.entrySet()) {
                        try {
                            AncientRPG.permissionHandler.playerAddGroup(event.getPlayer(), entry.getValue().permGroup);
                        } catch (Exception ignored) {

                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
        PlayerData.getPlayerData(event.getPlayer().getName()).getHpsystem().player = event.getPlayer();
        PlayerData.getPlayerData(event.getPlayer().getName()).getXpSystem().addXP(0, false);
        PlayerData.getPlayerData(event.getPlayer().getName()).getHpsystem().setMaxHp();
        /*
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("ScoreboardAPI");
        if (p != null) {
            ScoreboardInterface.showScoreboard(event.getPlayer());
        }
        */
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        setVisibleToAll(event.getPlayer());
        setAllVisible(event.getPlayer());
        AncientRPGClass mClass = AncientRPGClass.classList.get(
                PlayerData.getPlayerData(event.getPlayer().getName()).getClassName().toLowerCase());
        if (mClass == null) {
            ClassResetCommand.reset(event.getPlayer(), null, PlayerData.getPlayerData(event.getPlayer().getName()));
        } else if (mClass.permGroup != null && !mClass.permGroup.equals("")) {
            if (AncientRPG.permissionHandler != null) {
                try {
                    AncientRPG.permissionHandler.playerRemoveGroup(event.getPlayer(), mClass.permGroup);
                    for (Map.Entry<String, AncientRPGClass> entry : mClass.stances.entrySet()) {
                        AncientRPG.permissionHandler.playerRemoveGroup(event.getPlayer(), entry.getValue().permGroup);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (summonedCreatures.containsValue(event.getPlayer())) {
            HashSet<Entity> removeentity = new HashSet<Entity>();
            for (Entity e : summonedCreatures.keySet()) {
                if (summonedCreatures.get(e).equals(event.getPlayer())) {
                    e.remove();
                    removeentity.add(e);
                }
            }
            for (Entity e : removeentity) {
                summonedCreatures.remove(e);
            }
        }
        PlayerData pd = PlayerData.getPlayerData(event.getPlayer().getName());
        PlayerData.playerData.remove(pd);
        pd.save();
        pd.dispose();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player mPlayer = event.getPlayer();
        PlayerData pd = PlayerData.getPlayerData(mPlayer.getName());
        AncientRPGRace mRace = AncientRPGRace.getRaceByName(pd.getRacename());
        if (mRace != null) {
            if (mRace.spawnLoc != null) {
                event.setRespawnLocation(mRace.spawnLoc.toLocation());
            }
        }
        AncientRPGGuild mGuild = AncientRPGGuild.getPlayersGuild(event.getPlayer().getName());
        if (mGuild != null) {
            if (AncientRPGGuild.spawnEnabled && mGuild.spawnLocation != null) {
                event.setRespawnLocation(mGuild.spawnLocation.toLocation());
            }
        }
        pd.getHpsystem().health = pd.getHpsystem().maxhp;
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        if (event.getInventory().getHolder() instanceof Player) {
            Player p = (Player) event.getInventory().getHolder();
            AncientRPGClass mClass = AncientRPGClass.classList.get(
                    PlayerData.getPlayerData(p.getName()).getClassName().toLowerCase());
            if (mClass == null) {
                return;
            }
            if (!mClass.isWorldEnabled(p.getWorld())) {
                return;
            }
            for (ItemStack is : p.getInventory().getArmorContents()) {
                boolean canEquip = !mClass.blacklistedArmor.contains(is.getType());
                if (!canEquip) {
                    if (is.equals(p.getInventory().getBoots())) {
                        p.getInventory().setBoots(null);
                    }
                    if (is.equals(p.getInventory().getChestplate())) {
                        p.getInventory().setChestplate(null);
                    }
                    if (is.equals(p.getInventory().getLeggings())) {
                        p.getInventory().setLeggings(null);
                    }
                    if (is.equals(p.getInventory().getHelmet())) {
                        p.getInventory().setHelmet(null);
                    }
                    p.getInventory().addItem(is);
                    p.sendMessage("Your class can't equip this item");
                }
            }
        }

        Material m;
        int newslot = event.getPlayer().getInventory().getHeldItemSlot();
        try {
            m = event.getPlayer().getInventory().getItemInHand().getType();
        } catch (Exception e) {
            return;
        }
        Player p = (Player) event.getPlayer();
        AncientRPGClass mClass = AncientRPGClass.classList.get(
                PlayerData.getPlayerData(p.getName()).getClassName().toLowerCase());
        if (mClass == null) {
            return;
        }
        if (!mClass.isWorldEnabled(p.getWorld())) {
            return;
        }
        if (mClass.blacklistedMats.contains(m)) {
            ItemStack oldStack = event.getPlayer().getInventory().getItemInHand();
            int fslot = event.getPlayer().getInventory().firstEmpty();
            if (fslot == -1) {
                event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), oldStack);
            } else {
                event.getPlayer().getInventory().setItem(fslot, oldStack);
            }
            if (newslot != -1) {
                event.getPlayer().getInventory().clear(newslot);
            }
            ((CommandSender) event.getPlayer()).sendMessage("Your class can't use this item");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
        AncientRPGClass mClass = AncientRPGClass.classList.get(
                PlayerData.getPlayerData(event.getPlayer().getName()).getClassName().toLowerCase());
        int free = 0;
        for (ItemStack s : event.getPlayer().getInventory().getContents()) {
            if (s == null) {
                free++;
            }
        }
        if (free < 2) {
            if (mClass != null && mClass.isWorldEnabled(event.getPlayer())) {
                if (mClass.blacklistedMats.contains(event.getItem().getItemStack().getType())) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        AncientRPG.plugin.getServer().getScheduler().scheduleSyncDelayedTask(AncientRPG.plugin, new Runnable() {
            public void run() {
                Player p = event.getPlayer();
                AncientRPGClass mClass = AncientRPGClass.classList.get(
                        PlayerData.getPlayerData(p.getName()).getClassName().toLowerCase());
                if (mClass == null) {
                    return;
                }
                if (!mClass.isWorldEnabled(p.getWorld())) {
                    return;
                }
                Material m;
                int newslot = event.getPlayer().getInventory().getHeldItemSlot();
                try {
                    m = event.getPlayer().getInventory().getItemInHand().getType();
                } catch (Exception e) {
                    return;
                }
                if (mClass.blacklistedMats.contains(m)) {
                    ItemStack oldStack = event.getPlayer().getInventory().getItemInHand();
                    event.getPlayer().getInventory().setItem(event.getPlayer().getInventory().firstEmpty(), oldStack);
                    if (newslot != -1) {
                        event.getPlayer().getInventory().clear(newslot);
                    }
                    event.getPlayer().sendMessage("Your class can't equip use item");
                }
            }
        });
    }

    @EventHandler
    public void onPlayerItemHeld(final PlayerItemHeldEvent event) {
        PlayerData pd = PlayerData.getPlayerData(event.getPlayer().getName());
        if (pd.getBindings() != null && pd.getBindings().size() >= 1 && event.getPlayer().getInventory().getItem(
                event.getNewSlot()) != null && event.getPlayer().getInventory().getItem(
                event.getNewSlot()).getType() != Material.AIR) {
            if (pd.getBindings().containsKey(
                    new BindingData(event.getPlayer().getInventory().getItem(event.getNewSlot())))) {
                event.getPlayer().sendMessage("This item is bound to the spell: " + pd.getBindings().get(
                        new BindingData(event.getPlayer().getInventory().getItem(event.getNewSlot()))));
            }
        }
        if (pd.getSlotbinds() != null && pd.getSlotbinds().size() >= 1) {
            if (pd.getSlotbinds().containsKey(event.getNewSlot())) {
                event.getPlayer().sendMessage(
                        "This slot is bound to the spell: " + pd.getSlotbinds().get(event.getNewSlot()));
            }
        }
        if (event.getPlayer() != null) {
            Player p = event.getPlayer();
            AncientRPGClass mClass = AncientRPGClass.classList.get(
                    PlayerData.getPlayerData(p.getName()).getClassName().toLowerCase());
            if (mClass == null) {
                return;
            }
            if (!mClass.isWorldEnabled(p.getWorld())) {
                return;
            }
            Material m;
            int newslot = event.getNewSlot();
            try {
                m = event.getPlayer().getInventory().getItem(newslot).getType();
                if (mClass.blacklistedMats.contains(m)) {
                    ItemStack oldStack = event.getPlayer().getInventory().getItem(newslot);
                    int fslot = event.getPlayer().getInventory().firstEmpty();
                    if (fslot == -1) {
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), oldStack);
                    } else {
                        event.getPlayer().getInventory().setItem(fslot, oldStack);
                    }
                    if (newslot != -1) {
                        event.getPlayer().getInventory().clear(newslot);
                    }
                    event.getPlayer().sendMessage("Your class can't equip use item");
                }
            } catch (Exception ignored) {
            }
        }
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith("/")) {
            if (toggleguildlist.contains(event.getPlayer())) {
                AncientRPGGuild mGuild = AncientRPGGuild.getPlayersGuild(event.getPlayer().getName());
                if (mGuild != null) {
                    mGuild.sendMessage(event.getMessage(), event.getPlayer());
                    event.setCancelled(true);
                }
            }
            if (togglepartylist.contains(event.getPlayer())) {
                AncientRPGParty mParty = AncientRPGParty.getPlayersParty(event.getPlayer());
                if (mParty != null) {
                    String[] s = {event.getMessage()};
                    mParty.sendMessage(s, event.getPlayer());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (AncientRPGEntityListener.StunList.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public static boolean damageignored = false;

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getDamage() == Integer.MAX_VALUE) {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent && event.getCause() == DamageCause.ENTITY_ATTACK) {
            EntityDamageByEntityEvent damageevent = (EntityDamageByEntityEvent) event;
            if (damageevent.getDamager() instanceof Player) {
                Player p = (Player) damageevent.getDamager();
                PlayerData pd = PlayerData.getPlayerData(p.getName());
                if (pd != null && !AncientRPGClass.rightClick && p.getInventory().getItemInHand() != null && pd.getBindings().containsKey(
                        new BindingData(p.getItemInHand())) && !damageignored) {
                    ClassCastCommand.processCast(pd, p, pd.getBindings().get(new BindingData(p.getItemInHand())), ClassCastCommand.castType.Left);
                }
            }
        }
    }

    public static LinkedHashMap<Player, Integer> healpotions = new LinkedHashMap<Player, Integer>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.POTION) {
            Potion p = Potion.fromItemStack(item);
            switch (p.getType()) {
                case INSTANT_HEAL: {
                    healpotions.put(event.getPlayer(), DamageConverter.healPotionHp * (p.getLevel() + 1));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(AncientRPG.plugin, new Runnable() {
                        @Override
                        public void run() {
                            healpotions.remove(event.getPlayer());
                        }
                    });
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (AncientRPGEntityListener.StunList.contains(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        PlayerData pd = PlayerData.getPlayerData(event.getPlayer().getName());
        if (pd != null && (!AncientRPGClass.rightClick || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getItem() != null && pd.getBindings().containsKey(
                new BindingData(event.getPlayer().getItemInHand()))) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ClassCastCommand.processCast(pd, event.getPlayer(),
                        pd.getBindings().get(new BindingData(event.getPlayer().getItemInHand())), ClassCastCommand.castType.Right);
            } else {
                ClassCastCommand.processCast(pd, event.getPlayer(),
                        pd.getBindings().get(new BindingData(event.getPlayer().getItemInHand())), ClassCastCommand.castType.Left);
            }
        } else if (pd != null && (!AncientRPGClass.rightClick || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && pd.getSlotbinds().containsKey(
                event.getPlayer().getInventory().getHeldItemSlot())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ClassCastCommand.processCast(pd, event.getPlayer(),
                        pd.getSlotbinds().get(event.getPlayer().getInventory().getHeldItemSlot()), ClassCastCommand.castType.Right);
            } else {
                ClassCastCommand.processCast(pd, event.getPlayer(),
                        pd.getSlotbinds().get(event.getPlayer().getInventory().getHeldItemSlot()), ClassCastCommand.castType.Left);
            }
        }
    }
}