package tera.aiwolf;

import tera.aiwolf.framework.AbstractPlayer;
import tera.aiwolf.framework.AbstractRole;
import tera.aiwolf.framework.Game;
import tera.aiwolf.framework.MetagameModel;
import tera.aiwolf.metagame.TFAFMetagameModel;
import tera.aiwolf.role.BodyguardRole;
import tera.aiwolf.role.MediumRole;
import tera.aiwolf.role.PossessedRole;
import tera.aiwolf.role.PossessedRole5ver;
import tera.aiwolf.role.SeerRoll;
import tera.aiwolf.role.SeerRoll5ver;
import tera.aiwolf.role.VillagerRole;
import tera.aiwolf.role.VillagerRole5ver;
import tera.aiwolf.role.WerewolfRole;
import tera.aiwolf.role.WerewolfRole5ver;

/**
 * CEDEC2018人狼知能Player
 */
public class TeraAgentPlayerv3 extends AbstractPlayer {

    @Override
    public String getName() {
        return "f6wbl6";
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
