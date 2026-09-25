# Sequential API Requests

Kotlin/JVM console demo showing a dependent catalog → quote → order API workflow with direct suspend-function calls inside `runBlocking`.

The complete project is in [`SequentialRequests/`](SequentialRequests/), with the interview-focused explanation in [`SequentialRequests/README.md`](SequentialRequests/README.md). That guide explains why the requests are sequential, why this sample uses blocking `HttpClient.send` rather than non-blocking I/O, and how to build and run it.

To build from a clone of this repository:

```bash
cd SequentialRequests
./gradlew build
```

Running `./gradlew run` creates a real order through the live API and deducts inventory. The temporary demo API key is embedded in the source and visible in this public repository; deactivate or rotate it after the interview demo.
