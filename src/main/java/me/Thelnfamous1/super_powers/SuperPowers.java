package me.Thelnfamous1.super_powers;

import com.mojang.logging.LogUtils;
import me.Thelnfamous1.super_powers.client.ClientHandler;
import me.Thelnfamous1.super_powers.client.keymapping.SPKeymapping;
import me.Thelnfamous1.super_powers.common.SPCommands;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityAttacher;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Mod(SuperPowers.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SuperPowers {
    public static final String MODID = "super_powers";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SuperPowers() {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> SPCommands.register(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener(SuperpowerCapability::register);
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.Clone event) -> SuperpowerCapability.clone(event));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SuperpowerCapabilityAttacher::attach);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(SPNetwork::register);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientHandler::registerEventHandlers);
    }

    public static ResourceLocation location(String path){
        return new ResourceLocation(MODID, path);
    }


    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // Do something when the setup is run on both client and server
        LOGGER.info("HELLO from common setup!");
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Do something when the setup is run on only the client
        LOGGER.info("HELLO from client setup!");
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event){
        LanguageProvider languageProvider = new LanguageProvider(event.getGenerator(), MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for(Superpower superpower : Superpower.values()){
                    this.add(superpower.getDisplayName().getString(), StringUtils.capitalize(superpower.getSerializedName()));
                }
                this.add(SPCommands.COMMANDS_SUPERPOWER_QUERY, "The current superpower for %s is %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_FAILURE, "%s already has superpower %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_SUCCESS, "The current superpower for %s has been set to %s");
                this.add(SPKeymapping.USE_ABILITY.getName(), "Use Ability");
                this.add(SPKeymapping.USE_ABILITY.getCategory(), "Super Powers");
            }
        };
        event.getGenerator().addProvider(event.includeClient(), languageProvider);
    }
}
