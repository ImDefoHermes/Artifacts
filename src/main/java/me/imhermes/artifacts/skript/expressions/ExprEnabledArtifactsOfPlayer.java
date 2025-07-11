package me.imhermes.artifacts.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import me.imhermes.artifacts.util.Util;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import static ch.njol.skript.classes.Changer.ChangeMode;

public class ExprEnabledArtifactsOfPlayer extends SimplePropertyExpression<OfflinePlayer, String[]> {

    static {
        register(ExprEnabledArtifactsOfPlayer.class, String[].class, "enabled artifact[s]", "player");
    }

    @Override
    public void change(Event event, Object[] delta, ChangeMode mode) {
    }

    @Override
    public Class<? extends String[]> getReturnType() {
        return String[].class;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    @Nullable
    public String[] convert(OfflinePlayer player) {
        return player == null ? null : Util.getEnabledArtifacts(player).toArray(new String[0]);
    }

    @Override
    protected String getPropertyName() {
        return "enabled_artifacts";
    }

    @Override
    public Class<?> @org.jetbrains.annotations.Nullable [] acceptChange(ChangeMode mode) {
        return null;
    }
}