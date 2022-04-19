package me.maxouxax.supervisor.utils;

import net.dv8tion.jda.api.entities.Member;

public class UserUtils {

    /**
     * Get the url of the user's avatar, depending on whether he set a per-guild one or not
     * @param member Member from which to get the avatar
     * @return The member avatar (per-guild avatar if set, user avatar otherwise)
     */
    public static String getAvatarUrl(Member member){
        return member.getAvatarUrl() != null ? member.getAvatarUrl() : member.getUser().getAvatarUrl();
    }

}
