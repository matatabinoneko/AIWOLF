package jp.or.plala.amail.rin0114.aiwolf;

import jp.or.plala.amail.rin0114.aiwolf.framework.AbstractPlayer;
import jp.or.plala.amail.rin0114.aiwolf.framework.AbstractRole;
import jp.or.plala.amail.rin0114.aiwolf.framework.Game;
import jp.or.plala.amail.rin0114.aiwolf.framework.MetagameModel;
import jp.or.plala.amail.rin0114.aiwolf.metagame.TFAFMetagameModel;
import jp.or.plala.amail.rin0114.aiwolf.role.BodyguardRole;
import jp.or.plala.amail.rin0114.aiwolf.role.MediumRole;
import jp.or.plala.amail.rin0114.aiwolf.role.PossessedRole;
import jp.or.plala.amail.rin0114.aiwolf.role.PossessedRole5ver;
import jp.or.plala.amail.rin0114.aiwolf.role.SeerRoll;
import jp.or.plala.amail.rin0114.aiwolf.role.SeerRoll5ver;
import jp.or.plala.amail.rin0114.aiwolf.role.VillagerRole;
import jp.or.plala.amail.rin0114.aiwolf.role.VillagerRole5ver;
import jp.or.plala.amail.rin0114.aiwolf.role.WerewolfRole;
import jp.or.plala.amail.rin0114.aiwolf.role.WerewolfRole5ver;

/**
 * GAT2018人狼知能プレ大会用Player
 */
public class RsaitoPlayer extends AbstractPlayer {

    @Override
    public String getName() {
        return "rsaito";
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
