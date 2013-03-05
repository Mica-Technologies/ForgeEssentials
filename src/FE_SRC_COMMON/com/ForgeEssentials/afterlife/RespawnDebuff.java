package com.ForgeEssentials.afterlife;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;

import com.ForgeEssentials.api.permissions.IPermRegisterEvent;
import com.ForgeEssentials.api.permissions.PermissionsAPI;
import com.ForgeEssentials.api.permissions.RegGroup;
import com.ForgeEssentials.api.permissions.query.PermQueryPlayer;

import cpw.mods.fml.common.IPlayerTracker;

public class RespawnDebuff implements IPlayerTracker
{
	public static final String				BYPASSPOTION	= ModuleAfterlife.BASEPERM + ".bypassPotions";
	public static final String				BYPASSSTATS		= ModuleAfterlife.BASEPERM + ".bypassStats";
	public static ArrayList<PotionEffect>	potionEffects;
	public static int						hp;
	public static int						food;

	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player)
	{
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player)
	{
		if (!PermissionsAPI.checkPermAllowed(new PermQueryPlayer(player, BYPASSPOTION)))
		{
			for (PotionEffect effect : potionEffects)
			{
				player.addPotionEffect(effect);
			}
		}
		if (!PermissionsAPI.checkPermAllowed(new PermQueryPlayer(player, BYPASSSTATS)))
		{
			System.out.println(player.username + " " + food + " " + hp);
			player.getFoodStats().setFoodLevel(food);
			player.setEntityHealth(hp);
		}
	}
}