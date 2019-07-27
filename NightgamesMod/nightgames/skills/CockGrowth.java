package nightgames.skills;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.body.CockPart;
import nightgames.characters.body.mods.SizeMod;
import nightgames.characters.trait.Trait;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Formatter;
import nightgames.global.Random;
import nightgames.status.Hypersensitive;

public class CockGrowth extends Skill {
    public CockGrowth() {
        super("Cock Growth");
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return user.getAttribute(Attribute.spellcasting) >= 12;
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        return user.canAct() && c.getStance().mobile(user) && !c.getStance().prone(user);
    }

    @Override
    public float priorityMod(Combat c, Character user) {
        return .5f;
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Grows or enlarges your opponent's cock.";
    }

    @Override
    public int getMojoCost(Combat c, Character user) {
        return 25;
    }

    @Override
    public int baseAccuracy(Combat c, Character user, Character target) {
        return 90;
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target, boolean rollSucceeded) {
        Result res = rollSucceeded ? Result.normal : Result.miss;
        if (res == Result.normal && !target.hasDick()) {
            res = Result.special;
        }

        boolean permanent = Random.random(20) == 0 && (user.human() || c.shouldPrintReceive(target, c))
                        && !target.has(Trait.stableform);

        if (res != Result.miss) {
            target.add(c, new Hypersensitive(target.getType(), 10));
            CockPart part = target.body.getCockBelow(SizeMod.getMaximumSize("cock"));
            if (permanent) {
                if (part != null) {
                    target.body.addReplace(part.upgrade(), 1);
                } else {
                    target.body.addReplace(new CockPart().applyMod(new SizeMod(SizeMod.COCK_SIZE_SMALL)), 1);
                }
            } else {
                if (part != null) {
                    target.body.temporaryAddOrReplacePartWithType(part.upgrade(), 10);
                } else {
                    target.body.temporaryAddPart(new CockPart().applyMod(new SizeMod(SizeMod.COCK_SIZE_SMALL)), 10);
                }
            }
        }
        writeOutput(c, permanent ? 1 : 0, res, user, target);
        return res != Result.miss;
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.debuff;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        String message;
        if (modifier == Result.miss) {
            message = "You attempt to channel your arcane energies into " + target.getName()
                            + "'s crotch, but she dodges out of the way, causing your spell to fail.";
        } else {
            if (modifier == Result.special) {
                message = "You channel your arcane energies into " + target.getName() + "'s groin. A moment later, "
                                + target.getName() + " yelps as her clitoris rapidly enlarges into a small girl-cock!";
            } else {
                message = "You channel your arcane energies into " + target.getName() + "'s dick. A moment later, "
                                + target.getName() + " yelps as her cock rapidly enlarges!";
            }
            if (damage > 0) {
                message += " You realize the effects are permanent!";
            }
        }
        return message;
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        String message;
        if (modifier == Result.miss) {
            message = String.format("%s moving and begins chanting. %s feeling some "
                            + "tingling in %s groin, but it quickly subsides as %s %s out of the way.", 
                            user.subjectAction("stop"), Formatter.capitalizeFirstLetter(target.subjectAction("start")),
                            target.possessiveAdjective(), target.pronoun(), target.action("dodge"));
        } else {
            if (modifier == Result.special) {
                message = String.format("%s moving and begins chanting. %s to feel %s clit grow hot, and start expanding! "
                                + "%s try to hold it back with your hands, but the growth continues until %s %s the proud owner of a new %s. "
                                + "The sensations from %s new maleness make %s tremble.",
                                user.subjectAction("stop"), Formatter.capitalizeFirstLetter(target.subjectAction("start")),
                                target.possessiveAdjective(),
                                Formatter.capitalizeFirstLetter(target.pronoun()), target.pronoun(), target.action("are", "is"),
                                target.body.getRandomCock().describe(target),
                                target.possessiveAdjective(), target.directObject());
            } else {
                message = String.format("%s moving and begins chanting. %s feel %s cock grow hot, and start expanding! "
                                + "%s try to hold it back with your hands, but the growth continues until it's much larger than before. "
                                + "The new sensations from %s new larger cock make %s tremble.",
                                user.subjectAction("stop"), Formatter.capitalizeFirstLetter(target.subjectAction("start")),
                                target.possessiveAdjective(),
                                Formatter.capitalizeFirstLetter(target.pronoun()),
                                target.possessiveAdjective(), target.directObject());
            }
            if (damage > 0) {
                message += " You realize the effects are permanent!";
            }
        }
        return message;
    }

}
