package com.alotofletters.schmucks.server.command;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.specialization.Specialization;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.*;

import java.util.Collection;

public class SpecializationCommand {

	private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
		Collection<Specialization> collection = Schmucks.LOADER.getSpecializations();
		return CommandSource.suggestIdentifiers(collection.stream().map(Specialization::getId), builder);
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("specialization")
			.requires(source -> source.hasPermissionLevel(2))
				.then(literal("upgrade")
						.then(argument("specialization", IdentifierArgumentType.identifier())
							.suggests(SUGGESTION_PROVIDER)
								.executes(context -> {
									Identifier id = IdentifierArgumentType.getIdentifier(context, "specialization");
									if (context.getSource().getPlayer() != null && id != null) {
										SpecializationsComponent component =
												Schmucks.SPECIALIZATIONS.get(context.getSource().getPlayer());
										Specialization spec = Schmucks.LOADER.get(
												context.getArgument("specialization", Identifier.class));
										try {
											component.upgradeLevel(spec);
										} catch (Exception e) {
											e.printStackTrace();
										}
										return Command.SINGLE_SUCCESS;
									}
									return 0;
								}))
				)
				.then(literal("apply")
						.executes(context -> {
							if (context.getSource().getPlayer() != null) {
								SpecializationsComponent component =
										Schmucks.SPECIALIZATIONS.get(context.getSource().getPlayer());
								component.apply();
								return Command.SINGLE_SUCCESS;
							}
							return 0;
						})
				)
		);
	}
}
