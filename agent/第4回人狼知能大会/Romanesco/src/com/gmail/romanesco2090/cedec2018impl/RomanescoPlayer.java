package com.gmail.romanesco2090.cedec2018impl;

import com.gmail.romanesco2090.cedec2018impl.metagame.TFAFMetagameModel;
import com.gmail.romanesco2090.cedec2018impl.role.BodyguardRole;
import com.gmail.romanesco2090.cedec2018impl.role.MediumRole;
import com.gmail.romanesco2090.cedec2018impl.role.PossessedRole;
import com.gmail.romanesco2090.cedec2018impl.role.PossessedRole5ver;
import com.gmail.romanesco2090.cedec2018impl.role.SeerRoll;
import com.gmail.romanesco2090.cedec2018impl.role.SeerRoll5ver;
import com.gmail.romanesco2090.cedec2018impl.role.VillagerRole;
import com.gmail.romanesco2090.cedec2018impl.role.VillagerRole5ver;
import com.gmail.romanesco2090.cedec2018impl.role.WerewolfRole;
import com.gmail.romanesco2090.cedec2018impl.role.WerewolfRole5ver;
import com.gmail.romanesco2090.framework.AbstractPlayer;
import com.gmail.romanesco2090.framework.AbstractRole;
import com.gmail.romanesco2090.framework.Game;
import com.gmail.romanesco2090.framework.MetagameModel;

/**
 * cedec2018用Player
 */
public class RomanescoPlayer extends AbstractPlayer {

    @Override
    public String getName() {
        return "Romanesco";
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
