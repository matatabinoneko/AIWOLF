package com.gmail.shoot7arrow25.player;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class NeoElizaRoleAssignPlayer extends AbstractRoleAssignPlayer {
    @Override
    public String getName() {
        return "NeoElizaRoleAssignPlayer";
    }

    public NeoElizaRoleAssignPlayer() {
        setVillagerPlayer(new NeoElizaVillager());
        setSeerPlayer(new NeoElizaSeer());
        setWerewolfPlayer(new NeoElizaWerewolf());
        setMediumPlayer(new NeoElizaMedium());
        setBodyguardPlayer(new NeoElizaBodyguard());
        setPossessedPlayer(new NeoElizaPossessed());
    }
}
