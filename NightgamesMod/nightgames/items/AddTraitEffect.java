package nightgames.items;

import nightgames.characters.Character;
import nightgames.characters.trait.Trait;
import nightgames.combat.Combat;
import nightgames.gui.GUI;

public class AddTraitEffect extends ItemEffect {
    private Trait trait;
    private int selfDuration;

    public AddTraitEffect(String selfverb, String otherverb, Trait trait, int duration) {
        super(selfverb, otherverb, true, true);
        this.trait = trait;
        selfDuration = duration;
    }

    public AddTraitEffect(String selfverb, String otherverb, Trait trait) {
        this(selfverb, otherverb, trait, -1);
    }

    @Override
    public boolean use(Combat c, Character user, Character opponent, Item item) {
        int duration = selfDuration >= 0 ? selfDuration : item.duration;
        if (user.addTemporaryTrait(trait, duration)) {
            if (c != null) {
                c.write(user, user.subjectAction("temporarily gained", "temporarily gained") + " the trait "
                                + trait.toString() + ".");
            } else if (user.human()) {
                GUI.gui.message(user.subjectAction("temporarily gained", "temporarily gained") + " the trait "
                                + trait.toString() + ".");
            }
            return true;
        }
        return false;
    }
}
