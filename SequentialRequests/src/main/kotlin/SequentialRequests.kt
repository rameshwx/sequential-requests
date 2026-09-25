package sequential_requests

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.coroutines.runBlocking

private const val BASE_URL = "https://train.uxi.asia"
private const val API_KEY = "886d2dea9ac511733d4853daa30b396c72b262e2d460009039d19389019fe257"

private val httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(15))
    .build()

fun main(args: Array<String>) = runBlocking {
    println("=== Sequential Grocery Order ===\n")

    try {
        println("STEP 1")
        val catalogResponse = getApi(
            step = "Catalog",
            path = "/rest/v1/grocery_catalog?select=id,name,price_cents,is_active&is_active=eq.true"
        )
        val itemId = extractStringField(
            json = catalogResponse,
            fieldName = "id",
            step = "Catalog"
        )
        println("First active item ID: $itemId\n")

        println("STEP 2")
        val ridersResponse = getApi(
            step = "Available Riders",
            path = "/rest/v1/delivery_riders?select=id,display_name,vehicle_type,service_area,availability&availability=eq.available"
        )
        println("Available riders: $ridersResponse\n")

        println("STEP 3")
        val quoteResponse = postApi(
            step = "Create Quote",
            path = "/rest/v1/rpc/create_order_quote",
            body = """{"p_item_id":"$itemId","p_quantity":1}"""
        )
        val quoteId = extractStringField(
            json = quoteResponse,
            fieldName = "quote_id",
            step = "Create Quote"
        )
        println("Quote ID: $quoteId")
        println("Quote response: $quoteResponse\n")

        println("STEP 4")
        val orderResponse = postApi(
            step = "Create Delivery Order",
            path = "/rest/v1/rpc/create_delivery_order",
            body = """{"p_quote_id":"$quoteId","p_customer_name":"Sam Taylor","p_delivery_address":"42 Palm Grove, Colombo 3","p_customer_note":"Please ring the bell"}"""
        )
        println("Order response: $orderResponse")

        println("\n==============================")
        println("Order created successfully")
        println("==============================")

    } catch (e: RideApiException) {
        println("\n==============================")
        println("Sequential requests stopped")
        println("==============================")
        println("Failed step : ${e.step}")
        println("Reason      : ${e.message}")

    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
    }
}

private suspend fun getApi(step: String, path: String): String {
    val request = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + path))
        .timeout(Duration.ofSeconds(30))
        .header("Accept", "application/json")
        .GET()
        .build()

    return sendRequest(step, request)
}

private suspend fun postApi(step: String, path: String, body: String): String {
    val request = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + path))
        .timeout(Duration.ofSeconds(30))
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .header("x-api-key", API_KEY)
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build()

    return sendRequest(step, request)
}

private suspend fun sendRequest(step: String, request: HttpRequest): String {
    try {
        val response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() !in 200..299) {
            throw RideApiException(
                step = step,
                message = "HTTP ${response.statusCode()}: ${response.body()}"
            )
        }

        return response.body()

    } catch (e: RideApiException) {
        throw e
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw RideApiException(step, "Request was interrupted")
    } catch (e: Exception) {
        throw RideApiException(step, "Request failed: ${e.message}")
    }
}

private fun extractStringField(json: String, fieldName: String, step: String): String {
    val pattern = Regex("\"${Regex.escape(fieldName)}\"\\s*:\\s*\"([^\"]+)\"")
    return pattern.find(json)?.groupValues?.get(1)
        ?: throw RideApiException(step, "Response did not contain '$fieldName'")
}

class RideApiException(
    val step: String,
    message: String
) : Exception(message)
