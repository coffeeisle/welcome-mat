package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AnimationRegistry {
    private final WelcomeMat plugin;
    private final Map<String, Animation> animations;
    private final List<Animation> randomAnimations;
    private final Map<UUID, String> playerAnimations;
    private final Random random;

    public AnimationRegistry(WelcomeMat plugin) {
        this.plugin = plugin;
        this.animations = new HashMap<>();
        this.randomAnimations = new ArrayList<>();
        this.playerAnimations = new HashMap<>();
        this.random = new Random();

        registerDefaultAnimations();
    }

    private void registerDefaultAnimations() {
        registerAnimation(new FireSpiralAnimation(plugin));
        registerAnimation(new GreenSpikeFireworkAnimation(plugin));
    }

    public void registerAnimation(Animation animation) {
        animations.put(animation.getName(), animation);
        if (animation.isRandom()) {
            randomAnimations.add(animation);
        }
    }

    public void setPlayerAnimation(Player player, String animationName) {
        if (animations.containsKey(animationName)) {
            playerAnimations.put(player.getUniqueId(), animationName);
        }
    }

    public void setRandomAnimation(Player player) {
        playerAnimations.remove(player.getUniqueId());
    }

    public void playAnimation(Player player) {
        String animationName = playerAnimations.get(player.getUniqueId());
        Animation animation;

        if (animationName != null) {
            animation = animations.get(animationName);
        } else if (!randomAnimations.isEmpty()) {
            animation = randomAnimations.get(random.nextInt(randomAnimations.size()));
        } else {
            return;
        }

        animation.play(player);
    }

    public List<String> getAvailableAnimations() {
        return new ArrayList<>(animations.keySet());
    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public String getPlayerAnimation(Player player) {
        return playerAnimations.get(player.getUniqueId());
    }

    public boolean isUsingRandomAnimation(Player player) {
        return !playerAnimations.containsKey(player.getUniqueId());
    }
}