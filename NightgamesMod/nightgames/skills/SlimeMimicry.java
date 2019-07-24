package nightgames.skills;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.trait.Trait;
import nightgames.combat.Combat;
import nightgames.combat.Result;
import nightgames.global.Random;
import nightgames.status.Stsflag;

import java.util.Arrays;
import java.util.List;

public class SlimeMimicry extends Skill {
    private final static List<Skill> MIMICRY_SKILLS = Arrays.asList(
                    new MimicAngel(),
                    new MimicCat(),
                    new MimicDryad(),
                    new MimicSuccubus(),
                    new MimicWitch()
                    );
    
    SlimeMimicry() {
        super("Slime Mimicry");
    }

    @Override
    public boolean requirements(Combat c, Character user, Character target) {
        return user.get(Attribute.slime) >= 10 && !user.human() && user.has(Trait.Imposter);
    }

    @Override
    public boolean usable(Combat c, Character user, Character target) {
        return user.canRespond() && !user.is(Stsflag.mimicry);
    }

    @Override
    public String describe(Combat c, Character user) {
        return "Mimics a random NPC";
    }

    @Override
    public boolean resolve(Combat c, Character user, Character target) {
        return Random.pickRandomGuaranteed(MIMICRY_SKILLS).copy(user).resolve(c, user, target);
    }

    @Override
    public Skill copy(Character user) {
        return new SlimeMimicry();
    }

    @Override
    public Tactics type(Combat c, Character user) {
        return Tactics.positioning;
    }

    @Override
    public String deal(Combat c, int damage, Result modifier, Character user, Character target) {
        return "";
    }

    @Override
    public String receive(Combat c, int damage, Result modifier, Character user, Character target) {
        return "";
    }

}
