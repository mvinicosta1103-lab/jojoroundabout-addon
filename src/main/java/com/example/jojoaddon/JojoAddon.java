package com.example.jojoaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(JojoAddon.MOD_ID)
public class JojoAddon {
    public static final String MOD_ID = "jojoaddon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    // Item de teste: se aparecer na aba "Ingredients" do criativo, o setup funciona.
    public static final RegistryObject<Item> TEST_ITEM =
            ITEMS.register("test_item", () -> new Item(new Item.Properties()));

    public JojoAddon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);

        boolean roundabout = ModList.get().isLoaded("roundabout");
        LOGGER.info("[{}] Roundabout carregado: {}", MOD_ID, roundabout);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(TEST_ITEM);
        }
    }
}
