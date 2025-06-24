object ServiceNameMapper {
    fun mapServiceNameToId(serviceName: String): String {
        return when (serviceName.lowercase()) {
            "policy" -> "Policy"
            "asset" -> "Asset"
            "holiday calendar" -> "Holiday Calendar"
            "communique" -> "Communique"
            else -> serviceName
        }
    }
}
