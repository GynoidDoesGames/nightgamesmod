package nightgames.status;

import com.google.gson.JsonObject;
import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.Emotion;
import nightgames.characters.body.BodyPart;
import nightgames.combat.Combat;

public class Distorted extends DurationStatus {
    public Distorted(CharacterType affected, int duration) {
        super("Distorted", affected, duration);
        flag(Stsflag.distorted);
        flag(Stsflag.purgable);
    }

    @Override
    public String describe(Combat c) {
        if (getAffected().human()) {
            return "Your image is distorted, making you hard to hit.";
        } else {
            return "Multiple " + getAffected().getName()
                            + "s appear in front of you. When you focus, you can tell "
                            + "which one is real, but it's still screwing up "+getAffected().nameOrPossessivePronoun()+" accuracy.";
        }
    }

    @Override
    public String initialMessage(Combat c, Status replacement) {
        return String.format("%s image is now distorted.\n", getAffected().nameOrPossessivePronoun());
    }

    @Override
    public float fitnessModifier() {
        return 1;
    }

    @Override
    public int mod(Attribute a) {
        return 0;
    }

    @Override
    public int regen(Combat c) {
        super.regen(c);
        getAffected().emote(Emotion.confident, 5);
        return 0;
    }

    @Override
    public int damage(Combat c, int x) {
        return 0;
    }

    @Override
    public double pleasure(Combat c, BodyPart withPart, BodyPart targetPart, double x) {
        return 0;
    }

    @Override
    public int weakened(Combat c, int x) {
        return 0;
    }

    @Override
    public int tempted(Combat c, int x) {
        return 0;
    }

    @Override
    public int evade() {
        return 10;
    }

    @Override
    public int escape(Character from) {
        return 0;
    }

    @Override
    public int gainmojo(int x) {
        return 0;
    }

    @Override
    public int spendmojo(int x) {
        return 0;
    }

    @Override
    public int counter() {
        return 0;
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public Status instance(Character newAffected, Character newOther) {
        return new Distorted(newAffected.getType(), getDuration());
    }

    @Override  public JsonObject saveToJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getClass().getSimpleName());
        obj.addProperty("duration", getDuration());
        return obj;
    }

    @Override public Status loadFromJson(JsonObject obj) {
        return new Distorted(null, obj.get("duration").getAsInt());
    }
}
