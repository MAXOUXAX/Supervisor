package me.maxouxax.supervisor.supervised;

/**
 * Generic config class
 */
public class Config {

    private String discordToken;
    private Embed embed;
    private String gameName;
    private String websiteUrl;

    public Config() {
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public Embed getEmbed() {
        return embed;
    }

    public String getGameName() {
        return gameName;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

}
