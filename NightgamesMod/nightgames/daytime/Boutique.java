package nightgames.daytime;

import nightgames.characters.NPC;
import nightgames.characters.Player;
import nightgames.global.Flag;
import nightgames.gui.GUI;
import nightgames.gui.LabeledValue;
import nightgames.items.clothing.Clothing;
import nightgames.items.clothing.ClothingTable;

import java.util.List;

public class Boutique extends Store {
    public Boutique(Player player) {
        super("Boutique", player);
        ClothingTable.getAllBuyableFrom("Boutique").forEach(this::add);
    }

    @Override
    public boolean known() {
        if (player.useFemalePronouns()) {
            return Flag.checkFlag(Flag.basicStores);
        }
        return false;
    }

    @Override
    public void visit(String choice, int page, List<LabeledValue<String>> nextChoices, ActivityInstance instance) {
        GUI.gui.clearText();
        GUI.gui.clearCommand();
        if (choice.equals("Start")) {
            acted = false;
        } else if (choice.equals("Leave")) {
            done(acted, instance);
            return;
        } else {
            attemptBuy(choice);
        }
        if (player.human()) {
            GUI.gui.message(
                            "This is a higher end store for women's clothing. Things may get a bit expensive here.");
            for (Clothing i : clothing().keySet()) {
                GUI.gui.message(i.getName() + ": " + i.getPrice() + (player.has(i) ? " (Owned)" : ""));
            }
            GUI.gui.message("You have: $" + player.money + " available to spend.");
            displayGoods(nextChoices);
            choose("Leave", nextChoices);
        }
    }

    @Override
    public void shop(NPC npc, int budget) {}

}
