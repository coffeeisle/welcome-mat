package xyz.coffeeisle.welcomemat.effects;

import xyz.coffeeisle.welcomemat.effects.animations.Animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class AnimationRegistry {
    private final Map<String, Animation> animations;

    public AnimationRegistry() {
        this.animations = new HashMap<>();
    }

    public void registerAnimation(Animation animation) {
        animations.put(animation.getName().toLowerCase(), animation);
    }

    public Animation getAnimation(String name) {
        return animations.get(name.toLowerCase());
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name.toLowerCase());
    }

    public List<String> getAvailableAnimations() {
        return new ArrayList<>(animations.keySet());
    }
}