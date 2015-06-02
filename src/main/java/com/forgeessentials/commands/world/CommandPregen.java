package com.forgeessentials.commands.world;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.permissions.PermissionsManager.RegisteredPermValue;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.permissions.FEPermissions;
import com.forgeessentials.commands.util.FEcmdModuleCommands;
import com.forgeessentials.commons.selections.AreaShape;
import com.forgeessentials.core.commands.ParserCommandBase;
import com.forgeessentials.core.misc.TaskRegistry;
import com.forgeessentials.core.misc.TaskRegistry.TickTask;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.util.CommandParserArgs;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.ServerUtil;
import com.forgeessentials.worldborder.ModuleWorldBorder;
import com.forgeessentials.worldborder.WorldBorder;

public class CommandPregen extends ParserCommandBase implements TickTask
{

    private boolean running = false;

    private WorldServer world;

    private int speed;

    private AreaShape shape;

    private int minX;

    private int minZ;

    private int maxX;

    private int maxZ;

    private int centerX;

    private int centerZ;

    private int sizeX;

    private int sizeZ;

    private int x;

    private int z;

    private int totalTicks;

    private int totalChunks;

    @Override
    public String getCommandName()
    {
        return "pregen";
    }

    @Override
    public String[] getDefaultAliases()
    {
        return new String[] { "filler" };
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pregen start [speed] [dim]";
    }

    @Override
    public String getPermissionNode()
    {
        return FEcmdModuleCommands.COMMANDS_PERM + ".pregen";
    }

    @Override
    public RegisteredPermValue getDefaultPermission()
    {
        return RegisteredPermValue.OP;
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public void parse(CommandParserArgs arguments)
    {
        if (arguments.isEmpty())
        {
            if (running)
            {
                arguments.confirm("Pregen running");
            }
            else
            {
                arguments.confirm("No pregen running");
            }
            return;
        }

        arguments.tabComplete("start", "stop");
        String subCmd = arguments.remove().toLowerCase();
        switch (subCmd)
        {
        case "start":
            parseStart(arguments);
            break;
        case "stop":
            parseStop(arguments);
            break;
        default:
            throw new TranslatedCommandException(FEPermissions.MSG_UNKNOWN_SUBCOMMAND, subCmd);
        }
    }

    /* ------------------------------------------------------------ */

    private void parseStart(CommandParserArgs arguments)
    {
        if (running)
        {
            arguments.error("Pregen already running");
            return;
        }

        speed = 1;
        world = null;
        if (!arguments.isEmpty())
        {
            speed = arguments.parseInt();

            if (!arguments.isEmpty())
            {
                world = (WorldServer) arguments.senderPlayer.worldObj;
            }
        }
        if (world == null)
        {
            if (arguments.senderPlayer == null)
                throw new TranslatedCommandException(FEPermissions.MSG_NOT_ENOUGH_ARGUMENTS);
            else
                world = (WorldServer) arguments.senderPlayer.worldObj;
        }

        WorldBorder border = ModuleWorldBorder.getInstance().getBorder(world);
        if (border == null)
            throw new TranslatedCommandException("No worldborder defined");

        centerX = border.getCenter().getX() / 16;
        centerZ = border.getCenter().getZ() / 16;
        sizeX = border.getSize().getX() / 16;
        sizeZ = border.getSize().getZ() / 16;
        minX = border.getArea().getLowPoint().getX() / 16;
        minZ = border.getArea().getLowPoint().getZ() / 16;
        maxX = border.getArea().getHighPoint().getX() / 16;
        maxZ = border.getArea().getHighPoint().getZ() / 16;
        shape = border.getShape();

        x = minX - 1;
        z = minZ;
        running = true;
        totalTicks = 0;
        totalChunks = 0;

        TaskRegistry.getInstance().schedule(this);
        arguments.confirm("Pregen started");
    }

    private void parseStop(CommandParserArgs arguments)
    {
        if (!running)
        {
            arguments.error("No pregen running");
            return;
        }
        running = false;
    }

    @Override
    public boolean tick()
    {
        if (!running)
        {
            notifyPlayers("Pregen stopped");
            return true;
        }
        if (++totalTicks % 250 == 0)
        {
            notifyPlayers(String.format("Pregen: %d/%d chunks processed", totalChunks, sizeX * sizeZ * 4));
        }
        for (int i = 0; i < speed; i++)
        {
            while (true)
            {
                totalChunks++;
                if (!next())
                {
                    running = false;
                    notifyPlayers("World pregen finished");
                    return true;
                }

                ChunkProviderServer providerServer = (ChunkProviderServer) world.getChunkProvider();
                if (RegionFileCache.createOrLoadRegionFile(world.getChunkSaveLocation(), x, z).chunkExists(x & 0x1F, z & 0x1F))
                    continue;

                Chunk chunk = providerServer.currentChunkProvider.loadChunk(x, z);
                chunk.populateChunk(providerServer, providerServer, x, z);
                saveChunk(providerServer, chunk);
                break;
            }
        }
        return false;
    }

    private boolean next()
    {
        switch (shape)
        {
        default:
        case BOX:
            if (++x > maxX)
            {
                x = minX;
                if (++z > maxZ)
                    return false;
            }
            return true;
        case CYLINDER:
        case ELLIPSOID:
            while (true)
            {
                if (++x > maxX)
                {
                    x = minX;
                    if (++z > maxZ)
                        return false;
                }
                double dx = (double) (centerX - x) / sizeX;
                double dz = (double) (centerZ - z) / sizeZ;
                if (dx * dx + dz * dz <= 1)
                    return true;
            }
        }
    }

    @Override
    public boolean editsBlocks()
    {
        return true;
    }

    public void notifyPlayers(String message)
    {
        for (EntityPlayerMP player : ServerUtil.getPlayerList())
            if (APIRegistry.perms.checkPermission(player, getPermissionNode()))
                OutputHandler.chatNotification(player, message);
    }

    /* ------------------------------------------------------------ */

    private static Method writeChunkToNBT;

    static
    {
        Class<?>[] cArg = new Class[] { Chunk.class, World.class, NBTTagCompound.class };
        try
        {
            writeChunkToNBT = AnvilChunkLoader.class.getDeclaredMethod("func_75820_a", cArg); // writeChunkToNBT
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                writeChunkToNBT = AnvilChunkLoader.class.getDeclaredMethod("writeChunkToNBT", cArg);
            }
            catch (NoSuchMethodException e1)
            {
                throw new RuntimeException("Pregen: Unable to obtain access to private method AnvilChunkLoader.writeChunkToNBT");
            }
        }
        writeChunkToNBT.setAccessible(true);
    }

    private static void saveChunk(ChunkProviderServer provider, Chunk chunk)
    {
        AnvilChunkLoader loader = (AnvilChunkLoader) provider.currentChunkLoader;
        try
        {
            NBTTagCompound chunkTag = new NBTTagCompound();
            NBTTagCompound levelTag = new NBTTagCompound();
            chunkTag.setTag("Level", levelTag);
            writeChunkToNBT(provider.worldObj, loader, chunk, levelTag);
            writeChunkData(provider.worldObj, loader, chunk, chunkTag);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /* wrapper for AnvilChunkLoader.writeChunkToNBT */
    private static void writeChunkToNBT(WorldServer world, AnvilChunkLoader loader, Chunk chunk, NBTTagCompound tag)
    {
        Object[] args = new Object[] { chunk, world, tag };
        try
        {
            writeChunkToNBT.invoke(loader, args);
        }
        catch (IllegalAccessException | IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.getCause().printStackTrace();
        }
    }

    private static void writeChunkData(WorldServer world, AnvilChunkLoader loader, Chunk chunk, NBTTagCompound tag) throws IOException
    {
        try (DataOutputStream dataoutputstream = RegionFileCache.getChunkOutputStream(world.getChunkSaveLocation(), chunk.xPosition, chunk.zPosition))
        {
            CompressedStreamTools.write(tag, dataoutputstream);
        }
    }

}
