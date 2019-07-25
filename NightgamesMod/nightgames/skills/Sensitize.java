package nightgames.skills;

import nightgames.characters.Character;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Formatter;
import nightgames.items.Item;
import nightgames.status.Hypersensitive;
import nightgames.status.Stsflag;

public class Sensitize extends Skill {

    Sensitize() {
        super("Sensitivity Potion");
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return true;
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        return c.getStance().mobile(user) && user.canAct() && user.has(Item.SPotion)
                        && target.mostlyNude() && !c.getStance().prone(user) && !target.is(Stsflag.hypersensitive);
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Makes your opponent hypersensitive";
    }

    @Override
    public int accuracy(Combat c, Character user, Character target) {
        return user.has(Item.Aersolizer) ? 200 : 65;
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target) {
        user.consume(Item.SPotion, 1);
        if (user.has(Item.Aersolizer)) {
            writeOutput(c, Result.special, user, target);
        } else if (target.roll(user, accuracy(c, user, target))) {
            writeOutput(c, Result.normal, user, target);
        } else {
            writeOutput(c, Result.miss, user, target);
            return false;
        }
        target.add(c, new Hypersensitive(target.getType()));
        return true;
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.debuff;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.special) {
            return "You pop a sensitivity potion into your Aerosolizer and spray " + target.getName()
                            + " with a cloud of mist. She shivers as it takes hold and heightens her "
                            + "sense of touch.";
        } else if (modifier == Result.miss) {
            return "You throw a bottle of sensitivity elixir at " + target.getName()
                            + ", but she ducks out of the way and it splashes harmlessly on the ground. What a waste.";
        } else {
            return "You throw a sensitivity potion at " + target.getName()
                            + ". You see her skin flush as it takes effect.";
        }
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.special) {
            return String.format("%s inserts a bottle into the attachment on %s arm. %s "
                            + "suddenly surrounded by a cloud of minty gas. %s skin becomes"
                            + " hot, but goosebumps appear anyway. "
                            + "Even the air touching %s skin makes %s shiver.", user.subject(),
                            user.possessiveAdjective(),
                            Formatter.capitalizeFirstLetter(target.subjectAction("are", "is")),
                            target.possessiveAdjective(), target.possessiveAdjective(),
                            target.directObject());
        } else if (modifier == Result.miss) {
            return String.format("%s splashes a bottle of liquid in %s direction, but none of it hits %s.",
                            user.subject(), target.nameDirectObject(), target.directObject());
        } else {
            return String.format("%s throws a bottle of strange liquid at %s. The skin it touches grows hot"
                            + " and oversensitive.", user.subject(), target.nameDirectObject());
        }
    }

}
