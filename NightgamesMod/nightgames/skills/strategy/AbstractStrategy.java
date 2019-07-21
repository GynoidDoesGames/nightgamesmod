package nightgames.skills.strategy;

import nightgames.characters.Character;
import nightgames.combat.Combat;
import nightgames.skills.Skill;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractStrategy implements CombatStrategy {
    private Set<Skill> getAllowedSkills(Combat c, nightgames.characters.Character self) {
        Set<Skill> availableSkills = new HashSet<>(self.getSkills());
        Skill.filterAllowedSkills(c, availableSkills, self);
        return availableSkills.stream().filter(skill -> Skill.skillIsUsable(c, skill)).collect(Collectors.toSet());
    }
    
    protected abstract Set<Skill> filterSkills(Combat c, nightgames.characters.Character self, Set<Skill> allowedSkills);
    
    public Set<Skill> nextSkills(Combat c, Character self) {
        return filterSkills(c, self, getAllowedSkills(c, self));
    }
}
