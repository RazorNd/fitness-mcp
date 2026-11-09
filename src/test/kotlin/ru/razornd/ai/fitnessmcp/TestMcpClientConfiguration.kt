/*
 * Copyright 2026 Daniil <RazorNd> Razorenov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.razornd.ai.fitnessmcp

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import tools.jackson.databind.json.JsonMapper
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.time.Instant
import java.util.Date

@TestConfiguration(proxyBeanMethods = false)
class TestMcpClientConfiguration {

    @Lazy
    @Bean
    fun mcpClient(
        environment: Environment,
        jsonMapper: JsonMapper
    ): McpSyncClient = McpClient.sync(
        HttpClientStreamableHttpTransport.builder("http://localhost:${environment.getProperty("local.server.port")}")
            .jsonMapper(JacksonMcpJsonMapper(jsonMapper))
            .httpRequestCustomizer { request, _, _, _, _ ->
                request.header("Authorization", "Bearer ${createTestJwt(environment.getRequiredProperty("wiremock.server.baseUrl"))}")
            }
            .endpoint("/mcp")
            .build()
    ).build()

    private fun createTestJwt(issuerBaseUrl: String): String {
        val privateKeyBytes = ClassPathResource("wiremock/__files/test-signing-key.pem").inputStream.bufferedReader()
            .use { it.readText() }
            .replace(Regex("-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\\s"), "")
            .let(Base64.getDecoder()::decode)

        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
        val now = Instant.now()
        val jwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID("fitness-test-key")
                .build(),
            JWTClaimsSet.Builder()
                .issuer("$issuerBaseUrl/issuer")
                .subject("fitness-test-user")
                .audience("fitness-mcp")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .claim("scope", "mcp")
                .build()
        )

        jwt.sign(RSASSASigner(privateKey))
        return jwt.serialize()
    }

}
