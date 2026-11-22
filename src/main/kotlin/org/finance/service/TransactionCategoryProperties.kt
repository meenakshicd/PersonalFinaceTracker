package org.finance.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "finance.transaction-categories")
class TransactionCategoryProperties {
    var keywords: Map<String, String> = emptyMap()
}
