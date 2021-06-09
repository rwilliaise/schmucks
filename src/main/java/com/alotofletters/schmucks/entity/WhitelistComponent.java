package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public interface WhitelistComponent extends Component {
	List<BlockPos> getWhitelist();

	boolean containsWhiteList(BlockPos pos);

	void sync();
	void addWhitelist(BlockPos pos);
	void removeWhitelist(BlockPos pos);

	class Impl implements WhitelistComponent, AutoSyncedComponent {
		private final List<BlockPos> whitelist = new ArrayList<>();
		private final PlayerEntity provider;

		public Impl(PlayerEntity provider) {
			this.provider = provider;
		}

		@Override
		public boolean shouldSyncWith(ServerPlayerEntity player) {
			return this.provider == player;
		}

		@Override
		public void writeSyncPacket(PacketByteBuf data, ServerPlayerEntity recipient) {
			data.writeVarInt(this.getWhitelist().size());
			for (BlockPos blockPos : this.getWhitelist()) {
				data.writeBlockPos(blockPos);
			}
		}

		@Override
		public void applySyncPacket(PacketByteBuf buf) {
			this.getWhitelist().clear();
			int max = buf.readVarInt();
			for (int i = 0; i < max; i++) {
				this.addWhitelist(buf.readBlockPos());
			}
		}

		@Override
		public void sync() {
			Schmucks.WHITELIST.sync(this.provider);
		}

		@Override
		public List<BlockPos> getWhitelist() {
			return whitelist;
		}

		@Override
		public boolean containsWhiteList(BlockPos pos) {
			return this.getWhitelist().contains(pos);
		}

		@Override
		public void addWhitelist(BlockPos pos) {
			this.whitelist.add(pos);
		}

		@Override
		public void removeWhitelist(BlockPos pos) {
			this.whitelist.remove(pos);
		}

		@Override
		public void readFromNbt(NbtCompound tag) {
			this.getWhitelist().clear();
			NbtList list = tag.getList("Whitelisted", 10);
			list.forEach(blockPosTag -> this.addWhitelist(NbtHelper.toBlockPos((NbtCompound) blockPosTag)));
		}

		@Override
		public void writeToNbt(NbtCompound tag) {
			NbtList list = new NbtList();
			for (BlockPos blockPos : this.getWhitelist()) {
				list.add(NbtHelper.fromBlockPos(blockPos));
			}
			tag.put("Whitelisted", list);
		}
	}
}
