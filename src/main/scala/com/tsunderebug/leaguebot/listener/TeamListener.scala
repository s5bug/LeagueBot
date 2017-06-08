package com.tsunderebug.leaguebot.listener

import java.util

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.{IRole, IUser, Permissions}

import scala.collection.JavaConverters._

class TeamListener extends IListener[MessageReceivedEvent] {

  override def handle(event: MessageReceivedEvent): Unit = {
    if (event.getMessage.getContent.startsWith("-team")) {
      if (event.getMessage.getContent.split("\\s+").length >= 3) {
        val name = event.getMessage.getFormattedContent.split("\\s+")(1).toLowerCase
        val members: List[IUser] = event.getAuthor :: event.getMessage.getMentions.asScala.toList
        if (members.exists(_.getRolesForGuild(event.getGuild).asScala.map(_.getName).exists(_.startsWith("team-")))) {
          event.getChannel.sendMessage("You or someone you want to add is already in a team!")
        } else if (event.getGuild.getRoles.asScala.exists((r: IRole) => r.getName.equals("team-" + name))) {
          event.getChannel.sendMessage("Team with that name already exists!")
        } else {
          val role = event.getGuild.createRole()
          role.changeName("team-" + name)
          val channel = event.getGuild.createChannel("team-" + name)
          channel.overrideRolePermissions(event.getGuild.getEveryoneRole, util.EnumSet.noneOf(classOf[Permissions]), util.EnumSet.of(Permissions.READ_MESSAGES))
          channel.overrideRolePermissions(role, util.EnumSet.of(Permissions.READ_MESSAGES), util.EnumSet.noneOf(classOf[Permissions]))
          members.foreach(_.addRole(role))
          role.changeMentionable(true)
          channel.sendMessage("Created team " + role.mention())
          role.changeMentionable(false)
        }
      } else {
        event.getMessage.reply("Incorrect usage! `-team name mention[ mention[ mention...]]]`")
      }
    } else if (event.getMessage.getContent.startsWith("-leave") && event.getChannel.getName.startsWith("team-")) {
      val r = event.getGuild.getRolesByName(event.getChannel.getName).get(0)
      event.getMessage.getAuthor.removeRole(r)
      if(event.getGuild.getUsersByRole(r).isEmpty) {
        event.getChannel.delete()
      }
    }
  }

}