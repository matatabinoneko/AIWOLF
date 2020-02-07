package com.gmail.naglfar.the.on;

import com.gmail.naglfar.the.on.framework.AbstractPlayer;
import com.gmail.naglfar.the.on.framework.AbstractRole;
import com.gmail.naglfar.the.on.framework.Game;
import com.gmail.naglfar.the.on.framework.MetagameModel;
import com.gmail.naglfar.the.on.metagame.TFAFMetagameModel;
import com.gmail.naglfar.the.on.role.BodyguardRole;
import com.gmail.naglfar.the.on.role.MediumRole;
import com.gmail.naglfar.the.on.role.PossessedRole;
import com.gmail.naglfar.the.on.role.PossessedRole5ver;
import com.gmail.naglfar.the.on.role.SeerRoll;
import com.gmail.naglfar.the.on.role.SeerRoll5ver;
import com.gmail.naglfar.the.on.role.VillagerRole;
import com.gmail.naglfar.the.on.role.VillagerRole5ver;
import com.gmail.naglfar.the.on.role.WerewolfRole;
import com.gmail.naglfar.the.on.role.WerewolfRole5ver;

/**
 * GAT2018用Player
 */
public class NekoPlayer extends AbstractPlayer {

    @Override
    public String getName() {
        return "neko";
    }

    @Override
    protected MetagameModel createMetagameModel() {
        return new TFAFMetagameModel();
    }

    @Override
    protected AbstractRole selectRole(Game game) {
        if (game.getVillageSize() == 5) {
            switch (game.getSelf().role) {
                case WEREWOLF:
                    return new WerewolfRole5ver(game);
                case POSSESSED:
                    return new PossessedRole5ver(game);
                case SEER:
                    return new SeerRoll5ver(game);
                case VILLAGER:
                    return new VillagerRole5ver(game);
            }
        } else {
            switch (game.getSelf().role) {
                case WEREWOLF:
                    return new WerewolfRole(game);
                case BODYGUARD:
                    return new BodyguardRole(game);
                case MEDIUM:
                    return new MediumRole(game);
                case POSSESSED:
                    return new PossessedRole(game);
                case SEER:
                    return new SeerRoll(game);
                case VILLAGER:
                    return new VillagerRole(game);
            }
        }
        return null;
    }
}
