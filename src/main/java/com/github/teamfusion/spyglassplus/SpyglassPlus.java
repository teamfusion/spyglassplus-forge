package com.github.teamfusion.spyglassplus;

import com.github.teamfusion.spyglassplus.client.ClientRegistrar;
import com.github.teamfusion.spyglassplus.common.message.ResetTargetMessage;
import com.github.teamfusion.spyglassplus.common.message.ScrutinyResetMessage;
import com.github.teamfusion.spyglassplus.common.message.SpyglassCameraRotateMessage;
import com.github.teamfusion.spyglassplus.common.message.SpyglassStandResetMessage;
import com.github.teamfusion.spyglassplus.common.message.TargetMessage;
import com.github.teamfusion.spyglassplus.core.registry.SpyglassPlusEnchantments;
import com.github.teamfusion.spyglassplus.core.registry.SpyglassPlusEntityTypes;
import com.github.teamfusion.spyglassplus.core.registry.SpyglassPlusItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpyglassPlus.MOD_ID)
public class SpyglassPlus {
	public static Logger LOGGER = LogManager.getLogger();

	public static final String MOD_ID = "spyglassplus";
	public static final String MOD_NAME = "Spyglass+";

	public static final String NETWORK_PROTOCOL = "2";

	public static final CreativeModeTab SPYGLASSPLUS_TAB = new CreativeModeTab(
			new ResourceLocation(MOD_ID, "spyglassplus_tab").toString()) {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(Items.SPYGLASS);
		}

		public void fillItemList(NonNullList<ItemStack> items) {
			super.fillItemList(items);
			for (RegistryObject<Enchantment> registryObject : SpyglassPlusEnchantments.ENCHANTMENTS.getEntries()) {
				if (registryObject.get() instanceof Enchantment) {
					Enchantment enchant = registryObject.get();
					items.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, enchant.getMaxLevel())));
				}
			}
		}
	};


	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MOD_ID, "net"))
			.networkProtocolVersion(() -> NETWORK_PROTOCOL)
			.clientAcceptedVersions(NETWORK_PROTOCOL::equals)
			.serverAcceptedVersions(NETWORK_PROTOCOL::equals)
			.simpleChannel();


	public SpyglassPlus() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		SpyglassPlusEnchantments.ENCHANTMENTS.register(bus);
		SpyglassPlusItems.ITEMS.register(bus);
		SpyglassPlusEntityTypes.ENTITIES.register(bus);
		log(Level.INFO, "Enhancing Spyglasses!");
		this.setupMessages();
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientRegistrar::setup));
	}

	private void setupMessages() {
		CHANNEL.messageBuilder(ScrutinyResetMessage.class, 0)
				.encoder(ScrutinyResetMessage::serialize).decoder(ScrutinyResetMessage::deserialize)
				.consumer(ScrutinyResetMessage::handle)
				.add();
		CHANNEL.messageBuilder(TargetMessage.class, 1)
				.encoder(TargetMessage::serialize).decoder(TargetMessage::deserialize)
				.consumer(TargetMessage::handle)
				.add();
		CHANNEL.messageBuilder(ResetTargetMessage.class, 2)
				.encoder(ResetTargetMessage::serialize).decoder(ResetTargetMessage::deserialize)
				.consumer(ResetTargetMessage::handle)
				.add();
		CHANNEL.messageBuilder(SpyglassStandResetMessage.class, 3)
				.encoder(SpyglassStandResetMessage::serialize).decoder(SpyglassStandResetMessage::deserialize)
				.consumer(SpyglassStandResetMessage::handle)
				.add();
		CHANNEL.messageBuilder(SpyglassCameraRotateMessage.class, 4)
				.encoder(SpyglassCameraRotateMessage::serialize).decoder(SpyglassCameraRotateMessage::deserialize)
				.consumer(SpyglassCameraRotateMessage::handle)
				.add();
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static void log(Level level, String message) {
		LOGGER.log(level, "[" + MOD_NAME + "] " + message);
	}
}
