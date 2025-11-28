import org.bukkit.Particle;

public class TempParticleList {
    public static void main(String[] args) {
        for (Particle particle : Particle.values()) {
            System.out.println(particle.name());
        }
    }
}
