package nightgames.skills;

import nightgames.characters.Character;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Random;
import nightgames.stance.Behind;
import nightgames.stance.Mount;
import nightgames.stance.Stance;
import nightgames.status.addiction.Addiction;
import nightgames.status.addiction.AddictionType;

// Not Prostate, ProstRate
public class Prostrate extends Skill {

    public Prostrate() {
        super("Prostrate");
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return user.human();
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        if (!user.checkAnyAddiction(AddictionType.ZEAL))
            return false;
        return user.getAnyAddiction(AddictionType.ZEAL).map(a -> c.getStance().en == Stance.neutral && a.wasCausedBy(target)).orElse(false);
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Display total submission to your Goddess by laying yourself before Her";
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target) {
        c.write(user, "You kneel and bend forward in front of Angel, touching your forehead to the ground."
                        + " You say nothing, wordlessly offering yourself to whatever She wants for you.");
        if (!target.hasDick() || Random.random(2) == 0) {
            c.write(target, "Angel lays a hand on the back of your head and then softly pushes to the side."
                            + " Understanding Her intent, you roll over onto your back, and she sits down on top of you,"
                            + " smiling kindly.");
            c.setStance(new Mount(target.getType(), user.getType()), user, true);
        } else {
            c.write(target, "Angel curls a finger under your chin and lifts your head. She keeps going, and you"
                            + " understand she wants you to stand up. Back on your feet, She traces a hand over your"
                            + " chest and shoulder while walking around you and gently hugging you from behind.");
            c.setStance(new Behind(target.getType(), user.getType()), user, true);
        }
        user.unaddictCombat(AddictionType.ZEAL, target, Addiction.LOW_INCREASE, c);
        user.addict(c, AddictionType.ZEAL, target, Addiction.LOW_INCREASE);
        return true;
    }

    @Override
    public Skill copy(Character user) {
        return new Prostrate();
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.recovery; // because of the unaddict?
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        throw new UnsupportedOperationException();
    }

}
