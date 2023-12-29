package me.Thelnfamous1.super_powers;

import com.mojang.logging.LogUtils;
import me.Thelnfamous1.super_powers.client.ClientHandler;
import me.Thelnfamous1.super_powers.client.keymapping.SPKeymapping;
import me.Thelnfamous1.super_powers.common.SPCommands;
import me.Thelnfamous1.super_powers.common.Superpower;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapability;
import me.Thelnfamous1.super_powers.common.capability.SuperpowerCapabilityAttacher;
import me.Thelnfamous1.super_powers.common.effect.ElectricShockEffect;
import me.Thelnfamous1.super_powers.common.entity.EnergyBeam;
import me.Thelnfamous1.super_powers.common.entity.TelekinesisBlockEntity;
import me.Thelnfamous1.super_powers.common.network.SPNetwork;
import me.Thelnfamous1.super_powers.common.util.SuperpowerHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Mod(SuperPowers.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SuperPowers {
    public static final String MODID = "super_powers";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<EnergyBeam>> ENERGY_BEAM = register("energy_beam",
            EntityType.Builder.<EnergyBeam>of(EnergyBeam::new, MobCategory.MISC)
                    .fireImmune()
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(6)
                    .updateInterval(2));

    public static final RegistryObject<EntityType<TelekinesisBlockEntity>> TELEKINESIS_BLOCK = register("telekinesis_block",
            EntityType.Builder.of(TelekinesisBlockEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .clientTrackingRange(10)
            .updateInterval(20));


    private static <T extends Entity> RegistryObject<EntityType<T>> register(String pKey, EntityType.Builder<T> pBuilder) {
        return ENTITY_TYPES.register(pKey, () -> pBuilder.build(MODID + ":" + pKey));
    }
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final RegistryObject<ElectricShockEffect> ELECTRIC_SHOCK_EFFECT = MOB_EFFECTS.register("electric_shock",
            () -> new ElectricShockEffect(MobEffectCategory.HARMFUL, Superpower.LIGHTNING.getDyeColor().getTextColor()));


    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);

    public static final RegistryObject<SimpleParticleType> ELECTRIC_SHOCK_PARTICLE = PARTICLE_TYPES.register("electric_shock",
            () -> new SimpleParticleType(false));

    public static final TagKey<Block> TELEKINESIS_IMMUNE = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MODID, "telekinesis_immune"));

    public SuperPowers() {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> SPCommands.register(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener(SuperpowerCapability::register);
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.Clone event) -> SuperpowerCapability.clone(event));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SuperpowerCapabilityAttacher::attach);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.PlayerTickEvent event) -> {
            if(event.phase == TickEvent.Phase.END){
                SuperpowerHelper.tickActivePowers(event.player);
                if(!event.player.level.isClientSide){
                    SuperpowerCapability.getOptional(event.player).ifPresent(cap -> {
                        ServerPlayer serverPlayer = (ServerPlayer) event.player;
                        // give flight to superpowered players, or remove it for non-superpowered players
                        if(cap.getSuperpowers().isEmpty()){
                            if(serverPlayer.gameMode.isSurvival()) serverPlayer.getAbilities().mayfly = false;
                            serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(SuperpowerHelper.copyAbilities(serverPlayer, false)));
                        } else if(!serverPlayer.getAbilities().mayfly){
                            serverPlayer.getAbilities().mayfly = true;
                            serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(SuperpowerHelper.copyAbilities(serverPlayer, true)));
                        }
                    });
                }
            }
        });
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        modEventBus.addListener(SPNetwork::register);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientHandler::registerEventHandlers);
    }

    public static ResourceLocation location(String path){
        return new ResourceLocation(MODID, path);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event){
        LanguageProvider languageProvider = new LanguageProvider(event.getGenerator(), MODID, "en_us") {
            @Override
            protected void addTranslations() {
                for(Superpower superpower : Superpower.values()){
                    this.add(SPKeymapping.SUPERPOWERS_BY_KEY.inverse().get(superpower).getName(), "Use %s".formatted(StringUtils.capitalize(superpower.getSerializedName())));
                    this.add(superpower.getDisplayName().getString(), StringUtils.capitalize(superpower.getSerializedName()));
                }
                this.add(SPKeymapping.KEY_CATEGORY, "Super Powers");
                this.add(SPCommands.COMMANDS_SUPERPOWER_QUERY, "%s has the following superpowers: ");
                this.add(SPCommands.COMMANDS_SUPERPOWER_GIVE_FAILURE, "%s already has %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_GIVE_SUCCESS, "%s now has %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_GIVE_ALL, "%s now has all superpowers");
                this.add(SPCommands.COMMANDS_SUPERPOWER_REMOVE_FAILURE, "%s does not have %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_REMOVE_SUCCESS, "%s no longer has %s");
                this.add(SPCommands.COMMANDS_SUPERPOWER_REMOVE_ALL, "%s now has no superpowers");
                this.add(ENERGY_BEAM.get(), "Energy Beam");
                this.add(TELEKINESIS_BLOCK.get(), "Telekinesis Block");
                this.add(ELECTRIC_SHOCK_EFFECT.get(), "Electric Shock");
            }
        };
        event.getGenerator().addProvider(event.includeClient(), languageProvider);

        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(event.getGenerator(), MODID, event.getExistingFileHelper()){
            @Override
            protected void addTags() {
                this.tag(TELEKINESIS_IMMUNE).addTag(BlockTags.WITHER_IMMUNE);
            }
        };
        event.getGenerator().addProvider(event.includeServer(), blockTagsProvider);
    }
}
