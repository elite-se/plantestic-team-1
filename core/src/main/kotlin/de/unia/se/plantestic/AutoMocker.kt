package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException

class AutoMocker(private val requests: Map<String, Map<String, Any>>, private val expectations: Map<String, Map<String, Any>>, port: Int) {
    private val wireMockServer = WireMockServer(WireMockConfiguration.options().port(port).notifier(ConsoleNotifier(true)))

    fun addMock(mock: String) {
        val path = (requests[mock] ?: error("No url was defined for this mock"))["url"]
        val matchPath = WireMock.urlPathMatching("/mock/$mock$path")
        if (!requests.containsKey(mock)) throw ValueException("The requested mock $mock was not found in expectations")
        val pathBuilder = when (val method = requests[mock]?.get("method")?.toString()?.toUpperCase() ?: "GET") {
            "GET" -> WireMock.get(matchPath)
            "POST" -> WireMock.post(matchPath)
            else -> throw ValueException("The method has to be empty (to assume GET) or POST! The method was $method.")
        }
        wireMockServer.stubFor(pathBuilder.willReturn(prepareResponse(mock)))
    }

    private fun prepareResponse(mock: String) : ResponseDefinitionBuilder {
        val res = WireMock.aResponse()
        if (requests[mock] == null) res.withStatus(200)
        if (requests[mock]?.get("body") is String) res.withBody(requests[mock]?.get("body").toString())
        return res
    }

    fun start() {
        wireMockServer.start()
    }
}