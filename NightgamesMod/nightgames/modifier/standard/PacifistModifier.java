package nightgames.modifier.standard;

import nightgames.characters.Player;
import nightgames.global.GameState;
import nightgames.modifier.BaseModifier;
import nightgames.modifier.skill.BanTacticsModifier;
import nightgames.skills.Tactics;

public class PacifistModifier extends BaseModifier {

    Player player;
    public PacifistModifier() {
        skills = new BanTacticsModifier(Tactics.damage);
        player = GameState.getGameState().characterPool.getPlayer();
    }

    @Override
    public int bonus() {
        return 100;
    }

    @Override
    public String name() {
        return "pacifist";
    }

    @Override
    public String intro() {
        player = GameState.getGameState().characterPool.getPlayer();
        return "Lilly gives you a long, appraising look. <i>\"I'm trying to decide what sort of " + player.manOrWoman() + "you are. You strike me as a good " + player.guyOrGal() + ", probably not the type "
                        + "to hit a girl outside a match. I propose you try being a perfect gentle" + player.manOrWoman() + " by refusing to hit anyone during tonight's match too. So no slapping, "
                        + "kicking, anything intended to purely cause pain. If you agree, I'll add $" + bonus()
                        + " to each point. What do you say?\"</i>";
    }

    @Override
    public String acceptance() {
        player = GameState.getGameState().characterPool.getPlayer();
        String kickThreat;
        if(player.hasBalls()) {
            kickThreat = "kicks " + player.directObject() + " in the balls"; 
        } else if(player.hasDick()) {
            kickThreat = "kicks " + player.directObject() + " in the dick"; 
        } else {
            kickThreat = "kicks " + player.directObject() + " down";
        }
        return "Lilly flashes you a broad grin and slaps you on the back uncomfortably hard. <i>\"Just so everyone's aware,\"</i> she calls out to your opponents, <i>\""
                        + player.getTrueName() + " has sworn that" + player.pronoun() + "won't hurt any girls tonight. So no matter how much anyone taunts " + player.directObject()
                        + ", whips " + player.directObject() + ", or " + kickThreat + ", " + player.pronoun() + " can't retaliate in \"any way.\\\""
                        + "</i> As you try to ignore a growing sense of dread, she leans close to your ear and whispers, <i>\"Good luck.\"</i>";
    }

}
