package nightgames.status;

import com.google.gson.JsonObject;
import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.body.BodyPart;
import nightgames.combat.Combat;

public class Satiated extends DurationStatus {
    int value;

    public Satiated(CharacterType affected, int xp, int levels) {
        super("Satiated", affected, 1);
        value = xp + 95 + 5 * (affected.fromPoolGuaranteed().getLevel() + levels);
    }

    public Satiated(CharacterType affected, int value) {
        super("Satiated", affected, 1);
        this.value = value;
    }

    @Override
    public String describe(Combat c) {
        if (getAffected().human()) {
            return "You feel immensely powerful after feeding on your opponent's essence\n";
        } else {
            return getAffected().getName() + " feels immensely satisfied after feeding on "+
                            c.getOpponent(getAffected()).nameOrPossessivePronoun()+" essence\n";
        }
    }

    @Override
    public String initialMessage(Combat c, Status replacement) {
        return String.format("%s now satiated.\n", getAffected().subjectAction("are", "is"));
    }

    @Override
    public float fitnessModifier() {
        return value / 10;
    }

    @Override
    public int mod(Attribute a) {
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
        return 0;
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
    public Status instance(Character newAffected, Character opponent) {
        return new Satiated(newAffected.getType(), value);
    }

    @Override  public JsonObject saveToJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getClass().getSimpleName());
        obj.addProperty("value", value);
        return obj;
    }

    @Override public Status loadFromJson(JsonObject obj) {
        return new Satiated(null, obj.get("value").getAsInt());
    }
}
