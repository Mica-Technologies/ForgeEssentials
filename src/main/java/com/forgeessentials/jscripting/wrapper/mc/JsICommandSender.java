package com.forgeessentials.jscripting.wrapper.mc;

import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.jscripting.wrapper.JsWrapper;
import com.forgeessentials.jscripting.wrapper.mc.entity.JsEntityPlayer;
import com.forgeessentials.util.DoAsCommandSender;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.google.gson.JsonParseException;

/**
 *
 */
public class JsICommandSender extends JsWrapper<ICommandSender>
{

    private JsEntityPlayer player;

    /**
     * @tsd.ignore
     */
    public static JsICommandSender get(ICommandSender sender)
    {
        return sender == null ? null : new JsICommandSender(sender);
    }

    private JsICommandSender(ICommandSender that)
    {
        super(that);
    }

    public JsICommandSender(EntityPlayer that, JsEntityPlayer jsPlayer)
    {
        super(that);
        this.player = jsPlayer;
    }

    public String getName()
    {
        return that.getName();
    }

    public JsEntityPlayer getPlayer()
    {
        if (player != null || !(that instanceof EntityPlayer))
            return player;
        return player = new JsEntityPlayer((EntityPlayer) that, this);
    }

    public JsICommandSender doAs(Object userIdOrPlayer, boolean hideChatOutput)
    {
        UserIdent doAsUser = userIdOrPlayer instanceof UUID ? UserIdent.get((UUID) userIdOrPlayer)
                : userIdOrPlayer instanceof JsEntityPlayer ? UserIdent.get(((JsEntityPlayer) userIdOrPlayer).getThat()) : APIRegistry.IDENT_SERVER;
        DoAsCommandSender result = new DoAsCommandSender(doAsUser, that);
        result.setHideChatMessages(hideChatOutput);
        return new JsICommandSender(result);
    }

    public void chat(String message)
    {
        ChatOutputHandler.sendMessage(that, message);
    }

    public void chatConfirm(String message)
    {
        ChatOutputHandler.chatConfirmation(that, message);
    }

    public void chatNotification(String message)
    {
        ChatOutputHandler.chatNotification(that, message);
    }
    public void chatError(String message)
    {
        ChatOutputHandler.chatError(that, message);
    }
    public void chatWarning(String message)
    {
        ChatOutputHandler.chatWarning(that, message);
    }
    public void tellRaw(String msg)
    { 
    	if (msg.isEmpty())
    	{
    	    return;
        }
        try
        {
            Entity senderEntity = this.that.getCommandSenderEntity();
            if (senderEntity != null)
            {
                ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(msg);
                this.that.sendMessage(TextComponentUtils.processComponent(this.that, itextcomponent, senderEntity));
            }
        }
        catch (JsonParseException jsonparseexception)
        {
            this.chatError("There is an error in your JSON: "+jsonparseexception.getMessage());
        } 
        catch (CommandException e) 
        {
            this.chatError("There is an error in your input: "+e.getMessage());
        }
    }

}
