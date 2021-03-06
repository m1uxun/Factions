package com.massivecraft.factions.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.FPerm;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Perm;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.event.FPlayerLeaveEvent;

public class CmdFactionsKick extends FCommand
{
	
	public CmdFactionsKick()
	{
		super();
		this.aliases.add("kick");
		
		this.requiredArgs.add("player");
		//this.optionalArgs.put("", "");
		
		this.permission = Perm.KICK.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{	
		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (you == null) return;
		
		if (fme == you)
		{
			msg("<b>You cannot kick yourself.");
			msg("<i>You might want to: %s", p.cmdBase.cmdFactionsLeave.getUseageTemplate(false));
			return;
		}
		
		if (you.getRole() == Rel.LEADER && !(this.senderIsConsole || fme.hasAdminMode()))
		{
			msg("<b>The leader can not be kicked.");
			return;
		}

		if ( ! ConfServer.canLeaveWithNegativePower && you.getPower() < 0)
		{
			msg("<b>You cannot kick that member until their power is positive.");
			return;
		}
		
		Faction yourFaction = you.getFaction();

		if (fme != null && ! FPerm.KICK.has(fme, yourFaction)) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if ( ! canAffordCommand(ConfServer.econCostKick, "to kick someone from the faction")) return;

		// trigger the leave event (cancellable) [reason:kicked]
		FPlayerLeaveEvent event = new FPlayerLeaveEvent(you, you.getFaction(), FPlayerLeaveEvent.PlayerLeaveReason.KICKED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		// then make 'em pay (if applicable)
		if ( ! payForCommand(ConfServer.econCostKick, "to kick someone from the faction", "for kicking someone from the faction")) return;

		yourFaction.msg("%s<i> kicked %s<i> from the faction! :O", fme.describeTo(yourFaction, true), you.describeTo(yourFaction, true));
		you.msg("%s<i> kicked you from %s<i>! :O", fme.describeTo(you, true), yourFaction.describeTo(you));
		if (yourFaction != myFaction)
		{
			fme.msg("<i>You kicked %s<i> from the faction %s<i>!", you.describeTo(fme), yourFaction.describeTo(fme));
		}

		if (ConfServer.logFactionKick)
			Factions.get().log((senderIsConsole ? "A console command" : fme.getName())+" kicked "+you.getName()+" from the faction: "+yourFaction.getTag());

		if (you.getRole() == Rel.LEADER)
			yourFaction.promoteNewLeader();

		yourFaction.deinvite(you);
		you.resetFactionData();
	}
	
}
