package de.unia.se.plantestic

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException

// Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> is meant as a multi key hashmap which uses
// actor (receiver), url, method, roundtrip, requests/responses, variableName to get value/xPath (check out RestAssured.png)
class AutoMocker(private val specs: Map<String, Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>>, port: Int) {
    private val wireMockServer = WireMockServer(WireMockConfiguration.options().port(port).notifier(ConsoleNotifier(true)))

    fun addMock(mockName: String) {
        // the actor is mocked => actorName = mockName
        val actor = (specs[mockName]) ?: error("The mock $mockName does not exist")
        for ((urlName, url) in actor)
            for ((methodName, method) in url)
                for (roundtip in method.values) {
                    val pathWithMethod = preparePathWithMethod(mockName, urlName, methodName)
                    val status = roundtip["status"] ?: error("The requests were never initialized for $methodName")
                    val requests = roundtip["requests"] ?: error("The requests were never initialized for $methodName")
                    val responses = roundtip["responses"] ?: error("The requests were never initialized for $methodName")
                    if (requests.isEmpty()) wireMockServer.stubFor(pathWithMethod.willReturn(prepareResponse(status, responses)))
                    else wireMockServer.stubFor(
                        // containingCorrectRequests(pathWithMethod.willReturn(prepareResponse(status, responses)), requests))
                        pathWithMethod.willReturn(prepareResponse(status, responses))
                        .withQueryParams(prepareQueryMatcher(requests))) // see https://github.com/tomakehurst/wiremock/issues/383
                        // .withRequestBody(prepareBodyMatcher(requests)))
                }
    }

    private fun preparePathWithMethod(mockName: String, urlName: String, methodName: String): MappingBuilder {
        val matchPath = WireMock.urlPathMatching("/mock/$mockName$urlName")
        return when (methodName) {
            "GET" -> WireMock.get(matchPath)
            "POST" -> WireMock.post(matchPath)
            "PATCH" -> WireMock.patch(matchPath)
            "PUT" -> WireMock.put(matchPath)
            "DELETE" -> WireMock.delete(matchPath)
            else -> throw ValueException("The method has to be empty (to assume GET) or POST! The method was $methodName.")
        }
    }

    private fun prepareResponse(status: Map<String, String>, responses: Map<String, String>) : ResponseDefinitionBuilder {
        return WireMock.aResponse()
            .withStatus(status.keys.toList()[0].toInt())  // return first given status
            .withBody(ObjectMapper().writeValueAsString(responses))
    }

    private fun prepareQueryMatcher(requests: Map<String, String>) : Map<String, StringValuePattern> {
        val queryMatcher = HashMap<String, StringValuePattern>()
        for ((key, value) in requests) {
            queryMatcher[key] = WireMock.equalTo(value)
        }
        return queryMatcher
    }

    // exact Body Matcher, which fails with the xcall test
    private fun prepareBodyMatcher(requests: Map<String, String>): StringValuePattern? {
        var result = ""
        for ((key, value) in requests)
            result += "$key=$value&"
        return WireMock.equalTo(result.substring(0, result.length - 1))
    }

    // Body matcher which checks if a request is inside the body
    private fun containingCorrectRequests(pathWithMethod: MappingBuilder, requests: Map<String, String>): MappingBuilder {
        for ((key, value) in requests)
            pathWithMethod.withRequestBody(WireMock.containing("$key=$value"))
        return pathWithMethod
    }

    fun start() {
        wireMockServer.start()
    }
}
