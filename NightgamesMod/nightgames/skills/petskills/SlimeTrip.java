package nightgames.skills.petskills;

import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.combat.Combat;
import nightgames.global.Formatter;
import nightgames.nskills.tags.SkillTag;
import nightgames.skills.Skill;
import nightgames.skills.Tactics;
import nightgames.status.Falling;

public class SlimeTrip extends SimpleEnemySkill {
    public SlimeTrip(CharacterType self) {
        super("Slime Trip", self);
        addTag(SkillTag.positioning);
        addTag(SkillTag.knockdown);
    }

    @Override
    public boolean usable(Combat c, Character target) {
        return super.usable(c, target) && !c.getStance().prone(target);
    }

    @Override
    public int getMojoCost(Combat c) {
        return 5;
    }

    @Override
    public int accuracy(Combat c, Character target) {
        return 50;
    }

    @Override
    public boolean resolve(Combat c, Character target) {
        if (target.roll(getSelf(), accuracy(c, target))) {
            c.write(getSelf(), Formatter.format("{other:SUBJECT-ACTION:slip|slips} on {self:name-do} as it clings to {other:possessive} feet, losing {other:possessive} balance.",
                            getSelf(), target));
            target.add(c, new Falling(target.getType()));
        } else {
            c.write(getSelf(), Formatter.format("{self:SUBJECT-ACTION:stumble|stumbles} as {self:subject} clings to {other:possessive} leg. "
                            + "{other:SUBJECT-ACTION:manage|manages} to catch {other:reflective} and {other:action:scrape|scrapes} off the clingy blob.",
                            getSelf(), target));
            return false;
        }
        return true;
    }

    @Override
    public Skill copy(Character user) {
        return new SlimeTrip(user.getType());
    }

    @Override
    public int speed() {
        return 8;
    }

    @Override
    public Tactics type(Combat c) {
        return Tactics.stripping;
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
