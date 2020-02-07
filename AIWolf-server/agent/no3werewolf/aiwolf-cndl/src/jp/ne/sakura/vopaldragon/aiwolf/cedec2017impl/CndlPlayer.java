package jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl;

import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.metagame.TFAFMetagameModel;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.BodyguardRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.MediumRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.PossessedRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.PossessedRole5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.SeerRoll;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.SeerRoll5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.VillagerRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.VillagerRole5ver;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.WerewolfRole;
import jp.ne.sakura.vopaldragon.aiwolf.cedec2017impl.role.WerewolfRole5ver;
import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractPlayer;
import jp.ne.sakura.vopaldragon.aiwolf.framework.Game;
import jp.ne.sakura.vopaldragon.aiwolf.framework.MetagameModel;
import jp.ne.sakura.vopaldragon.aiwolf.framework.AbstractRole;

/**
 * cedec2017用Player
 */
public class CndlPlayer extends AbstractPlayer {

    @Override
    public String getName() {
        return "cndl";
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
