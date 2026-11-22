package org.finance.api

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor

object MockJwtTestSupport {
    fun client(
        subject: String = "test-client",
        scopes: Set<String> = emptySet(),
    ): RequestPostProcessor =
        jwt().jwt { token ->
            token.subject(subject)
                .claim("scope", scopes.joinToString(" "))
        }
}
