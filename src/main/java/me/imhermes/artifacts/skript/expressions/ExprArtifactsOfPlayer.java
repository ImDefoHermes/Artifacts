package me.imhermes.artifacts.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import me.imhermes.artifacts.listeners.ModifiablePersistentDataContainers;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.njol.skript.classes.Changer.ChangeMode;
import static me.imhermes.artifacts.util.Util.namespacedKey;

public class ExprArtifactsOfPlayer extends SimplePropertyExpression<OfflinePlayer, String[]> {

    static {
        register(ExprArtifactsOfPlayer.class, String[].class, "[available] artifact[s]", "offlineplayer");
    }

    @Override
    public void change(Event event, Object[] delta, ChangeMode mode) {
        OfflinePlayer p = getExpr().getSingle(event);
        if (p == null) return;
        List<String> strings = Arrays.stream(delta)
                .filter(String.class::isInstance)
                .map(s -> (String) s)
                .toList();
        PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(p);
        List<String> artifacts = pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), new ArrayList<>());

        switch (mode) {
            case SET -> {
                pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), strings);
                ModifiablePersistentDataContainers.set(p.getUniqueId(), pdc);
            }
            case ADD -> {
                artifacts.addAll(strings);
                pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), strings);
                ModifiablePersistentDataContainers.set(p.getUniqueId(), pdc);
            }
            case REMOVE -> {
                artifacts.removeAll(strings);
                pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), strings);
                ModifiablePersistentDataContainers.set(p.getUniqueId(), pdc);
            }
            case RESET -> {
                pdc.set(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of());
                ModifiablePersistentDataContainers.set(p.getUniqueId(), pdc);
            }
        }
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
        if (player == null) return null;
        PersistentDataContainer pdc = ModifiablePersistentDataContainers.getModifiablePersistentDataContainer(player);
        List<String> artifactIds = pdc.getOrDefault(namespacedKey("artifacts"), PersistentDataType.LIST.strings(), List.of());

        return artifactIds.toArray(new String[0]);
    }

    @Override
    protected String getPropertyName() {
        return "artifacts";
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (List.of(ChangeMode.REMOVE, ChangeMode.ADD, ChangeMode.SET, ChangeMode.RESET).contains(mode)) return new Class[]{String.class};
        return null;
    }
}