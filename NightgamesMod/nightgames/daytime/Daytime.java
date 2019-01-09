package nightgames.daytime;

import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.NPC;
import nightgames.characters.Player;
import nightgames.global.Flag;
import nightgames.global.GameState;
import nightgames.global.Random;
import nightgames.global.Time;
import nightgames.gui.GUI;
import nightgames.gui.LabeledValue;
import nightgames.status.addiction.Addiction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Daytime {
    private final int MORNING_TIME = 10;
    private final int NEXT_MATCH_TIME = 22;
    private final int LONG_DAY_LENGTH = 10;
    private final int SHORT_DAY_LENGTH = 7;
    private final int NOON = 12;
    private final int AFTER_CLASS = 15;
    public static Daytime day;
    private ArrayList<Activity> activities;
    private Player player;
    int time;
    private int dayLength;
    private DaytimeEventManager eventMgr;

    public Daytime(Player player) {
        this.player = player;
        this.eventMgr = new DaytimeEventManager(player);
        buildActivities();
        if (Flag.checkFlag(Flag.metAlice)) {
            if (Flag.checkFlag(Flag.victory)) {
                Flag.unflag(Flag.AliceAvailable);
            } else {
                Flag.flag(Flag.AliceAvailable);
            }
        }
        if (Flag.checkFlag(Flag.YuiLoyalty)) {
            Flag.flag(Flag.YuiAvailable);
        }
        if (Flag.checkFlag(Flag.YuiUnlocking)) {
            Flag.unflag(Flag.YuiUnlocking);
        }
        
        Flag.unflag(Flag.threesome);
        time = MORNING_TIME;
        // do NPC day length
        if (Time.isWeekend()) {
            dayLength = LONG_DAY_LENGTH;
        } else {
            dayLength = SHORT_DAY_LENGTH;
        }
    }

    public static Daytime getDay() {
        return day;
    }

    private void morning() {
        GUI.gui.clearText();
        GameState.gameState.characterPool.getPlayer().getAddictions().forEach(Addiction::clearDaytime);
        GameState.gameState.characterPool.getPlayer().getAddictions().stream().map(Addiction::describeMorning).forEach(
                        s -> GUI.gui.message(s));
        if (eventMgr.playMorningScene()) {
            time = NOON;
        } else if (player.getLevel() >= 10 && player.getRank() == 0) {
            GUI.gui.message("The next day, just after getting out of class you receive call from a restricted number. Normally you'd just ignore it, "
                            + "but for some reason you feel compelled to answer this one. You're greeted by a man with a clear deep voice. <i>\"Hello "
                            + player.getTrueName() + ", and "
                            + "congratulations. Your performance in your most recent matches has convinced me you're ready for a higher level of play. You're promoted to "
                            + "ranked status, effective immediately. This new status earns you a major increase in monetary rewards and many new opportunities. I'll leave "
                            + "the details to others. I just wanted to congratulate you personally.\"</i> Wait, wait. That's not the end of the call. This guy is clearly "
                            + "someone overseeing the Game, but he hasn't even given you his name. Why all the secrecy? <i>\"If you're looking for more information, you "
                            + "know someone who sells it.\"</i> There's a click and the call ends.");
            player.rankup();
            time = AFTER_CLASS;
        } else if (player.getLevel() >= 20 && player.getRank() == 1) {
            GUI.gui.message("In the morning, you receive a call from a restricted number. You have a pretty decent guess who it might be. Hopefully it is good news. <i>\"Hello again "
                            + player.getTrueName()
                            + ".\"</i> You were right, that voice is pretty hard to forget. <i>\"I am impressed. You and your opponents are "
                            + "all quickly adapting to what most people would consider an extraordinary situation. If you are taking advantage of the people and services available "
                            + "to you, you could probably use more money. Therefore, I am authorizing another pay increase. Congratulations.\"</i> This is the mysterious Benefactor "
                            + "everyone keeps referring to, right? Is he ever planning to show himself in person? What is he getting out of all this? <i>\"Your curiosity is admirable. "
                            + "Keep searching. If you have as much potential as I think you do, we'll meet soon enough.\"</i>");
            player.rankup();
            time = AFTER_CLASS;
        } else if (player.getLevel() >= 30 && player.getRank() == 2) {
            GUI.gui.message("In the morning, you receive a call from a restricted number. You are not at all surprised to hear the voice of your anonymous Benefactor again. It did seem about time for him to call again. <i>\"Hello "
                            + player.getTrueName() + ". Have you been keeping busy? You've been putting "
                            + "on a good show in your matches, but when we last spoke, you had many questions. Are you any closer to finding your answers?\"</i><br/><br/>"
                            + "That's an odd question since it depends on whether or not he has become more willing to talk. Who else is going to fill you in about this "
                            + "apparently clandestine organization. <i>\"Oh don't become lazy now. I chose you for this Game, in part, for your drive and initiative. Are "
                            + "you limited to just the information that has been handed to you? Just because Aesop does not have the answers for sale does not mean there "
                            + "are no clues. Will you simply give up?\"</i><br/><br/>"
                            + "You know he's trying to provoke you, but it's working anyway. If he's offering a challenge, you'll show him you can track him down. The next "
                            + "time you speak to this Benefactor, it will be in person. <i>\"Excellent!\"</i> His voice has only a trace of mockery in it. <i>\"You are "
                            + "already justifying your new rank, which is what I am calling you about, incidentally. Perhaps you can put your increased pay rate or the trust "
                            + "you've built with your opponents to good use. Well then, I shall wait to hear from you this time.\"</i> There's a click and the call ends.");
            player.rankup();
            time = AFTER_CLASS;
        } else if (player.getLevel() / 10 > player.getRank()) {
            GUI.gui.message("You have advanced to rank " + ++player.rank + "!");
            player.rankup();
            time = AFTER_CLASS;
        } else if (Time.isWeekend()) {
            GUI.gui.message("You don't have any classes today, but you try to get up at a reasonable hour so you can make full use of your weekend.");
            time = NOON;
        } else {
            GUI.gui.message("You try to get as much sleep as you can before your morning classes.<br/><br/>You're done with classes by mid-afternoon and have the rest of the day free.");
            time = AFTER_CLASS;
        }
    }

    public void dayLoop() {
        morning();
        while (time < NEXT_MATCH_TIME) {
            GUI.gui.message(String.format("It is currently %s. Your next match starts at %s.", displayTime(), displayTime(NEXT_MATCH_TIME)));
            if (eventMgr.playRegularScene()) {
                break;
            }
            List<LabeledValue<Activity>> activityButtonLabels = activities.stream().filter(Activity::known)
                            .filter(activity -> activity.time() + time <= NEXT_MATCH_TIME)
                            .map(activity -> new LabeledValue<>(activity, activity.name)).collect(Collectors.toList());
            Activity activity = null;
            try {
                activity = GUI.gui.promptFuture(activityButtonLabels).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (activity != null) {
                Activity.ActivityInstance activityInstance = new Activity.ActivityInstance(activity);
                while (!activityInstance.finished) {
                    List<LabeledValue<String>> availableChoices = activityInstance.visit();
                    if (activityInstance.finished) {
                        break;
                    } else if (availableChoices.isEmpty()) {
                        throw new RuntimeException("No scene choices available for daytime activity!");
                    } else {
                        try {
                            activityInstance.currentChoice = GUI.gui.promptFuture(availableChoices).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                        activityInstance.next();
                    }
                }
                if (activityInstance.acted) {
                    advance(activityInstance.activity.time());
                }
                GUI.gui.clearImage();
                GUI.gui.clearPortrait();
            } else {
                System.err.println("No activity selected!");
                time++;
            }
        }
        for (NPC npc : GameState.gameState.characterPool.allNPCs()) {
            npcDaytime(npc);
        }
    }

    public void npcDaytime(NPC npc) {
        if (npc.getLevel() > npc.getRank() * 10) {
            npc.rankup();
        }
        npc.daytime(dayLength);
    }

    private void buildActivities() {
        activities = new ArrayList<>();
        activities.add(new Exercise(player));
        activities.add(new Porn(player));
        activities.add(new VideoGames(player));
        activities.add(new Informant(player));
        activities.add(new BlackMarket(player));
        activities.add(new BodyShop(player));
        activities.add(new XxxStore(player));
        activities.add(new HWStore(player));
        activities.add(new Bookstore(player));
        activities.add(new Meditation(player));
        activities.add(new AngelTime(player));
        activities.add(new AiriTime(player));
        activities.add(new CassieTime(player));
        activities.add(new JewelTime(player));
        activities.add(new MaraTime(player));
        if (Flag.checkFlag(Flag.Kat)) {
            activities.add(new KatTime(player));
        }
        activities.add(new Closet(player));
        activities.add(new ClothingStore(player));
        activities.add(new Boutique(player));
        if (Flag.checkFlag(Flag.Reyka)) {
            activities.add(new ReykaTime(player));
        }
        if (Flag.checkFlag(Flag.YuiLoyalty)) {
            activities.add(new YuiTime(player));
        }
        activities.add(new MagicTraining(player));
        activities.add(new Workshop(player));
        activities.add(new AddictionRemoval(player));
    }

    public String getTime() {
        return displayTime();
    }

    public void advance(int t) {
        time += t;
        buildActivities();
    }

    public static void train(Character one, Character two, Attribute att) {
        int a;
        int b;
        if (one.getPure(att) > two.getPure(att)) {
            a = 100 - 2 * one.get(Attribute.Perception);
            b = 90 - 2 * two.get(Attribute.Perception);
        } else if (one.getPure(att) < two.getPure(att)) {
            a = 90 - 2 * one.get(Attribute.Perception);
            b = 100 - 2 * two.get(Attribute.Perception);
        } else {
            a = 100 - 2 * one.get(Attribute.Perception);
            b = 100 - 2 * two.get(Attribute.Perception);
        }
        if (Random.random(100) >= a) {
            one.modAttributeDontSaveData(att, 1);
            if (one.human()) {
                GUI.gui
                      .message("<b>Your " + att + " has improved.</b>");
            }
        }
        if (Random.random(100) >= b) {
            two.modAttributeDontSaveData(att, 1);
            if (two.human()) {
                GUI.gui
                      .message("<b>Your " + att + " has improved.</b>");
            }
        }
    }

    public void visit(String name, NPC npc, int budget) {
        for (Activity a : activities) {
            if (a.toString().equalsIgnoreCase(name)) {
                a.shop(npc, budget);
                break;
            }
        }
    }

    private String displayTime() {
        return displayTime(this.time);
    }

    private String displayTime(int time) {
        if (time == 0) {
            return "12:00am";
        } else if (time < 12) {
            return time + ":00am";
        } else if (time == 12) {
            return time + ":00pm";
        } else {
            return time - 12 + ":00pm";
        }
    }

}
