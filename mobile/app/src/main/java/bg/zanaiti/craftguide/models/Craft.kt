package bg.zanaiti.craftguide.models

data class Craft (
    val id: Long,
    val translations: Map<String, CraftTranslation>,
    val imageUrl: String,
    val animationUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean
)

data class CraftTranslation(
    val name: String,
    val description: String,
    val historicalFacts: String,
    val makingProcess: String
)