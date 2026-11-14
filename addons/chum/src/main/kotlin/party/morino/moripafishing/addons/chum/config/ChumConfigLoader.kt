package party.morino.moripafishing.addons.chum.config

import com.charleskorn.kaml.Yaml
import org.bukkit.Material

/**
 * `config.yml` を [ChumConfig] として読み込み、値を検証する。
 *
 * 魚・レアリティの ID が登録済みかどうかはここでは確かめない (Core の再読み込みで変わるため、抽選時に確かめる)。
 */
object ChumConfigLoader {
    private val ID_PATTERN = Regex("[a-z0-9_-]+")

    /**
     * YAML を読み込んで検証する。
     *
     * @param text `config.yml` の内容
     * @throws IllegalArgumentException YAML として不正、未知のキーがある、または値が不正な場合
     */
    fun load(text: String): ChumConfig = Yaml.default.decodeFromString(ChumConfig.serializer(), text).also(::validate)

    /**
     * 値を検証する。不正な値を全て集めて 1 つの例外にする。
     *
     * @throws IllegalArgumentException 値が不正な場合
     */
    fun validate(config: ChumConfig) {
        val errors = mutableListOf<String>()
        if (!config.maxMultiplier.isFinite() || config.maxMultiplier < 1.0) {
            errors += "maxMultiplier must be finite and at least 1: ${config.maxMultiplier}"
        }
        config.chums.forEach { (id, chum) -> errors += validate(id, chum) }
        require(errors.isEmpty()) { "Invalid chum config:\n" + errors.joinToString("\n") { "- $it" } }
    }

    private fun validate(
        id: String,
        chum: ChumDefinition,
    ): List<String> {
        val path = "chums.$id"
        val errors = mutableListOf<String>()
        if (!ID_PATTERN.matches(id)) errors += "$path: id must consist of a-z, 0-9, '_' and '-'"
        val material = Material.matchMaterial(chum.item.material)
        if (material == null || !material.isItem) errors += "$path.item.material: '${chum.item.material}' is not an item material"
        if (chum.item.customModelData.any { !it.isFinite() }) errors += "$path.item.customModelData must be finite"
        if (chum.durationSeconds <= 0) errors += "$path.durationSeconds must be positive: ${chum.durationSeconds}"
        errors += validateScope(path, chum)
        errors += validateFactors("$path.rarityMultipliers", chum.rarityMultipliers)
        errors += validateFactors("$path.fishMultipliers", chum.fishMultipliers)
        errors += validateFactors("$path.fishWeights", chum.fishWeights)
        return errors
    }

    private fun validateScope(
        path: String,
        chum: ChumDefinition,
    ): List<String> {
        val errors = mutableListOf<String>()
        val charges = chum.charges
        when {
            charges == null -> Unit
            chum.scope != ChumScope.ANGLER -> errors += "$path.charges is only allowed for the ANGLER scope"
            charges <= 0 -> errors += "$path.charges must be positive: $charges"
        }
        val radius = chum.radius
        when {
            chum.scope != ChumScope.AREA -> if (radius != null) errors += "$path.radius is only allowed for the AREA scope"
            radius == null -> errors += "$path.radius is required for the AREA scope"
            !radius.isFinite() || radius <= 0.0 -> errors += "$path.radius must be finite and positive: $radius"
        }
        return errors
    }

    private fun validateFactors(
        path: String,
        values: Map<String, Double>,
    ): List<String> =
        values
            .filterValues { !it.isFinite() || it < 0.0 }
            .map { (key, value) -> "$path.$key must be finite and not negative: $value" }
}
