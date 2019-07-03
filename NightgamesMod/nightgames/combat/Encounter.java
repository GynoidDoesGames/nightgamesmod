package nightgames.combat;

import nightgames.actions.Movement;
import nightgames.areas.Area;
import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.State;
import nightgames.characters.trait.Trait;
import nightgames.global.DebugFlags;
import nightgames.global.Formatter;
import nightgames.global.Match;
import nightgames.global.Random;
import nightgames.gui.GUI;
import nightgames.items.Item;
import nightgames.status.*;
import nightgames.trap.Spiderweb;
import nightgames.trap.Trap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * An Encounter is a meeting between two or more characters in one area during a match.
 */
public class Encounter implements Serializable {

    private static final long serialVersionUID = 3122246133619156539L;

    private List<Character> participants;
    protected Area location;
    protected transient Combat fight;
    private CountDownLatch waitForFinish;
    private List<Character> faster;

    // TODO: Figure out what to do with encounters involving more than three characters.
    public Encounter(Area location) {
        this.location = location;
        participants = new ArrayList<>(location.present);
        assert participants.size() >= 2;
        fight = null;
        checkEnthrall(getP1(), getP2());
        checkEnthrall(getP2(), getP1());
        waitForFinish = new CountDownLatch(1);
        faster = faster(participants);
    }

    public Character getP1() {
        return participants.get(0);
    }

    public Character getP2() {
        return participants.get(1);
    }

    private boolean observed() {
        return participants.stream().anyMatch(Character::human);
    }

    private void messageIfObserved(String message) {
        messageIfObserved(message, GUI.gui);
    }

    private void messageIfObserved(String message, GUI gui) {
        if (observed()) {
            gui.message(message);
        }
    }

    private Optional<Character> getIntervener() {
        if (participants.size() > 2) {
            return Optional.of(participants.get(2));
        }
        return Optional.empty();
    }

    public List<Character> getExtras() {
        List<Character> extras = new ArrayList<>();
        if (participants.size() > 3) {
            extras.addAll(participants.subList(3, participants.size()));
        }
        return extras;
    }

    public void intervene(Character intervener) {
        assert participants.size() == 2;
        participants.add(intervener);
    }

    public enum Initiation {
        ambushStrip,
        ambushRegular
    }

    protected void checkEnthrall(Character p1, Character p2) {
        Status enthrall = p1.getStatus(Stsflag.enthralled);
        if (enthrall != null) {
            if (((Enthralled) enthrall).master != p2) {
                p1.removelist.add(enthrall);
                p1.addNonCombat(new Flatfooted(p1, 2));
                p1.addNonCombat(new Hypersensitive(p1));
                if (p1.human()) {
                    messageIfObserved( "At " + p2.getName() + "'s interruption, you break free from the"
                                    + " succubus' hold on your mind. However, the shock all but"
                                    + " short-circuits your brain; you "
                                    + " collapse to the floor, feeling helpless and"
                                    + " strangely oversensitive");
                } else if (p2.human()) {
                    messageIfObserved(p1.getName() + " doesn't appear to notice you at first, but when you "
                                    + "wave your hand close to her face her eyes open wide and"
                                    + " she immediately drops to the floor. Although the display"
                                    + " leaves you somewhat worried about her health, she is"
                                    + " still in a very vulnerable position and you never were"
                                    + " one to let an opportunity pass you by.");
                }
            }
        }
    }

    /**
     * Prompts characters for responses to the encounter, depending on the state of other present characters. Creates an active Combat if applicable.
     */
    protected void spotCheck() {
        final Character p1 = getP1();
        final Character p2 = getP2();
        // If both players are eligible, first check for various one-sided encounters. Second, see who's observant enough
        // to spot the other. If both spot each other, both face off (decide fight or flight). If only one spots the other,
        // the other starts flat-footed. If neither spot each other, they move on, no one the wiser.
        if (p1.eligible(p2) && p2.eligible(p1)) {
            Encs encounterType;
            Character attacker;
            Character target;
            if (p2.isVulnerable()) {
                attacker = p1;
                target = p2;
                encounterType = vulnerable(attacker, target);
            } else if (p1.isVulnerable()) {
                attacker = p2;
                target = p1;
                encounterType = vulnerable(attacker, target);
            } else {
                boolean p1SpotCheck = p1.spotCheck(p2);
                boolean p2SpotCheck = p2.spotCheck(p1);
                if (p1SpotCheck && p2SpotCheck) {
                    Character.FightIntent p1Intent = p1.faceOff(p2, this);
                    Character.FightIntent p2Intent = p2.faceOff(p1, this);

                    if (p1Intent == Character.FightIntent.smoke) {
                        attacker = p2;
                        target = p1;
                        encounterType = Encs.smoke;
                    } else if (p2Intent == Character.FightIntent.smoke) {
                        attacker = p1;
                        target = p2;
                        encounterType = Encs.smoke;
                    } else if (p1Intent == Character.FightIntent.flee
                                    && p2Intent == Character.FightIntent.flee) {
                        encounterType = Encs.bothflee;
                        attacker = faster.get(0);
                        target = faster.get(1);
                    } else if (p1Intent == Character.FightIntent.fight && p2Intent == Character.FightIntent.flee) {
                        encounterType = Encs.flee;
                        attacker = p1;
                        target = p2;
                    } else if (p1Intent == Character.FightIntent.flee
                                    && p2Intent == Character.FightIntent.fight) {
                        encounterType = Encs.flee;
                        attacker = p2;
                        target = p1;
                    } else {
                        encounterType = Encs.fight;
                        attacker = faster.get(0);
                        target = faster.get(1);
                    }
                } else if (p1SpotCheck) {
                    attacker = p1;
                    target = p2;
                    encounterType = attacker.spy(target, this);
                } else if (p2SpotCheck) {
                    attacker = p2;
                    target = p1;
                    encounterType = attacker.spy(target, this);
                } else {
                    attacker = faster.get(0);
                    target = faster.get(1);
                    encounterType = Encs.missed;
                }
            }
            parse(encounterType, attacker, target);
        } else {
            if (p1.state == State.masturbating) {
                if (p1.human()) {
                    messageIfObserved(p2.getName()
                                    + " catches you masturbating, but fortunately she's still not allowed to attack you, so she just watches you jerk off with "
                                    + "an amused grin.");
                } else if (p2.human()) {
                    messageIfObserved("You stumble onto " + p1.getName()
                                    + " with her hand between her legs, masturbating. Since you just fought, you still can't touch her, so "
                                    + "you just watch the show until she orgasms.");
                }
            } else if (p2.state == State.masturbating) {
                if (p2.human()) {
                    messageIfObserved(p1.getName()
                                    + " catches you masturbating, but fortunately she's still not allowed to attack you, so she just watches you jerk off with "
                                    + "an amused grin.");
                } else if (p1.human()) {
                    messageIfObserved("You stumble onto " + p2.getName()
                                    + " with her hand between her legs, masturbating. Since you just fought, you still can't touch her, so "
                                    + "you just watch the show until she orgasms.");
                }
            } else if (!p1.eligible(p2) && p1.human()) {
                messageIfObserved("You encounter " + p2.getName()
                                + ", but you still haven't recovered from your last fight.");
            } else if (p1.human()) {
                messageIfObserved("You find " + p2.getName()
                                + " still naked from your last encounter, but she's not fair game again until she replaces her clothes.");
            }
            location.endEncounter();
        }
    }

    /**
     * Determines which of two characters is faster. Ties broken by base speed, then by whoever ended up being p1.
     *
     * @return a list of the two characters sorted by speed.
     */
    private List<Character> faster(Character p1, Character p2) {
        Random.DieRoll p1Check = p1.check(Attribute.Speed, p1.getTraitMod(Trait.sprinter, 5));
        Random.DieRoll p2Check = p2.check(Attribute.Speed, p2.getTraitMod(Trait.sprinter, 5));

        if (p1Check.result() == p2Check.result()) {
            if (p1.get(Attribute.Speed) >= p2.get(Attribute.Speed)) {
                return Arrays.asList(p1, p2);
            } else {
                return Arrays.asList(p2, p1);
            }
        } else if (p1Check.result() > p2Check.result()) {
            return Arrays.asList(p1, p2);
        } else {
            return Arrays.asList(p2, p1);
        }
    }

    private List<Character> faster(List<Character> characters) {
        return faster(characters.get(0), characters.get(1));
    }

    private Encs vulnerable(Character attacker, Character target) {
        if (target.state == State.shower) {
            return attacker.showerSceneResponse(target, this);
        } else if (target.state == State.webbed) {
            return Encs.spidertrap;
        } else if (target.state == State.crafting || target.state == State.searching) {
            return attacker.spy(target, this);
        } else if (target.state == State.masturbating) {
            return Encs.caughtmasturbating;
        }
        throw new RuntimeException("Invalid vulnerable encounter type");
    }

    protected void smokeFlee(Character runner) {
        GUI.gui.message(String.format("%s a smoke bomb and %s.",
                        Formatter.capitalizeFirstLetter(runner.subjectAction("drop", "drops")),
                        runner.action("disappear", "disappears")));
        runner.consume(Item.SmokeBomb, 1);
        runner.flee(this.location);
    }

    protected void fleeHidden(Character attacker, Character runner) {
        if (attacker.human() || runner.human())
        GUI.gui.message(Formatter
                        .format("{self:SUBJECT-ACTION:flee} before {other:subject-action:can} notice {self:direct-object}.",
                                        runner, attacker));
        runner.flee(this.location);
    }

    protected void fleeAttempt(Character attacker, Character runner) {
        if (this.faster.get(0).equals(attacker)) {
            if (attacker.human()) {
                GUI.gui.message(runner.getName() + " tries to run, but you stay right on her heels and catch her.");
            } else if (runner.human()) {
                GUI.gui.message("You quickly try to escape, but " + attacker.getName() + " is quicker. She corners you and attacks.");
            }
            this.fight = new Combat(attacker, runner, this.location);
        } else {
            if (attacker.human()) {
                GUI.gui.message(runner.getName() + " dashes away before you can move.");
            } else if (runner.human()) {
                GUI.gui.message("You dash away before " + attacker.getName() + " can move.");
            }
            runner.flee(this.location);
        }
    }

    protected void ambush(Character attacker, Character target) {
        target.addNonCombat(new Flatfooted(target, 3));
        if (attacker.human() || target.human()) {
            GUI.gui.message(Formatter.format("{self:SUBJECT-ACTION:catch|catches} {other:name-do} by surprise and {self:action:attack|attacks}!", attacker, target));
        }
        fight = new Combat(attacker, target, location, Initiation.ambushRegular);
    }

    protected void showerambush(Character attacker, Character target) {
        if (target.human()) {
            if (location.id() == Movement.shower) {
                                messageIfObserved("You aren't in the shower long before you realize you're not alone. Before you can turn around, a soft hand grabs your exposed penis. "
                                                + attacker.getName() + " has the drop on you.");
            } else if (location.id() == Movement.pool) {
                                messageIfObserved("The relaxing water causes you to lower your guard a bit, so you don't notice "
                                                + attacker.getName()
                                                + " until she's standing over you. There's no chance to escape, you'll have to face her nude.");
            }
        } else if (attacker.human()) {
            if (location.id() == Movement.shower) {
                                messageIfObserved("You stealthily walk up behind " + target.getName()
                                                + ", enjoying the view of her wet naked body. When you stroke her smooth butt, "
                                                + "she jumps and lets out a surprised yelp. Before she can recover from her surprise, you pounce!");
            } else if (location.id() == Movement.pool) {
                                messageIfObserved("You creep up to the jacuzzi where " + target.getName()
                                                + " is soaking comfortably. As you get close, you notice that her eyes are "
                                                + "closed and she may well be sleeping. You crouch by the edge of the jacuzzi for a few seconds and just admire her nude body with her breasts "
                                                + "just above the surface. You lean down and give her a light kiss on the forehead to wake her up. She opens her eyes and swears under her breath "
                                                + "when she sees you. She scrambles out of the tub, but you easily catch her before she can get away.");
            }
        }
        fight = new Combat(attacker, target, location, Initiation.ambushStrip);
    }

    protected void aphrodisiactrick(Character attacker, Character target) {
        attacker.consume(Item.Aphrodisiac, 1);
        attacker.gainXP(attacker.getVictoryXP(target));
        target.gainXP(target.getDefeatXP(attacker));
        if (target.human()) {
            if (location.id() == Movement.shower) {
                                messageIfObserved("The hot shower takes your fatigue away, but you can't seem to calm down. Your cock is almost painfully hard. You need to deal with this while "
                                                + "you have the chance. You jerk off quickly, hoping to finish before someone stumbles onto you. Right before you cum, you are suddenly grabbed from behind and "
                                                + "spun around. " + attacker.getName()
                                                + " has caught you at your most vulnerable and, based on her expression, may have been waiting for this moment. She kisses you and "
                                                + "firmly grasps your twitching dick. In just a few strokes, you cum so hard it's almost painful.\n");
            } else if (location.id() == Movement.pool) {
                                messageIfObserved("As you relax in the jacuzzi, you start to feel extremely horny. Your cock is in your hand before you're even aware of it. You stroke yourself "
                                                + "off underwater and you're just about ready to cum when you hear nearby footsteps. Oh shit, you'd almost completely forgotten you were in the middle of a "
                                                + "match. The footsteps are from " + attacker.getName()
                                                + ", who sits down at the edge of the jacuzzi while smiling confidently. You look for a way to escape, but it's "
                                                + "hopeless. You were so close to finishing you just need to cum now. "
                                                + attacker.getName()
                                                + " seems to be thinking the same thing, as she dips her bare feet into the "
                                                + "water and grasps your penis between them. She pumps you with her feet and you shoot your load into the water in seconds.\n");
            }
        } else if (attacker.human()) {
            if (location.id() == Movement.shower) {
                                messageIfObserved("You empty the bottle of aphrodisiac onto the shower floor, letting the heat from the shower turn it to steam. You watch "
                                                + target.getName() + " and wait "
                                                + "for a reaction. Just when you start to worry that it was all washed down the drain, you see her hand slip between her legs. Her fingers go to work pleasuring herself "
                                                + "and soon she's completely engrossed in her masturbation, allowing you to safely get closer without being noticed. She's completely unreserved, assuming she's alone "
                                                + "and you feel a voyeuristic thrill at the show. You can't just remain an observer though. For this to count as a victory, you need to be in physical contact with her "
                                                + "when she orgasms. When you judge that she's in the home stretch, you embrace her from behind and kiss her neck. She freezes in surprise and you move your hand between "
                                                + "her legs to replace her own. Her pussy is hot, wet, and trembling with need. You stick two fingers into her and rub her clit with your thumb. She climaxes almost "
                                                + "immediately. You give her a kiss on the cheek and leave while she's still too dazed to realize what happened. You're feeling pretty horny, but after a show like that "
                                                + "it's hardly surprising.\n");
            } else if (location.id() == Movement.pool) {
                                messageIfObserved("You sneak up to the jacuzzi, and empty the aphrodisiac into the water without "
                                                + target.getName() + " noticing. You slip away and find a hiding spot. In a "
                                                + "couple minutes, you notice her stir. She glances around, but fails to see you and then closes her eyes and relaxes again. There's something different now though and "
                                                + "her soft moan confirms it. You grin and quietly approach again. You can see her hand moving under the surface of the water as she enjoys herself tremendously. Her moans "
                                                + "rise in volume and frequency. Now's the right moment. You lean down and kiss her on the lips. Her masturbation stops immediately, but you reach underwater and finger "
                                                + "her to orgasm. When she recovers, she glares at you for your unsportsmanlike trick, but she can't manage to get really mad in the afterglow of her climax. You're "
                                                + "pretty turned on by the encounter, but you can chalk this up as a win.\n");
            }
        }
        if (!target.mostlyNude()) {
            attacker.gain(target.getTrophy());
        }
        target.nudify();
        target.defeated(attacker);
        target.getArousal()
                        .empty();
        attacker.tempt(20);
        Match.getMatch()
                        .score(attacker, target.has(Trait.event) ? 5 : 1);
        attacker.state = State.ready;
        target.state = State.ready;
        location.endEncounter();
    }

    // TODO: Update messages to use formatter and provide alternate-gender options.
    protected void caught(Character attacker, Character target) {
        attacker.gainXP(attacker.getVictoryXP(target));
        target.gainXP(target.getDefeatXP(attacker));
        if (target.human()) {
            messageIfObserved("You jerk off frantically, trying to finish as fast as possible. Just as you feel the familiar sensation of imminent orgasm, you're grabbed from behind. "
                            + "You freeze, cock still in hand. As you turn your head to look at your attacker, "
                            + attacker.getName()
                            + " kisses you on the lips and rubs the head of your penis with her "
                            + "palm. You were so close to the edge that just you cum instantly.");
            if (!target.mostlyNude()) {
                messageIfObserved("You groan in resignation and reluctantly strip off your clothes and hand them over.");
            }
        } else if (attacker.human()) {
            messageIfObserved("You spot " + target.getName()
                            + " leaning against the wall with her hand working excitedly between her legs. She is mostly, but not completely successful at "
                            + "stifling her moans. She hasn't noticed you yet, and as best as you can judge, she's pretty close to the end. It'll be an easy victory for you as long as you work fast. "
                            + "You sneak up and hug her from behind while kissing the nape of her neck. She moans and shudders in your arms, but doesn't stop fingering herself. She probably realizes "
                            + "she has no chance of winning even if she fights back. You help her along by licking her neck and fondling her breasts as she hits her climax.");
        }
        if (!target.mostlyNude()) {
            attacker.gain(target.getTrophy());
        }
        target.nudify();
        target.defeated(attacker);
        target.getArousal()
                        .empty();
        attacker.tempt(20);
        Match.getMatch()
                        .score(attacker, target.has(Trait.event) ? 5 : 1);
        attacker.state = State.ready;
        target.state = State.ready;
        location.endEncounter();
    }

    protected void spider(Character attacker, Character target) {
        attacker.gainXP(attacker.getVictoryXP(target));
        target.gainXP(target.getDefeatXP(attacker));
        if (attacker.human()) {
                            messageIfObserved(target.getName()
                                            + " is naked and helpless in the giant rope web. You approach slowly, taking in the lovely view of her body. You trail your fingers "
                                            + "down her front, settling between her legs to tease her sensitive pussy lips. She moans and squirms, but is completely unable to do anything in her own defense. "
                                            + "You are going to make her cum, that's just a given. If you weren't such a nice guy, you would leave her in that trap afterward to be everyone else's prey "
                                            + "instead of helping her down. You kiss and lick her neck, turning her on further. Her entrance is wet enough that you can easily work two fingers into her "
                                            + "and begin pumping. You gradually lick your way down her body, lingering at her nipples and bellybutton, until you find yourself eye level with her groin. "
                                            + "You can see her clitoris, swollen with arousal, practically begging to be touched. You trap the sensitive bud between your lips and attack it with your tongue. "
                                            + "The intense stimulation, coupled with your fingers inside her, quickly brings her to orgasm. While she's trying to regain her strength, you untie the ropes "
                                            + "binding her hands and feet and ease her out of the web.");
        } else if (target.human()) {
                            messageIfObserved("You're trying to figure out a way to free yourself, when you see " + attacker.getName()
                                            + " approach. You groan in resignation. There's no way you're "
                                            + "going to get free before she finishes you off. She smiles as she enjoys your vulnerable state. She grabs your dangling penis and puts it in her mouth, licking "
                                            + "and sucking it until it's completely hard. Then the teasing starts. She strokes you, rubs you, and licks the head of your dick. She uses every technique to "
                                            + "pleasure you, but stops just short of letting you ejaculate. It's maddening. Finally you have to swallow your pride and beg to cum. She pumps you dick in earnest "
                                            + "now and fondles your balls. When you cum, you shoot your load onto her face and chest. You hang in the rope web, literally and figuratively drained. "
                                            + attacker.getName() + " " + "graciously unties you and helps you down.");
        }
        if (!target.mostlyNude()) {
            attacker.gain(target.getTrophy());
        }
        target.nudify();
        target.defeated(attacker);
        target.getArousal()
                        .empty();
        attacker.tempt(20);
        Match.getMatch()
                        .score(attacker, target.has(Trait.event) ? 5 : 1);
        attacker.state = State.ready;
        target.state = State.ready;
        location.endEncounter();
        location.remove(location.get(Spiderweb.class));
    }

    public void intrude(Character intruder, Character assist) {
        participants.add(intruder);
        fight.intervene(intruder, assist);
    }

    /**
     * NPC combat lasts for a few turns before resolving.
     */
    public void battle() {
        // Handled by combat's delayCounter during match loop combat phase.
    }

    public Optional<Combat> getCombat() {
        return Optional.ofNullable(fight);
    }

    public Character getPlayer(int i) {
        if (i == 1) {
            return getP1();
        } else {
            return getP2();
        }
    }

    protected void steal(Character thief, Character target) {
        if (thief.human()) {
            messageIfObserved("You quietly swipe " + target.getName()
                            + "'s clothes while she's occupied. It's a little underhanded, but you can still turn them in for cash just as if you defeated her.");
        }
        thief.gain(target.getTrophy());
        target.nudify();
        target.state = State.lostclothes;
        location.endEncounter();
    }

    public void trap(Character opportunist, Character target, Trap trap) {
        if (opportunist.human()) {
            messageIfObserved("You leap out of cover and catch " + target.getName() + " by surprise.");
        } else if (target.human()) {
            messageIfObserved("Before you have a chance to recover, " + opportunist.getName() + " pounces on you.");
        }
        trap.capitalize(opportunist, target, this);
    }

    public void engage(Combat fight) {
        this.fight = fight;
        if (fight.p1.human() || fight.p2.human()) {
            fight.loadCombatGUI(GUI.gui);
        }
    }

    public void parse(Encs choice, Character self, Character target) {
        parse(choice, self, target, null);
    }

    public void parse(Encs choice, Character attacker, Character target, Trap trap) {
        if (DebugFlags.isDebugOn(DebugFlags.DEBUG_SCENE)) {
            System.out.println(
                            Formatter.format("{self:true-name} uses %s (%s) on {other:true-name}", attacker, target, choice, trap));
        }
        switch (choice) {
            case ambush:
                ambush(attacker, target);
                break;
            case capitalizeontrap:
                trap(attacker, target, trap);
                break;
            case showerattack:
                showerambush(attacker, target);
                break;
            case aphrodisiactrick:
                aphrodisiactrick(attacker, target);
                break;
            case stealclothes:
                steal(attacker, target);
                break;
            case caughtmasturbating:
                caught(attacker, target);
                break;
            case fight:
                this.fight = new Combat(attacker, target, this.location);
                break;
            case bothflee:
                bothFlee(attacker, target);
                break;
            case flee:
                fleeAttempt(attacker, target);
                break;
            case fleehidden:
                fleeHidden(attacker, target);
                break;
            case smoke:
                smokeFlee(target);
                break;
            default:
                location.endEncounter();
        }
    }

    private void bothFlee(Character faster, Character slower) {
        messageIfObserved(Formatter.format("{self:subject} and {other:subject} dash away from each other at top speed.",
                        faster, slower));
        faster.flee(this.location);
        slower.flee(this.location);
        location.endEncounter();
    }

    public boolean checkIntrudePossible(Character c) {
        return fight != null && !c.equals(getP1()) && !c.equals(getP2());
    }

    public void watch(GUI gui) {
        fight.loadCombatGUI(gui);
    }

    public void await() throws InterruptedException {
        waitForFinish.await();
    }

    public void finish() {
        if (fight != null) {
            fight = null;
        }
        waitForFinish.countDown();
    }

    /**
     * Based on participant responses, determine whether this encounter results in combat.
     */
    public Optional<Combat> resolve() {
        // TODO: spotCheck() has a lot of side effects. Refactor them into something less innocuous.
        if (fight == null) {
            spotCheck();
        }
        if (fight != null) {
            if (!fight.isEnded()) {
                return Optional.of(fight);
            }
        }
        return Optional.empty();
    }
}
