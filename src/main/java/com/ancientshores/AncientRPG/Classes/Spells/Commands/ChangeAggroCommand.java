package com.ancientshores.AncientRPG.Classes.Spells.Commands;

import com.ancientshores.AncientRPG.AncientRPG;
import com.ancientshores.AncientRPG.Classes.Spells.CommandDescription;
import com.ancientshores.AncientRPG.Classes.Spells.ParameterType;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ConcurrentHashMap;

public class ChangeAggroCommand extends ICommand {
    @CommandDescription(description = "Changes the aggro of the entity to the new target for atleast the specified time",
            argnames = {"entity", "newtarget", "duration"}, name = "ChangeAggro", parameters = {ParameterType.Entity, ParameterType.Entity, ParameterType.Number})

    public static final ConcurrentHashMap<LivingEntity, LivingEntity> tauntedEntities = new ConcurrentHashMap<LivingEntity, LivingEntity>();

    public ChangeAggroCommand() {
        this.paramTypes = new ParameterType[]{ParameterType.Entity, ParameterType.Entity, ParameterType.Number};
    }

    @Override
    public boolean playCommand(final EffectArgs ca) {
        if (ca.getParams().size() == 3) {
            if (ca.getParams().get(0) instanceof Entity[] && ca.getParams().get(1) instanceof Entity[] && ca.getParams().get(2) instanceof Number) {
                final Entity[] target = (Entity[]) ca.getParams().get(0);
                final Entity[] newaggro = (Entity[]) ca.getParams().get(1);
                if (newaggro.length == 0 || !(newaggro[0] instanceof LivingEntity)) {
                    return false;
                }
                final int time = (int) ((Number) ca.getParams().get(2)).doubleValue();
                for (final Entity e : target) {
                    if (e instanceof Creature) {
                        ((Creature) e).setTarget((LivingEntity) newaggro[0]);
                        tauntedEntities.put((LivingEntity) e, (LivingEntity) newaggro[0]);

                        AncientRPG.plugin.scheduleThreadSafeTask(AncientRPG.plugin, new Runnable() {
                            public void run() {
                                tauntedEntities.remove(e);
                            }
                        }, Math.round(time / 50));
                    }
                }
                return true;
            }
        }
        return false;
    }
}