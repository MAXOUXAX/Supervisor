package me.maxouxax.supervisor.jda.pojos;

import net.dv8tion.jda.api.entities.PermissionOverride;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * POJO for the {@link PermissionOverride} interface
 */
public class ChannelPermission {

    private final long allowedRaw;
    private final long deniedRaw;
    private final boolean isMemberPermission;
    private final String channelId;
    private final String guildId;
    private final String holderId;

    @BsonCreator
    public ChannelPermission(@BsonProperty("allowedRaw") final long allowedRaw,
                             @BsonProperty("deniedRaw") final long deniedRaw,
                             @BsonProperty("isMemberPermission") final boolean isMemberPermission,
                             @BsonProperty("channelId") final String channelId,
                             @BsonProperty("guildId") final String guildId,
                             @BsonProperty("holderId") final String holderId) {
        this.allowedRaw = allowedRaw;
        this.deniedRaw = deniedRaw;
        this.isMemberPermission = isMemberPermission;
        this.channelId = channelId;
        this.guildId = guildId;
        this.holderId = holderId;
    }

    public ChannelPermission(PermissionOverride permissionOverride) {
        this.allowedRaw = permissionOverride.getAllowedRaw();
        this.deniedRaw = permissionOverride.getDeniedRaw();
        this.isMemberPermission = permissionOverride.isMemberOverride();
        this.channelId = permissionOverride.getChannel().getId();
        this.guildId = permissionOverride.getGuild().getId();
        this.holderId = permissionOverride.getPermissionHolder().getId();
    }

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions allowed by this override.
     *
     * @return Never-negative long containing the binary representation of the allowed permissions of this override.
     */
    public long getAllowedRaw() {
        return allowedRaw;
    }

    /**
     * This is the raw binary representation (as a base 10 long) of the permissions denied by this override.
     *
     * @return Never-negative long containing the binary representation of the denied permissions of this override.
     */
    public long getDeniedRaw() {
        return deniedRaw;
    }

    /**
     * Used to determine if this ChannelPermission relates to a specific Member.
     *
     * @return {@code true} if this override is a user override.
     */
    public boolean isMemberPermission() {
        return isMemberPermission;
    }

    /**
     * @return The ID of the channel this ChannelPermission is for.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @return The ID of the guild this ChannelPermission is for.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * @return The ID of the user or role that this ChannelPermission is for.
     */
    public String getHolderId() {
        return holderId;
    }

}
