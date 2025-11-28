package xyz.coffeeisle.welcomemat.effects;

import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.effects.animations.Animation;
import xyz.coffeeisle.welcomemat.effects.animations.ConfettiBurstAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.CopperGolemAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.DataStreamAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.FireSpiralAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.GreenSpikeFireworkAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.HeartBloomAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.MoonRiseAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.RuneCircleAnimation;
import xyz.coffeeisle.welcomemat.effects.animations.SkywardCascadeAnimation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AnimationRegistry {
    private final WelcomeMat plugin;
    private final Map<String, Animation> animations;
    private final List<String> randomPool;

    public AnimationRegistry(WelcomeMat plugin) {
        this.plugin = plugin;
        this.animations = new HashMap<>();
        this.randomPool = new ArrayList<>();
        this.random = new Random();
        registerDefaultAnimations();
    }

    private void registerDefaultAnimations() {
        registerAnimation(new FireSpiralAnimation(plugin));
        registerAnimation(new GreenSpikeFireworkAnimation(plugin));
        registerAnimation(new CopperGolemAnimation(plugin));
        registerAnimation(new MoonRiseAnimation(plugin));
        registerAnimation(new HeartBloomAnimation(plugin));
        registerAnimation(new RuneCircleAnimation(plugin));
        registerAnimation(new SkywardCascadeAnimation(plugin));
        registerAnimation(new DataStreamAnimation(plugin));
        registerAnimation(new ConfettiBurstAnimation(plugin));
    }

    public void registerAnimation(Animation animation) {
        String key = animation.getName().toLowerCase();
        animations.put(key, animation);
        if (animation.isRandom()) {
            randomPool.add(key);
        }
    }

    public Animation getAnimation(String name) {
        if (name == null) {
            return null;
        }
        return animations.get(name.toLowerCase());
    }

    public boolean hasAnimation(String name) {
        return getAnimation(name) != null;
    }

    public List<String> getAvailableAnimations() {
        List<String> ids = new ArrayList<>(animations.keySet());
        Collections.sort(ids);
        return ids;
    }

    public boolean playAnimation(Player player, String animationName) {
        Animation animation = getAnimation(animationName);
        if (animation == null) {
            return false;
        }
        animation.play(player);
        return true;
    }

    public boolean playRandomAnimation(Player player) {
        if (randomPool.isEmpty()) {
            return false;
        }
        String id = randomPool.get(random.nextInt(randomPool.size()));
        return playAnimation(player, id);
    }

    public List<Animation> getRegisteredAnimations() {
        return new ArrayList<>(animations.values());
    }
}