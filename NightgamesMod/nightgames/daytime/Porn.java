package nightgames.daytime;

import nightgames.characters.Character;
import nightgames.characters.Trait;
import nightgames.global.Configuration;
import nightgames.global.Flag;
import nightgames.global.GameState;
import nightgames.global.Random;
import nightgames.gui.GUI;

import java.util.ArrayList;

public class Porn extends Activity {
    public Porn(Character player) {
        super("Browse Porn Sites", player);
    }

    @Override
    public boolean known() {
        return Flag.checkFlag(Flag.metBroker);
    }

    @Override
    public void visit(String choice) {
        GUI.gui.clearText();
        if (page == 0) {
            int gain = gainArousal(player);
            showScene(pickScene(gain));
            next(GUI.gui);
            GUI.gui.message("<b>Your maximum arousal has increased by " + gain + ".</b>");
        } else {
            done(true);
        }
    }

    private int gainArousal(Character self) {
        int maximumArousalForLevel = Configuration.getMaximumArousalPossible(self);
        int gain = 1 + Random.random(2);
        if (player.has(Trait.expertGoogler)) {
            gain = gain + Random.random(2);
        }
        gain = (int) Math.max(0, (int) Math.min(maximumArousalForLevel, self.getArousal().trueMax() + gain) - self.getArousal().trueMax());
        self.getArousal().gain(gain);
        return gain;
    }

    @Override
    public void shop(Character npc, int budget) {
        gainArousal(npc);
    }

    private void showScene(Scene chosen) {
        switch (chosen) {
            case basic3:
                GUI.gui.message(
                                "You watch a nude 'audition' by a self-proclaimed aspiring actress. If she can't fake a better orgasm than that, you can see why her career isn't going anywhere.");
                break;
            case basic1:
                GUI.gui.message(
                                "You spend about an hour browsing fetish porn websites. Some things do not need to be inserted into the human body.");
                break;
            case basic2:
                GUI.gui.message(
                                "You spend about an hour browsing fetish porn websites. You feel a bit more desensitized to normal sex and a little bit dead inside.");
                break;
            case none:
                GUI.gui.message(
                                "You try find something arousing on the internet, but nothing seems sexy anymore. You've probably done this too much recently. (Need to raise your level more).");
                break;
            case fail1:
                GUI.gui.message(
                                "It feels like the internet has run out of sexy. There's nothing new worth fapping to. Maybe there's something decent behind this paywall? No, don't do it. It's a trap. (Need to raise your level more).");
                break;
            case mara1:
                GUI.gui.message(
                                "You were planning to browse some porn and probably rub one out, but why is Mara in your room? <i>\"Don't sweat the details. I brought you this new porn game so we could play it together. I even saved you some time by making a custom girl who looks like me.\"</i>");

                break;
            case angel1:
                GUI.gui.message(
                                "When Angel invited you to watch a movie with her friends, you did not expect it to be porn. In retrospect, you probably should have. Caroline and Sarah have claimed comfortable looking arm chairs, while you, Angel and Mei are packed together on a small sofa. Mei grins at you suggestively. <i>\"If you need to whip it out and jerk off, we'll pretend not to notice.\"</i> That's kinda considerate of her, but it's probably not going to be an option. Angel already has her hand down your pants.");

                break;
            case reyka1:
                GUI.gui.message(
                                "You stumble onto a webcam of a girl who specializes in fantasy roleplay. Wait... is that Reyka? That's definitely Reyka. Can she absorb libido over the internet?");
        }
    }

    private Scene pickScene(int gain) {
        ArrayList<Scene> available = new ArrayList<Scene>();
        if (gain == 0) {
            available.add(Scene.none);            
            available.add(Scene.fail1);
        } else {
            available.add(Scene.basic1);
            available.add(Scene.basic2);
            available.add(Scene.basic3);
            if (GameState.gameState.characterPool.getNPC("Mara").getAffection(player) >= 5) {
                available.add(Scene.mara1);
            }
            if (GameState.gameState.characterPool.getNPC("Angel").getAffection(player) >= 10) {
                available.add(Scene.angel1);
            }
            if (Flag.checkFlag(Flag.Reyka) && GameState.gameState.characterPool.getNPC("Reyka").getAffection(player) >= 1) {
                available.add(Scene.reyka1);
            }
        }
        return available.get(Random.random(available.size()));
    }

    private static enum Scene {
        basic1,
        basic2,
        basic3,
        none,
        fail1,
        mara1,
        angel1,
        reyka1;
    }
}
