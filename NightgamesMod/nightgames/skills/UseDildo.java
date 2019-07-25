package nightgames.skills;

import nightgames.characters.Character;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Formatter;
import nightgames.global.Random;
import nightgames.items.Item;
import nightgames.nskills.tags.SkillTag;
import nightgames.skills.damage.DamageType;
import nightgames.stance.Stance;

public class UseDildo extends Skill {

    UseDildo() {
        super(Item.Dildo.getName());
        addTag(SkillTag.usesToy);
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return true;
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        return (user.has(Item.Dildo) || user.has(Item.Dildo2)) && user.canAct() && target.hasPussy()
                        && c.getStance().reachBottom(user) && target.crotchAvailable()
                        && !c.getStance().vaginallyPenetrated(c, target);
    }

    @Override
    public int accuracy(Combat c, Character user, Character target) {
        return c.getStance().en == Stance.neutral ? 50 : 100;
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target) {
        if (target.roll(user, accuracy(c, user, target))) {
            int m;
            if (user.has(Item.Dildo2)) {
                writeOutput(c, Result.upgrade, user, target);
                m = Random.random(10, 20);
            } else {
                writeOutput(c, Result.normal, user, target);
                m = Random.random(5, 15);
                
            }

            m = (int) DamageType.gadgets.modifyDamage(user, target, m);
            target.body.pleasure(user, null, target.body.getRandom("pussy"), m, c, new SkillUsage<>(this, user, target));
        } else {
            writeOutput(c, Result.miss, user, target);
            return false;
        }
        return true;
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.pleasure;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.miss) {
            return "You try to slip a dildo into " + target.getName() + ", but she blocks it.";
        } else if (modifier == Result.upgrade) {
            return "You touch the imperceptibly vibrating dildo to " + target.getName()
                            + "'s love button and she jumps as if shocked. Before she can defend herself, you "
                            + "slip it into her " + target.body.getRandomPussy().describe(target)
                            + ". She starts moaning in pleasure immediately.";
        } else {
            return "You rub the dildo against " + target.getName()
                            + "'s lower lips to lubricate it before you thrust it inside her. She can't help moaning a little as you "
                            + "pump the rubber toy in and out of her " + target.body.getRandomPussy().describe(target)
                            + ".";
        }
    }

    @Override

    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        if (modifier == Result.miss) {
            return Formatter.format(
                            "{self:SUBJECT-ACTION:try|tries} to slip a dildo into {other:name-do}, but {other:pronoun-action:block|blocks} it.",
                            user, target);
        } else if (modifier == Result.upgrade) {
            return Formatter.format(
                            "{self:SUBJECT-ACTION:touch|touches} the imperceptibly vibrating dildo to {other:possessive} love button and {other:subject-action:jump|jumps} as if shocked. Before {other:subject} can defend {other:reflective}, {self:subject} "
                                            + "slips it into {other:possessive} {other:body-part:pussy}. {other:SUBJECT-ACTION:start|starts} moaning in pleasure immediately.",
                            user, target);
        } else {
            return Formatter.format(
                            "{self:SUBJECT-ACTION:rub|rubs} the dildo against {other:name-possessive} lower lips to lubricate it before {self:pronoun-action:thrust|thrusts} it inside {other:name-do}. "
                                            + "{other:SUBJECT} can't help but moan a little as {self:subject-action:pump|pumps} the rubber toy in and out of {other:possessive} {other:body-part:pussy}.",
                            user, target);
        }
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Pleasure opponent with your dildo";
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
