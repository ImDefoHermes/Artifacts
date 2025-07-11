package me.imhermes.artifacts.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.imhermes.artifacts.Artifacts;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ExprArtifactItem extends SimpleExpression<ItemStack> {

    static {
        Skript.registerExpression(ExprArtifactItem.class, ItemStack.class, ExpressionType.SIMPLE, "artifact item %string%");
    }

    Expression<String> artifactId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        artifactId = (Expression<String>) expressions[0];
        return true;
    }
    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    protected ItemStack @Nullable [] get(Event event) {
        String artifactIdSingle = artifactId.getSingle(event);
        if (artifactIdSingle == null) return null;
        ConfigurationSection configurationSection = Artifacts.instance().getConfig().getConfigurationSection("artifacts");
        if (configurationSection == null) return null;
        return new ItemStack[]{configurationSection.getItemStack(artifactIdSingle)};
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "artifact item " + artifactId.toString(event, b);
    }
}