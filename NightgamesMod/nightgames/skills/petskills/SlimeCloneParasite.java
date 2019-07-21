package nightgames.skills.petskills;

import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.body.BodyPart;
import nightgames.characters.body.mods.ParasitedMod;
import nightgames.characters.trait.Trait;
import nightgames.combat.Combat;
import nightgames.global.Formatter;
import nightgames.global.Random;
import nightgames.nskills.tags.SkillTag;
import nightgames.pet.PetCharacter;
import nightgames.skills.Skill;
import nightgames.skills.Tactics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SlimeCloneParasite extends SimpleEnemySkill {
    public SlimeCloneParasite(CharacterType self) {
        super("Parasitism", self);
        addTag(SkillTag.debuff);
    }

    public float priorityMod(Combat c) {
        return 10.0f;
    }

    @Override
    public int getMojoCost(Combat c) {
        return 25;
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return super.requirements(c, user, target) && user instanceof PetCharacter && ((PetCharacter)user).getSelf().owner().has(Trait.MimicBodyPart);
    }

    private final static List<String> PARASITEABLE_PARTS = Arrays.asList("cock", "pussy", "ass", "mouth");
    @Override
    public boolean resolve(Combat c, Character target) {
        if (target.roll(getSelf(), accuracy(c, target))) {
            List<BodyPart> possibleTargets = new ArrayList<>();
            for (String type : PARASITEABLE_PARTS) {
                if (!target.body.getRandom(type).moddedPartCountsAs(target, ParasitedMod.INSTANCE)) {
                    possibleTargets.add(target.body.getRandom(type));
                }
            }
            Optional<BodyPart> result = Random.pickRandom(possibleTargets);
            if (result.isPresent()) {
                BodyPart targetPart = result.get();
                c.write(getSelf(), Formatter.format("{self:SUBJECT-ACTION:launch} {self:reflective} at {other:name-possessive} %s. "
                                + "{other:PRONOUN-ACTION:try} to dodge out of the way, but it's no use. "
                                + "{self:NAME-POSSESSIVE} gelatinous body has deformed around {other:possessive} %s and manages "
                                + "to crawl inside {other:possessive} body somehow!",
                                getSelf(), target, targetPart.describe(target), targetPart.getType()));
                target.body.temporaryAddPartMod(targetPart.getType(), ParasitedMod.INSTANCE, 10);
            }
            return true;
        }
        c.write(getSelf(), Formatter.format("{self:SUBJECT-ACTION:launch} {self:reflective} at {other:name-do}, but {other:pronoun-action:dodge} away in time.", getSelf(), target));
        return false;
    }

    @Override
    public Skill copy(Character user) {
        return new SlimeCloneParasite(user.getType());
    }

    @Override
    public int speed() {
        return 8;
    }

    @Override
    public Tactics type(Combat c) {
        return Tactics.pleasure;
    }

    @Override
    public boolean makesContact() {
        return true;
    }
}
