package nightgames.skills.petskills;

import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.Emotion;
import nightgames.combat.Combat;
import nightgames.global.Formatter;
import nightgames.global.Random;
import nightgames.nskills.tags.SkillTag;
import nightgames.skills.Skill;
import nightgames.skills.Tactics;
import nightgames.skills.damage.DamageType;
import nightgames.status.Stsflag;

public class FairyKick extends SimpleEnemySkill {
    public FairyKick(CharacterType self) {
        super("Fairy Kick", self);
        addTag(SkillTag.staminaDamage);
        addTag(SkillTag.positioning);
        addTag(SkillTag.hurt);
    }

    @Override
    public boolean usable(Combat c, Character target) {
        return super.usable(c, target) && target.stunned() && target.is(Stsflag.braced);
    }

    @Override
    public int getMojoBuilt(Combat c) {
        return 5;
    }

    @Override
    public boolean resolve(Combat c, Character target) {
        if (target.roll(getSelf(), accuracy(c, target))) {
            int m = 3 + getSelf().getLevel() + Random.random(5);
            c.write(getSelf(), Formatter.format("{self:SUBJECT-ACTION:fly|flies} at {other:direct-object} and kicks {other:direct-object} in the balls. "
                            + "{self:PRONOUN} doesn't have a lot of weight to put behind it, but it still hurts like hell.", getSelf(), target));
            target.pain(c, getSelf(), (int) DamageType.physical.modifyDamage(getSelf(), target, m));
            target.emote(Emotion.nervous, 10);
            target.emote(Emotion.angry, 10);
        } else {
            c.write(getSelf(), String.format("%s tries to kick %s but %s %s %s small legs before they reach %s.",
                            getSelf().subject(), target.nameDirectObject(),
                            target.pronoun(), target.action("catch", "catches"),
                            getSelf().possessiveAdjective(),
                            target.directObject()));
            return false;
        }
        return true;
    }

    @Override
    public Skill copy(Character user) {
        return new FairyKick(user.getType());
    }

    @Override
    public int speed() {
        return 8;
    }

    @Override
    public Tactics type(Combat c) {
        return Tactics.damage;
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
