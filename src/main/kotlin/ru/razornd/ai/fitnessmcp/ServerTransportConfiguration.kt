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

import io.modelcontextprotocol.common.McpTransportContext
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStatelessServerTransport
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.principalOrNull
import tools.jackson.databind.json.JsonMapper

@Configuration(proxyBeanMethods = false)
class ServerTransportConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun serverTransport(
        @Qualifier("mcpServerJsonMapper")
        jsonMapper: JsonMapper,
        serverProperties: McpServerStreamableHttpProperties
    ): WebMvcStatelessServerTransport = WebMvcStatelessServerTransport.builder()
        .jsonMapper(JacksonMcpJsonMapper(jsonMapper))
        .messageEndpoint(serverProperties.mcpEndpoint)
        .contextExtractor { request ->
            val principal = checkNotNull(request.principalOrNull()) {
                "Cannot create MCP transport context without an authenticated principal. " +
                        "The server is configured as an OAuth2 resource server, so every MCP request must be " +
                        "authenticated before reaching the transport context extractor. Check the security filter " +
                        "chain and ensure the request includes a valid bearer token."
            }
            McpTransportContext.create(mapOf(USER_NAME_CONTEXT_KEY to principal.name))
        }
        .build()

}
