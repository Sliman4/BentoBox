package us.tastybento.bskyblock.commands.island.teams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import us.tastybento.bskyblock.Constants;
import us.tastybento.bskyblock.api.commands.User;

public class IslandTeamKickCommand extends AbstractIslandTeamCommand {

    Set<UUID> kickSet;

    public IslandTeamKickCommand(IslandTeamCommand islandTeamCommand) {
        super(islandTeamCommand, "kick");
    }

    @Override
    public void setup() {
        this.setPermission(Constants.PERMPREFIX + "island.team");
        this.setOnlyPlayer(true);
        this.setParameters("commands.island.team.kick.parameters");
        this.setDescription("commands.island.team.kick.description");
        kickSet = new HashSet<>();
    }

    @Override
    public boolean execute(User user, List<String> args) {
        if (!getPlayers().inTeam(user.getUniqueId())) {
            user.sendMessage("general.errors.no-team");
            return true;
        }
        if (!getIslands().getTeamLeader(user.getUniqueId()).equals(user.getUniqueId())) {
            user.sendMessage("general.errors.not-leader");
            return true;
        }
        // If args are not right, show help
        if (args.size() != 1) {
            return this.getSubCommand("help").map(command -> execute(user, new ArrayList<>())).orElse(false);
        }
        // Get target
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player");
            return true; 
        }
        if (!getIslands().getMembers(user.getUniqueId()).contains(targetUUID)) {
            user.sendMessage("general.errors.not-in-team");
            return true;  
        }
        if (!getSettings().isKickConfirmation() || kickSet.contains(targetUUID)) {
            kickSet.remove(targetUUID);
            User.getInstance(targetUUID).sendMessage("commands.island.team.kick.leader-kicked");
            getIslands().removePlayer(targetUUID);
            user.sendMessage("general.success");
        } else {
            user.sendMessage("commands.island.team.kick.type-again");
            kickSet.add(targetUUID);
            new BukkitRunnable() {

                @Override
                public void run() {
                    kickSet.remove(targetUUID);
                    user.sendMessage("general.errors.command-cancelled");
                }}.runTaskLater(getPlugin(), getSettings().getKickWait());
        }
        return true;
    }


}