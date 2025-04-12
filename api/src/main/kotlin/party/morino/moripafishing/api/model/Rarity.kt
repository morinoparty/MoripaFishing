package party.morino.moripafishing.api.model

/**
 * 魚のレアリティを表す列挙型
 */
enum class Rarity(string: String) {

    /**
     * ゴミ
     */
    JUNK("junk"),

    /**
     * 一般的な魚
     */
    COMMON("common"),

    /**
     * 珍しい魚
     */
    RARE("rare"),

    /**
     * 非常に珍しい魚
     */
    EPIC("epic"),

    /**
     * 伝説の魚
     */
    LEGENDARY("legendary"),

    /**
     * 神話の魚
     */
    MYTHIC("mythic");

} 