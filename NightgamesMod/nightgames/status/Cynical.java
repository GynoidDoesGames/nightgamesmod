package nightgames.status;

import com.google.gson.JsonObject;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.CharacterType;
import nightgames.characters.body.BodyPart;
import nightgames.combat.Combat;

public class Cynical extends DurationStatus {
    public Cynical(CharacterType affected) {
        super("Cynical", affected, 3);
        flag(Stsflag.cynical);
    }

    @Override
    public String describe(Combat c) {
        if (affected.human()) {
            return "You're feeling more cynical than usual and won't fall for any mind games.";
        } else {
            return affected.getName() + " has a cynical edge in "+affected.possessiveAdjective()+" eyes.";
        }
    }

    @Override
    public String initialMessage(Combat c, Status replacement) {
        return String.format("%s now cynical towards future mind games.\n", affected.subjectAction("are", "is"));
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
        return -Math.min(5, x / 4);
    }

    @Override
    public int evade() {
        return 0;
    }

    @Override
    public int escape() {
        return 0;
    }

    @Override
    public int gainmojo(int x) {
        return -x/4;
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
        return new Cynical(newAffected);
    }

    @Override  public JsonObject saveToJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", getClass().getSimpleName());
        return obj;
    }

    @Override public Status loadFromJson(JsonObject obj) {
        return new Cynical(null);
    }
}
