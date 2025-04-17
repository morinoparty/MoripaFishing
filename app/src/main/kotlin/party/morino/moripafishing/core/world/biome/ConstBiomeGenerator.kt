package party.morino.moripafishing.core.world.biome

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

class ConstBiomeGenerator(val biome: String) : BiomeProvider() {
    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
        val namespace = NamespacedKey.fromString(biome) ?: throw IllegalArgumentException("Invalid biome name: $biome")
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)
            .getOrThrow(namespace)
    }

    override fun getBiomes(worldInfo: WorldInfo): List<Biome> {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).toList()
    }
}