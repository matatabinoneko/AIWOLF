package tkg.aiwolf;

import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractRole;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.MetagameModel;
import tkg.aiwolf.metagame.TFAFMetagameModel;
import tkg.aiwolf.role.BodyguardRole;
import tkg.aiwolf.role.MediumRole;
import tkg.aiwolf.role.PossessedRole;
import tkg.aiwolf.role.PossessedRole5ver;
import tkg.aiwolf.role.SeerRoll;
import tkg.aiwolf.role.SeerRoll5ver;
import tkg.aiwolf.role.VillagerRole;
import tkg.aiwolf.role.VillagerRole5ver;
import tkg.aiwolf.role.WerewolfRole;
import tkg.aiwolf.role.WerewolfRole5ver;

public class TKGPlayer extends AbstractPlayer {

    @Override
    public String getName() {
        return "tkg";
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
