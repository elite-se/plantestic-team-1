package de.unia.se.plantestic

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException

// Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> is meant as a multi key hashmap which uses
// actor (receiver), url, method, roundtrip, requests/responses, variableName to get value/xPath (check out RestAssured.png)
class AutoMocker(private val specs: Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>, port: Int) {
    private val wireMockServer = WireMockServer(WireMockConfiguration.options().port(port).notifier(ConsoleNotifier(true)))

    fun addMock(mockName: String) {
        // the actor is mocked => actorName = mockName
        val actor = (specs[mockName]) ?: error("The mock with the given name does not exist")
        for ((urlName, url) in actor)
            for ((methodName, method) in url)
                for (roundtip in method.values) {
                    val pathWithMethod = preparepathWithMethod(mockName, urlName, methodName)
                    val requests = roundtip["requests"] ?: error("The requests were never initialized for $methodName")
                    val responses = roundtip["responses"] ?: error("The requests were never initialized for $methodName")
                    wireMockServer.stubFor(pathWithMethod.willReturn(prepareResponse(requests, responses)))
                }
    }

    private fun preparepathWithMethod(mockName: String, urlName: String, methodName: String): MappingBuilder {
        val matchPath = WireMock.urlPathMatching("/mock/$mockName$urlName")
        return when (methodName) {
            "GET" -> WireMock.get(matchPath)
            "POST" -> WireMock.post(matchPath)
            else -> throw ValueException("The method has to be empty (to assume GET) or POST! The method was $methodName.")
        }
    }

    private fun prepareResponse(requests: Map<String, String>, responses: Map<String, String>) : ResponseDefinitionBuilder {
        val finalResponse = WireMock.aResponse()
        //if (specs[mock] == null) res.withStatus(200)  // already uses status 200, would be nice to return other status
        // using requests to customize the responses could be necessary or nice to have
        //if (specs[mock]?.get("body") is String) res.withBody(requests[mock]?.get("body").toString())
        return finalResponse.withBody(responses.toString())
    }

    fun start() {
        wireMockServer.start()
    }
}