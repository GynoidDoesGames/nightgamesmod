package nightgames.skills;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.Emotion;
import nightgames.characters.body.BreastsPart;
import nightgames.characters.body.TentaclePart;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Formatter;
import nightgames.global.Random;
import nightgames.status.Bound;
import nightgames.status.Oiled;
import nightgames.status.Stsflag;

public class TentaclePorn extends Skill {

    TentaclePorn() {
        super("Tentacle Porn");
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return user.getAttribute(Attribute.fetishism) >= 12;
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        return !target.wary() && !c.getStance().sub(user) && !c.getStance().prone(user)
                        && !c.getStance().prone(target) && user.canAct() && user.getArousal().get() >= 20;
    }

    @Override
    public int getMojoCost(Combat c, Character user) {
        return 10;
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Create a bunch of hentai tentacles.";
    }

    @Override
    public int accuracy(Combat c, Character user, Character target) {
        return 90;
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target) {
        if (target.roll(user, accuracy(c, user, target))) {
            if (target.mostlyNude()) {
                int m = Random.random(user.getAttribute(Attribute.fetishism)) / 2 + 1;
                if (target.bound()) {
                    writeOutput(c, Result.special, user, target);
                    if (target.hasDick())
                        TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandomCock());
                    if (target.hasPussy())
                        TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandomPussy());
                    TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandomBreasts());
                    TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandomAss());
                } else if (user.human()) {
                    c.write(user, deal(c, 0, Result.normal, user, target));
                    TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandom("skin"));
                } else if (c.shouldPrintReceive(target, c)) {
                    c.write(user, receive(c, 0, Result.normal, user, target));
                    TentaclePart.pleasureWithTentacles(c, target, m, target.body.getRandom("skin"));
                }
                if (!target.is(Stsflag.oiled)) {
                    target.add(c, new Oiled(target.getType()));
                }
                target.emote(Emotion.horny, 20);
            } else {
                writeOutput(c, Result.weak, user, target);
            }
            target.add(c, new Bound(target.getType(), 30 + 2 * Math.sqrt(user.getAttribute(Attribute.fetishism) + user.getAttribute(Attribute.slime)), "tentacles"));
        } else {
            writeOutput(c, Result.miss, user, target);
            return false;
        }
        return true;
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.positioning;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.miss) {
            return "You summon a mass of tentacles that try to snare " + target.getName()
                            + ", but she nimbly dodges them.";
        } else if (modifier == Result.weak) {
            return "You summon a mass of phallic tentacles that wrap around " + target.getName()
                            + "'s arms, holding her in place.";
        } else if (modifier == Result.normal) {
            return "You summon a mass of phallic tentacles that wrap around " + target.getName()
                            + "'s naked body. They squirm against her and squirt slimy fluids on her body.";
        } else {
            return "You summon tentacles to toy with " + target.getName()
                            + "'s helpless form. The tentacles toy with her breasts and penetrate her pussy and ass.";
        }
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.miss) {
            return String.format("%s stomps on the ground and a bundle of tentacles erupt from the "
                            + "ground. %s barely able to avoid them.", user.subject(),
                            Formatter.capitalizeFirstLetter(target.subjectAction("are", "is")));
        } else if (modifier == Result.weak) {
            return String.format("%s stomps on the ground and a bundle of tentacles erupt from the "
                            + "ground around %s, entangling %s arms and legs.", user.subject(),
                            target.nameDirectObject(), target.possessiveAdjective());
        } else if (modifier == Result.normal) {
            return String.format("%s stomps on the ground and a bundle of tentacles erupt from the "
                            + "ground around %s, entangling %s arms and legs. The slimy appendages "
                            + "wriggle over %s body and coat %s in the slippery liquid.",
                            user.subject(), target.nameDirectObject(), target.possessiveAdjective(),
                            target.possessiveAdjective(), target.directObject());
        } else {
            String actions = "";
            if (target.hasDick())
                actions += String.format("tease %s %s", target.possessiveAdjective(), 
                                target.body.getRandomCock().describe(target));
            
            if (target.hasPussy())
                actions += String.format("%scaress %s clit", actions.length() > 0 ? ", " : "", 
                                target.possessiveAdjective());
            
            if (target.body.getRandomBreasts() != BreastsPart.flat)
                actions += String.format("%sknead %s %s" ,actions.length() > 0 ? ", " : "", 
                                target.possessiveAdjective(),
                                target.body.getRandomBreasts().describe(target));
            
            if (actions.length() > 0)
                actions += ", and";
            return String.format("%s summons slimy tentacles that cover %s helpless body,"
                            + " %s probe %s ass.", user.subject(),
                            target.nameOrPossessivePronoun(), actions,
                            target.possessiveAdjective());
        }
    }
}
