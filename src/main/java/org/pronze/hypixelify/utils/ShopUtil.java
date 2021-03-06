package org.pronze.hypixelify.utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.pronze.hypixelify.Configurator;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.api.database.PlayerDatabase;
import org.pronze.hypixelify.listener.PlayerListener;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.RunningTeam;
import org.screamingsandals.bedwars.api.TeamColor;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.utils.ColorChanger;
import org.screamingsandals.bedwars.game.GameCreator;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;

import java.util.*;

import static org.screamingsandals.bedwars.lib.lang.I.i18n;

public class ShopUtil {

    public static ItemStack Diamond, FireWorks, Arrow, BED;

    private static void InitalizeStacks() {
        Arrow = new ItemStack(Material.ARROW);
        ItemMeta metaArrow = Arrow.getItemMeta();

        metaArrow.setDisplayName(Hypixelify.getConfigurator().config.getString("games-inventory.back-item.name", "§aGo Back"));
        List<String> arrowLore = Hypixelify.getConfigurator().config.getStringList("games-inventory.back-item.lore");
        metaArrow.setLore(arrowLore);
        Arrow.setItemMeta(metaArrow);

        if(Main.isLegacy()) {
            FireWorks = new ItemStack(Material.valueOf("FIREWORK"));
            BED = new ItemStack(Material.valueOf("BED"));
        }
        else {
            FireWorks = new ItemStack(Material.FIREWORK_ROCKET);
            BED = new ItemStack(Material.RED_BED);
        }

        ItemMeta fireMeta = FireWorks.getItemMeta();
        fireMeta.setDisplayName(Hypixelify.getConfigurator().config.getString("games-inventory.firework-name", "§aRandom Map"));
        FireWorks.setItemMeta(fireMeta);

        Diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = Diamond.getItemMeta();
        diamondMeta.setDisplayName(Hypixelify.getConfigurator().config.getString("games-inventory.firework-name", "§aRandom Favorite"));
        Diamond.setItemMeta(diamondMeta);
    }

    public static void addEnchantsToPlayerArmor(Player player, ItemStack item) {
        for (ItemStack i : player.getInventory().getArmorContents()) {
            if (i != null) {
                i.addEnchantments(item.getEnchantments());
            }
        }
    }

    public static void buyArmor(Player player, Material mat_boots, String name, Game game) {
        String matName  = name.substring(0, name.indexOf("_"));
        Material mat_leggings = Material.valueOf(matName + "_LEGGINGS");
        ItemStack boots = new ItemStack(mat_boots);
        ItemStack leggings = new ItemStack(mat_leggings);
        int level = Objects.requireNonNull(Hypixelify.getGameStorage(game)).getProtection(game.getTeamOfPlayer(player).getName());
        if(level != 0){
            boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
            leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.getInventory().setBoots(boots);
        player.getInventory().setLeggings(leggings);
    }

    public static boolean addEnchantsToPlayerTools(Player buyer, ItemStack newItem, String name, Enchantment enchantment) {
        for (ItemStack item : buyer.getInventory().getContents()) {
            if (item != null && item.getType().name().endsWith(name)) {
                if (item.getEnchantmentLevel(enchantment) >= newItem.getEnchantmentLevel(enchantment) || newItem.getEnchantmentLevel(enchantment) >= 5)
                    return false;

                item.addEnchantments(newItem.getEnchantments());
            }
        }

        return true;
    }

    public static boolean addEnchantsToTeamTools(Player buyer, ItemStack stack, String name,  Enchantment enchantment){
        RunningTeam team = BedwarsAPI.getInstance().getGameOfPlayer(buyer).getTeamOfPlayer(buyer);

        if(!ShopUtil.addEnchantsToPlayerTools(buyer, stack, name,enchantment)) return false;

        for (Player player : team.getConnectedPlayers()) {
            player.sendMessage("§c" + buyer.getName() +"§e has upgraded team sword damage!");
            if(player == buyer) continue;
            ShopUtil.addEnchantsToPlayerTools(player, stack, name,enchantment);
        }

        return true;
    }

    static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value) {
        List<K> listOfKeys = null;
        if (mapOfWords.containsValue(value)) {
            listOfKeys = new ArrayList<>();

            for (Map.Entry<K, V> entry : mapOfWords.entrySet()) {
                if (entry.getValue().equals(value)) {
                    listOfKeys.add(entry.getKey());
                }
            }
        }
        return listOfKeys;
    }

    public static List<Game> getGamesWithSize(int c) {
        List<String> allmapnames = getAllKeysForValue(Configurator.game_size, c);
        if (allmapnames == null || allmapnames.isEmpty())
            return null;

        ArrayList<Game> listofgames = new ArrayList<>();

        for (String n : allmapnames) {
            if (Main.getGameNames().contains(n)) {
                listofgames.add(Main.getGame(n));
            }
        }

        return listofgames;
    }

    public static FormatBuilder createBuilder(ArrayList<Object> games, ItemStack category, ItemStack category2, ItemStack category3,
                                              ItemStack category4) {
        FormatBuilder builder = new FormatBuilder();
        Map<String, Object> options = new HashMap<>();
        options.put("rows", 6);
        options.put("render_actual_rows", 6);


        builder.add(category)
                .set("column", 3)
                .set("row", 1);
        builder.add(category2)
                .set("row", 1)
                .set("column", 5)
                .set("items", games)
                .set("options", options);
        builder.add(category3)
                .set("row", 3)
                .set("column", 4);
        builder.add(category4)
                .set("row", 3)
                .set("column", 8);

        return builder;
    }


    public static <K, V> K getKey(HashMap<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    public static void initalizekeys() {

        PlayerListener.UpgradeKeys.put("STONE", 2);
        PlayerListener.UpgradeKeys.put("IRON", 4);
        PlayerListener.UpgradeKeys.put("DIAMOND", 5);
        if(!Main.isLegacy()) {
            PlayerListener.UpgradeKeys.put("WOODEN", 1);
            PlayerListener.UpgradeKeys.put("GOLDEN", 3);
        } else{
            PlayerListener.UpgradeKeys.put("WOOD", 1);
            PlayerListener.UpgradeKeys.put("GOLD", 3);
        }

        for (String material : Hypixelify.getConfigurator().config.getStringList("allowed-item-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            PlayerListener.allowed.add(mat);
        }
        for (String material : Hypixelify.getConfigurator().config.getStringList("running-generator-drops")) {
            Material mat;
            try {
                mat = Material.valueOf(material.toUpperCase().replace(" ", "_"));
            } catch (Exception ignored) {
                continue;
            }
            PlayerListener.generatorDropItems.add(mat);
        }
    }

    public static void giveItemToPlayer(List<ItemStack> itemStackList, Player player, TeamColor teamColor) {
        for (ItemStack itemStack : itemStackList) {
            if (!BedwarsAPI.getInstance().isPlayerPlayingAnyGame(player)) return;

            ColorChanger colorChanger = BedwarsAPI.getInstance().getColorChanger();

            final String materialName = itemStack.getType().toString();
            final PlayerInventory playerInventory = player.getInventory();

            if (materialName.contains("HELMET")) {
                playerInventory.setHelmet(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("CHESTPLATE")) {
                playerInventory.setChestplate(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("LEGGINGS")) {
                playerInventory.setLeggings(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("BOOTS")) {
                playerInventory.setBoots(colorChanger.applyColor(teamColor, itemStack));
            } else if (materialName.contains("PICKAXE")) {
                playerInventory.setItem(7, itemStack);
            } else if (materialName.contains("AXE")) {
                playerInventory.setItem(8, itemStack);
            } else if (materialName.contains("SWORD")) {
                playerInventory.setItem(0, itemStack);
            } else {
                playerInventory.addItem(colorChanger.applyColor(teamColor, itemStack));
            }
        }
    }

    public static ItemStack checkifUpgraded(ItemStack newItem) {
        if (PlayerListener.UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) > PlayerListener.UpgradeKeys.get("WOODEN")) {
            Map<Enchantment, Integer> enchant = newItem.getEnchantments();
            Material mat;
            mat = Material.valueOf(ShopUtil.getKey(PlayerListener.UpgradeKeys, PlayerListener.UpgradeKeys.get(newItem.getType().name().substring(0, newItem.getType().name().indexOf("_"))) - 1) + newItem.getType().name().substring(newItem.getType().name().lastIndexOf("_")));
            ItemStack temp = new ItemStack(mat);
            temp.addEnchantments(enchant);
            return temp;
        }
        return newItem;
    }

    static public String capFirstLetter ( String str )
    {
        String firstLetter = str.substring(0,1).toUpperCase();
        String restLetters = str.substring(1).toLowerCase();
        return firstLetter + restLetters;
    }

    public static ArrayList<Object> createGamesGUI(int mode, List<String> lore) {
        if (Arrow == null)
            InitalizeStacks();

        ArrayList<Object> games = new ArrayList<>();
        int items = 0;
        for (org.screamingsandals.bedwars.api.game.Game game : BedwarsAPI.getInstance()
                .getGames()) {
            if (Configurator.game_size.containsKey(game.getName()) &&
                    Configurator.game_size.get(game.getName()).equals(mode) && items < 28) {
                ItemStack temp = new ItemStack(Material.valueOf(Hypixelify.getConfigurator().config.getString("games-inventory.stack-material", "PAPER")));
                ItemMeta meta1 = temp.getItemMeta();
                String name1 = "§a" + game.getName();
                List<String> newLore = new ArrayList<>();
                for (String ls : lore) {
                    String l = ls.replace("{players}", String.valueOf(game.getConnectedPlayers().size()))
                            .replace("{status}", capFirstLetter(game.getStatus().name()));
                    newLore.add(l);
                }
                meta1.setLore(newLore);
                meta1.setDisplayName(name1);
                temp.setItemMeta(meta1);
                HashMap<String, Object> gameStack = new HashMap<>();
                gameStack.put("stack", temp);
                gameStack.put("game", game);
                games.add(gameStack);
                items++;
            }
        }

        ItemStack arrowStack = Arrow;
        HashMap<String, Object> arrows = new HashMap<>();
        arrows.put("stack", arrowStack);
        arrows.put("row", 5);
        arrows.put("column", 4);
        arrows.put("locate", "main");

        ItemStack fs = FireWorks;
        ItemMeta fsMeta = fs.getItemMeta();
        String size = getGamesWithSize(mode) == null ? "0" : String.valueOf(Objects.requireNonNull(getGamesWithSize(mode)).size());

        List<String> fsMetaLore = Hypixelify.getConfigurator().config.getStringList("games-inventory.fireworks-lore");
        List<String> tempList = new ArrayList<>();
        for(String st : fsMetaLore){
            st = st
                    .replace("{mode}", getModeFromInt(mode))
                    .replace("{games}", size);
            tempList.add(st);
        }

        fsMeta.setLore(tempList);


        fs.setItemMeta(fsMeta);
        HashMap<String, Object> fireworks = new HashMap<>();
        fireworks.put("stack", fs);
        fireworks.put("row", 4);
        fireworks.put("column", 3);

        ItemStack Dia = Diamond;
        ItemMeta diaMeta = Dia.getItemMeta();
        diaMeta.setLore(fsMeta.getLore());
        Dia.setItemMeta(diaMeta);
        HashMap<String, Object> diamond = new HashMap<>();
        diamond.put("stack", Dia);
        diamond.put("row", 4);
        diamond.put("column", 5);

        games.add(arrows);
        games.add(fireworks);
        games.add(diamond);
        return games;
    }

    public static String getModeFromInt(int mode) {
        return mode == 1 ? "Solo" : mode == 2 ? "Double" : mode == 3 ? "Triples" : "Squads";
    }

    public static Options generateOptions() {
        Options options = new Options(Hypixelify.getInstance());
        options.setShowPageNumber(false);

        ItemStack backItem = Main.getConfigurator().readDefinedItem("shopback", "BARRIER");
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName(i18n("shop_back", false));
        backItem.setItemMeta(backItemMeta);
        options.setBackItem(backItem);

        ItemStack pageBackItem = Main.getConfigurator().readDefinedItem("pageback", "ARROW");
        ItemMeta pageBackItemMeta = backItem.getItemMeta();
        pageBackItemMeta.setDisplayName(i18n("page_back", false));
        pageBackItem.setItemMeta(pageBackItemMeta);
        options.setPageBackItem(pageBackItem);

        ItemStack pageForwardItem = Main.getConfigurator().readDefinedItem("pageforward", "ARROW");
        ItemMeta pageForwardItemMeta = backItem.getItemMeta();
        pageForwardItemMeta.setDisplayName(i18n("page_forward", false));
        pageForwardItem.setItemMeta(pageForwardItemMeta);
        options.setPageForwardItem(pageForwardItem);

        ItemStack cosmeticItem = Main.getConfigurator().readDefinedItem("shopcosmetic", "AIR");
        options.setCosmeticItem(cosmeticItem);
        options.setRender_header_start(600);
        options.setRender_footer_start(600);
        options.setRender_offset(9);
        options.setRows(4);
        options.setRender_actual_rows(4);
        options.setShowPageNumber(false);
        return options;
    }

    public static List<ItemStack> createCategories(List<String> lore1,
                                                   String name, String name2) {
        List<ItemStack> myList = new ArrayList<>();

        ItemStack category;
        ItemStack category2;
        if(Main.isLegacy()){
            category = new ItemStack(Material.valueOf("BED"));
            category2 = new ItemStack(Material.valueOf("SIGN"));
        } else{
            category = new ItemStack(Material.valueOf("RED_BED"));
            category2 = new ItemStack(Material.valueOf("OAK_SIGN"));
        }

        ItemStack category3 = new ItemStack(Material.BARRIER);
        ItemStack category4 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = category.getItemMeta();
        meta.setLore(lore1);
        meta.setDisplayName(name);
        category.setItemMeta(meta);

        ItemMeta meta2 = category2.getItemMeta();
        meta2.setLore(Hypixelify.getConfigurator().config.getStringList("games-inventory.oak_sign-lore"));
        meta2.setDisplayName(name2);
        category2.setItemMeta(meta2);

        ItemMeta meta3 = category3.getItemMeta();
        String name3 = Hypixelify.getConfigurator().config.getString("games-inventory.barrier-name","§cExit");
        meta3.setDisplayName(name3);
        category3.setItemMeta(meta3);

        ItemMeta meta4 = category4.getItemMeta();
        String name4 = Hypixelify.getConfigurator().config.getString("games-inventory.ender_pearl-name"
                ,"§cClick here to rejoin!");

        meta4.setLore(Hypixelify.getConfigurator().config.getStringList("games-inventory.ender_pearl-lore"));
        meta4.setDisplayName(name4);
        category4.setItemMeta(meta4);

        myList.add(category);
        myList.add(category2);
        myList.add(category3);
        myList.add(category4);

        return myList;
    }

    public static String translateColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }



    public static void sendMessage(Player player, List<String> message){
        for(String st : message){
            player.sendMessage(translateColors(st));
        }
    }

    public static void upgradeSwordOnPurchase(Player player ,ItemStack newItem, Game game){
        if (Hypixelify.getConfigurator().config.getBoolean("remove-sword-on-upgrade", true)) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType().name().endsWith("SWORD")) {
                        player.getInventory().remove(item);
                }
            }
        }
        int level = Objects.requireNonNull(Hypixelify.getGameStorage(game)).getSharpness(game.getTeamOfPlayer(player).getName());
        if(level == 0) return;
        newItem.addEnchantment(Enchantment.DAMAGE_ALL, level);
    }



    public static void removeAxeOrPickaxe(Player player, ItemStack newItem){
        String name = newItem.getType().name().substring(newItem.getType().name().indexOf("_"));

        for (ItemStack p : player.getInventory().getContents()) {
            if (p != null && p.getType().name().endsWith(name) && !p.getType().name().equalsIgnoreCase(newItem.getType().name())) {
                player.getInventory().remove(p);
            }
        }
    }

    public static String ChatColorChanger(Player player){
        final PlayerDatabase db = Hypixelify.getDatabaseManager().getDatabase(player);
        if(db.getLevel() > 100 || player.isOp()){
            return "§f";
        }
        else{
            return "§7";
        }
    }

    public static boolean isABedwarsSpecialProperty(String property){
        if     (property.equalsIgnoreCase("arrowblocker")
                || property.equalsIgnoreCase("autoigniteabletnt")
                || property.equalsIgnoreCase("golem")
                || property.equalsIgnoreCase("luckyblock")
                || property.equalsIgnoreCase("magnetshoes")
                || property.equalsIgnoreCase("protectionwall")
                || property.equalsIgnoreCase("rescueplatform")
                || property.equalsIgnoreCase("tntsheep")
                || property.equalsIgnoreCase("teamchest")
                || property.equalsIgnoreCase("throwablefireball")
                || property.equalsIgnoreCase("tracker")
                || property.equalsIgnoreCase("trap")
                || property.equalsIgnoreCase("warppowder")
                )
            return true;


        return false;
    }

}