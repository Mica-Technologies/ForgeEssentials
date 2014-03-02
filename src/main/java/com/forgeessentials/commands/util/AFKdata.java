package com.forgeessentials.commands.util;

import net.minecraft.entity.player.EntityPlayerMP;

import com.forgeessentials.commands.CommandAFK;
import com.forgeessentials.teleport.util.TickHandlerTP;
import com.forgeessentials.util.AreaSelector.WarpPoint;
import com.forgeessentials.util.AreaSelector.WorldPoint;

public class AFKdata
{
	public EntityPlayerMP	player;
	private WorldPoint		lastPos;
	private WorldPoint		currentPos;
	int						waittime;
	public boolean			needstowait;

	public AFKdata(EntityPlayerMP player)
	{
		this.player = player;
		waittime = CommandAFK.warmup;
		lastPos = new WarpPoint(player);
		needstowait = true;
	}

	public void count()
	{
		if (player == null)
		{
			TickHandlerTP.afkListToRemove.add(this);
			return;
		}

		currentPos = new WarpPoint(player);
		if (!lastPos.equals(currentPos))
		{
			CommandAFK.instance.abort(this);
		}

		if (needstowait)
		{
			if (waittime == 0)
			{
				CommandAFK.instance.makeAFK(this);
			}
			waittime--;
		}
	}
}
