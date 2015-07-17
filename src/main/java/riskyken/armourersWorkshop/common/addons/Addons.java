package riskyken.armourersWorkshop.common.addons;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;
import riskyken.armourersWorkshop.client.render.item.RenderItemBowSkin;
import riskyken.armourersWorkshop.client.render.item.RenderItemSwordSkin;
import riskyken.armourersWorkshop.utils.EventState;
import riskyken.armourersWorkshop.utils.ModLogger;

public final class Addons {
    
    private static IdentityHashMap<Item, IItemRenderer> customItemRenderers = Maps.newIdentityHashMap();
    
    private static ArrayList<AbstractAddon> loadedAddons = new ArrayList<AbstractAddon>(); 
    
    public static final String[] overrideSwordsDefault = {
        //Minecraft
        "minecraft:wooden_sword",
        "minecraft:stone_sword",
        "minecraft:iron_sword",
        "minecraft:golden_sword",
        "minecraft:diamond_sword",
        "",
        
        //Botania
        "Botania:manasteelSword",
        "Botania:terraSword",
        "Botania:elementiumSword",
        "Botania:excaliber",
        "",
        
        //BetterStorage
        "betterstorage:cardboardSword",
        "",
        
        //Balkon's Weapon Mod
        "weaponmod:battleaxe.wood",
        "weaponmod:battleaxe.stone",
        "weaponmod:battleaxe.iron",
        "weaponmod:battleaxe.diamond",
        "weaponmod:battleaxe.gold",
        
        "weaponmod:warhammer.wood",
        "weaponmod:warhammer.stone",
        "weaponmod:warhammer.iron",
        "weaponmod:warhammer.diamond",
        "weaponmod:warhammer.gold",
        
        "weaponmod:katana.wood",
        "weaponmod:katana.stone",
        "weaponmod:katana.iron",
        "weaponmod:katana.diamond",
        "weaponmod:katana.gold",
        "",
        
        //Tinkers' Construct
        "TConstruct:longsword",
        "TConstruct:broadsword",
        "TConstruct:cleaver",
        "TConstruct:battleaxe",
        "TConstruct:rapier",
        "TConstruct:cutlass",
        "",
        
        //Thaumcraft
        "Thaumcraft:ItemSwordElemental",
        "Thaumcraft:ItemSwordThaumium",
        "Thaumcraft:ItemSwordVoid",
        "",
        
        //Zelda Sword Skills
        "zeldaswordskills:zss.sword_darknut",
        "zeldaswordskills:zss.sword_kokiri",
        "zeldaswordskills:zss.sword_ordon",
        "zeldaswordskills:zss.sword_giant",
        "zeldaswordskills:zss.sword_biggoron",
        "zeldaswordskills:zss.sword_master",
        "zeldaswordskills:zss.sword_tempered",
        "zeldaswordskills:zss.sword_golden",
        "zeldaswordskills:zss.sword_master_true",
        "",
        
        //More Swords Mod
        "MSM3:dawnStar",
        "MSM3:vampiric",
        "MSM3:gladiolus",
        "MSM3:draconic",
        "MSM3:ender",
        "MSM3:crystal",
        "MSM3:glacial",
        "MSM3:aether",
        "MSM3:wither",
        "MSM3:admin",
        "",
        
        //Mekanism Tools
        "MekanismTools:ObsidianSword",
        "MekanismTools:LapisLazuliSword",
        "MekanismTools:OsmiumSword",
        "MekanismTools:BronzeSword",
        "MekanismTools:GlowstoneSword",
        "MekanismTools:SteelSword",
        "",
        
        //Battlegear 2
        "battlegear2:waraxe.wood",
        "battlegear2:waraxe.stone",
        "battlegear2:waraxe.iron",
        "battlegear2:waraxe.diamond",
        "battlegear2:waraxe.gold",
        
        "battlegear2:mace.wood",
        "battlegear2:mace.stone",
        "battlegear2:mace.iron",
        "battlegear2:mace.diamond",
        "battlegear2:mace.gold",
        
        "battlegear2:spear.wood",
        "battlegear2:spear.stone",
        "battlegear2:spear.iron",
        "battlegear2:spear.diamond",
        "battlegear2:spear.gold",
        "",
        
        //Glass Shards
        "glass_shards:glass_sword"
    };
    
    public static final String[] overrideBowsDefault = {
        "minecraft:bow"
    };
    
    public static String[] overrideSwordsActive = {};
    public static String[] overrideBowsActive = {};
    
    public static void init() {
        if (Loader.isModLoaded("TConstruct")) {
            loadedAddons.add(new AddonTConstruct());
        }
        if (Loader.isModLoaded("battlegear2")) {
            loadedAddons.add(new AddonBattlegear2());
        }
        for (int i = 0; i < loadedAddons.size(); i++) {
            loadedAddons.get(i).init();
        }
    }
    
    public static void onWeaponRender(ItemRenderType type, EventState state) {
        for (int i = 0; i < loadedAddons.size(); i++) {
            loadedAddons.get(i).onWeaponRender(type, state);
        }
    }
    
    public static void initRenderers() {    
        overrideSwordRenders();
        overrideBowRenders();
    }
    
    private static void overrideSwordRenders() {
        for (int i = 0; i < overrideSwordsActive.length; i++) {
            String arrayItem = overrideSwordsActive[i];
            if (arrayItem.contains(":")) {
                String[] splitItem = arrayItem.split(":");
                if (Loader.isModLoaded(splitItem[0]) | splitItem[0].equalsIgnoreCase("minecraft")) {
                    overrideItemRenderer(splitItem[0], splitItem[1], RenderType.SWORD);
                }
            } else {
                if (!arrayItem.isEmpty()) {
                    ModLogger.log(Level.ERROR, "Invalid sword override in config file: " + arrayItem);
                }
            }
        }
    }
    
    private static void overrideBowRenders() {
        for (int i = 0; i < overrideBowsActive.length; i++) {
            String arrayItem = overrideBowsActive[i];
            if (arrayItem.contains(":")) {
                String[] splitItem = arrayItem.split(":");
                if (Loader.isModLoaded(splitItem[0]) | splitItem[0].equalsIgnoreCase("minecraft")) {
                    overrideItemRenderer(splitItem[0], splitItem[1], RenderType.BOW);
                }
            } else {
                if (!arrayItem.isEmpty()) {
                    ModLogger.log(Level.ERROR, "Invalid bow override in config file: " + arrayItem);
                }
            }
        }
    }
    
    private static void overrideItemRenderer(String modId, String itemName, RenderType renderType) {
        Item item = GameRegistry.findItem(modId, itemName);
        if (item != null) {
            ItemStack stack = new ItemStack(item);
            IItemRenderer renderer = getItemRenderer(stack);
            ModLogger.log("Overriding render on - " + modId + ":" + itemName);
            if (renderer != null) {
                ModLogger.log("Storing custom item renderer for: " + itemName);
                customItemRenderers.put(item, renderer);
            }
            switch (renderType) {
            case SWORD:
                MinecraftForgeClient.registerItemRenderer(item, new RenderItemSwordSkin());
                break;
            case BOW:
                MinecraftForgeClient.registerItemRenderer(item, new RenderItemBowSkin());
                break;
            }
            
        } else {
            ModLogger.log(Level.WARN, "Unable to override item renderer for: " + modId + " - " + itemName);
        }
    }
    
    private static IItemRenderer getItemRenderer(ItemStack stack) {
        try {
            IdentityHashMap<Item, IItemRenderer> customItemRenderers = null;
            customItemRenderers = ReflectionHelper.getPrivateValue(MinecraftForgeClient.class, null, "customItemRenderers");
            IItemRenderer renderer = customItemRenderers.get(stack.getItem());
            if (renderer != null) {
                return renderer;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static IItemRenderer getItemRenderer(ItemStack item, ItemRenderType type) {
        IItemRenderer renderer = customItemRenderers.get(item.getItem());
        if (renderer != null && renderer.handleRenderType(item, type)) {
            return renderer;
        }
        return null;
    }
    
    public static enum RenderType {
        SWORD,
        BOW;
    }
}
