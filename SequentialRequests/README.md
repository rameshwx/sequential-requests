# Sequential API Requests (Kotlin/JVM)

A small Kotlin console demo of an order workflow whose requests must happen in order because later calls depend on earlier responses.

## What it demonstrates

The program calls the Training Data API in this sequence:

1. Fetch the active grocery catalog and extract a product ID from the response.
2. Fetch available delivery riders.
3. Create a quote for the product ID from step 1 and extract the returned `quote_id`.
4. Create an order using the quote ID from step 3.

The rider request is sequenced before quote creation, but its response is only printed; the quote and order are the value-dependent part of the chain. If a request fails or a required field is missing, the flow stops and reports the failed step.

## Important coroutine distinction

`getApi`, `postApi`, and `sendRequest` are declared `suspend`, and `main` invokes them directly inside `runBlocking`. Direct calls execute in normal statement order: each call returns before the next line proceeds. A `suspend` function does not automatically mean asynchronous or non-blocking work.

In this project, the actual network operation is `java.net.http.HttpClient.send`, which is synchronous and blocks the calling thread. `runBlocking` also blocks its caller until the block completes. This demo illustrates ordered control flow and passing response data between calls; it does **not** demonstrate non-blocking network I/O, parallel requests, or Android networking. It is a Kotlin/JVM console application, not an Android app.

## Project structure

- `src/main/kotlin/SequentialRequests.kt` — entry point, request helpers, response-field extraction, and error handling.
- `build.gradle.kts` — Kotlin/JVM application configuration and dependencies.
- `settings.gradle.kts` — project name and Gradle plugin repositories.
- `gradle/wrapper/` and `gradlew` — pinned Gradle wrapper.

There are currently no dedicated automated test source files in this project.

## Requirements and commands

- JDK 17 or newer.
- A network connection to `https://train.uxi.asia`.
- The Gradle wrapper included in this repository; a separately installed Gradle is not required.

Build and run the configured checks:

```bash
./gradlew build
```

To run the console program:

```bash
./gradlew run
```

**Running the program creates a real quote and delivery order on the live API and deducts the ordered quantity from inventory.** Do not run it merely as a connectivity check. Use it only when you intentionally want to place the demo order.

## API and demo credential

The program uses these endpoints on `https://train.uxi.asia`:

- `GET /rest/v1/grocery_catalog` — reads active products.
- `GET /rest/v1/delivery_riders` — reads currently available riders.
- `POST /rest/v1/rpc/create_order_quote` — creates a quote for one product.
- `POST /rest/v1/rpc/create_delivery_order` — confirms the quote as an order.

The read endpoints are public. Quote and order mutations require an API key; this demo sends it in the `x-api-key` header. A demo key is temporarily embedded in the source for the interview walkthrough and will be visible in this public repository. It is for temporary demonstration only, not production use; deactivate or rotate it when the interview demo is over. The key value is intentionally not repeated here.

## Notes and limitations

- The program uses the JDK HTTP client and a small regular expression to extract string fields from the API's JSON responses. It is intentionally focused on this fixed workflow, not a general JSON/API client.
- Requests are made one at a time. No coroutine dispatcher, `async`, or concurrent network work is used.
- Quotes can expire, and an order confirmation consumes its quote. A successful order is a persistent side effect, not a simulated result.
