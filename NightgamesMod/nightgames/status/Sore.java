package nightgames.status;

import com.google.gson.JsonObject;
import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.Emotion;
import nightgames.characters.body.BodyPart;
import nightgames.combat.Combat;

public class Sore extends DurationStatus {
    private Sore(CharacterType affected, int duration) {
        super("Sore", affected, duration);

        flag(Stsflag.purgable);
        flag(Stsflag.debuff);
        flag(Stsflag.sore);
    }

    @Override
    public String initialMessage(Combat c, Status replacement) {
        return String.format("%s now sore.\n", getAffected().subjectAction("are", "is"));
    }

    @Override
    public String describe(Combat c) {
        return "";
    }

    @Override
    public float fitnessModifier() {
        return -1f;
    }

    @Override
    public int mod(Attribute a) {
        return 0;
    }

    @Override
    public int regen(Combat c) {
        super.regen(c);
        getAffected().emote(Emotion.nervous, 10);
        return -getAffected().getStamina().max() / 20;
    }

    @Override
    public int damage(Combat c, int x) {
        return 5;
    }

    @Override
    public double pleasure(Combat c, BodyPart withPart, BodyPart targetPart, double x) {
        return 0;
    }

    @Override
    public int weakened(Combat c, int x) {
        return x;
    }

    @Override
    public int tempted(Combat c, int x) {
        return 0;
    }

    @Override
    public int evade() {
        return -10;
    }

    @Override
    public int escape(Character from) {
        return -10;
    }

    @Override
    public int gainmojo(int x) {
        return -x / 2;
    }

    @Override
    public int spendmojo(int x) {
        return x * 2;
    }

    @Override
    public int counter() {
        return 0;
    }

    @Override
    public boolean lingering() {
        return true;
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public Status instance(Character newAffected, Character opponent) {
        return new Sore(newAffected.getType(), getDuration());
    }

    @Override  public JsonObject saveToJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getClass().getSimpleName());
        obj.addProperty("duration", getDuration());
        return obj;
    }

    @Override public Status loadFromJson(JsonObject obj) {
        return new Sore(null, obj.get("duration").getAsInt());
    }

}
