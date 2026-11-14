# LimitX – Lightweight Spring Boot Rate Limiting Library

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ridham-patel19/LimiterX)](https://central.sonatype.com/artifact/io.github.ridham-patel19/LimiterX)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**LimitX** is a lightweight, reusable rate-limiting library for Spring Boot applications. It provides an easy way to control incoming HTTP requests using two rate-limiting algorithms: **Token Bucket** and **Sliding Window**.

With LimitX, developers can configure a global rate limit for their application or define custom rate limits for individual controller endpoints using the `@RateLimited` annotation.

The library integrates with Spring Boot through automatic configuration, so developers can add the dependency and start using rate limiting with minimal setup.

---

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
- [Annotation-Based Rate Limiting](#annotation-based-rate-limiting)
- [Available Algorithms](#available-algorithms)
- [How LimitX Works Internally](#how-limitx-works-internally)
- [Project Architecture](#project-architecture)
- [Technology Stack](#technology-stack)
- [Error Handling](#error-handling)
- [Testing](#testing)
- [Limitations](#limitations)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Two Rate-Limiting Algorithms:** Supports Token Bucket and Sliding Window.
- **Global Rate Limiting:** Configure a default rate limit for incoming application requests.
- **Endpoint-Level Configuration:** Apply custom limits to individual controller methods using `@RateLimited`.
- **Automatic Configuration:** Integrates with Spring Boot using auto-configuration.
- **Configurable Properties:** Manage rate limits through `application.properties`.
- **Endpoint Exclusions:** Exclude selected endpoints from rate limiting.
- **Thread Safety:** Uses synchronized request-processing methods and concurrent collections to maintain correctness under concurrent access.
- **Standard HTTP Response:** Returns HTTP `429 Too Many Requests` when a request exceeds its configured limit.
- **Reusable Dependency:** Distributed through Maven Central.

---

## Installation

Add the following dependency to your Spring Boot application's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.ridham-patel19</groupId>
    <artifactId>LimiterX</artifactId>
    <version>0.0.1</version>
</dependency>
```

Once Maven resolves the dependency, LimitX's Spring Boot auto-configuration can register the required beans and configure its MVC interceptor automatically.

---

## Configuration

LimitX supports global rate limiting through Spring Boot application properties.

Add the following configuration to your `application.properties`:

```properties
# Enable or disable LimitX
limitx.enable=true

# Select the rate-limiting algorithm
limitx.algorithm=TOKEN_BUCKET

# Maximum allowed requests / bucket capacity
limitx.limit=5

# Duration in milliseconds
limitx.duration=60000

# Endpoints excluded from rate limiting
limitx.excludes[0]=/health
limitx.excludes[1]=/actuator/health
```

### Configuration Properties

| Property | Description | Default |
|---|---|---|
| `limitx.enable` | Enables or disables LimitX auto-configuration | `true` |
| `limitx.algorithm` | Selects the rate-limiting algorithm | `TOKEN_BUCKET` |
| `limitx.limit` | Global request limit or token bucket capacity | `100` |
| `limitx.duration` | Algorithm duration in milliseconds | `60000` |
| `limitx.excludes` | List of request paths excluded from rate limiting | Empty list |

### Supported Algorithm Values

```properties
limitx.algorithm=TOKEN_BUCKET
```

or

```properties
limitx.algorithm=SLIDING_WINDOW
```

**Note:** Duration is interpreted differently by the two algorithms:

- Token Bucket: The configured duration determines the refill interval for one token.
- Sliding Window: The configured duration represents the complete rolling time window.

---

## Annotation-Based Rate Limiting

LimitX provides the `@RateLimited` annotation to configure rate limits at the controller method level.

This allows developers to define different rate limits for different API endpoints without changing the global configuration.

### Basic Usage

```java
import com.Rp.RateLimiter.annotation.RateLimited;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @RateLimited(limit = 3, duration = 60000)
    @GetMapping("/api/users")
    public String getUsers() {
        return "Users retrieved successfully!";
    }
}
```

In this example:

- The endpoint allows up to 3 requests initially.
- The configured duration is 60,000 milliseconds.
- The default algorithm is Token Bucket.
- Once the available tokens are exhausted, additional requests are rejected until tokens are replenished.

### Using a Specific Algorithm

Developers can also explicitly select the algorithm for an endpoint.

```java
import com.Rp.RateLimiter.annotation.RateLimited;
import com.Rp.RateLimiter.core.RateLimiterAlgo;

@RestController
public class OrderController {

    @RateLimited(
        limit = 10,
        duration = 60000,
        algorithm = RateLimiterAlgo.SLIDING_WINDOW
    )
    @GetMapping("/api/orders")
    public String getOrders() {
        return "Orders retrieved successfully!";
    }
}
```

### Annotation Parameters

| Parameter | Description |
|---|---|
| `limit` | Maximum request count or token bucket capacity |
| `duration` | Duration in milliseconds |
| `algorithm` | Algorithm used for the endpoint; defaults to `TOKEN_BUCKET` |

**Important:** Endpoint-level configuration takes precedence over global configuration for annotated controller methods. Each annotated endpoint maintains its own rate-limiter instance.

---

## Available Algorithms

LimitX currently implements two rate-limiting algorithms.

### 1. Token Bucket Algorithm

The Token Bucket algorithm maintains a bucket containing a limited number of tokens.

Every incoming request consumes one token. If no tokens are available, the request is rejected.

Tokens are replenished lazily whenever a request arrives, based on the elapsed time since the previous refill.

**How it works:**

1. The bucket starts with its configured maximum capacity.
2. An incoming request checks the available token count.
3. If a token is available, one token is consumed and the request is allowed.
4. If no token is available, the request is rejected.
5. Tokens are replenished according to the configured refill interval.
6. The bucket cannot exceed its maximum capacity.

**Example:**

```java
@RateLimited(
    limit = 5,
    duration = 60000,
    algorithm = RateLimiterAlgo.TOKEN_BUCKET
)
```

This creates a bucket with a capacity of 5 tokens and replenishes one token per 60 seconds, subject to the bucket's maximum capacity.

**Best suited for:**
- Handling short bursts of incoming traffic.
- Allowing temporary traffic spikes.
- Controlling request consumption with gradual token replenishment.

### 2. Sliding Window Algorithm

The Sliding Window algorithm maintains timestamps of previously accepted requests.

Before accepting a new request, it removes timestamps that have expired outside the configured time window.

If the number of remaining requests is below the configured limit, the new request is accepted. Otherwise, it is rejected.

**How it works:**

1. An incoming request arrives.
2. The algorithm retrieves the current system time.
3. Expired request timestamps are removed from the queue.
4. The algorithm checks the number of requests remaining in the current window.
5. If the count is below the configured limit, the current timestamp is recorded and the request is allowed.
6. Otherwise, the request is rejected.

**Example:**

```java
@RateLimited(
    limit = 10,
    duration = 60000,
    algorithm = RateLimiterAlgo.SLIDING_WINDOW
)
```

This allows a maximum of 10 accepted requests within the rolling 60-second window.

**Best suited for:**
- Enforcing request limits over a rolling time period.
- Maintaining a more precise request count within a configured window.
- APIs requiring predictable request limits.

### Algorithm Comparison

| Feature | Token Bucket | Sliding Window |
|---|---|---|
| Internal data structure | Token counter | Deque of timestamps |
| Request tracking | Available tokens | Request timestamps |
| Burst handling | Supports bursts up to bucket capacity | Enforces the rolling window limit |
| Replenishment | Time-based token refill | Expired timestamps are removed |
| Memory usage | Low | Depends on accepted requests in the active window |

---

## How LimitX Works Internally

LimitX uses Spring Boot auto-configuration and Spring MVC interceptors to apply rate limiting without requiring developers to manually integrate the implementation into every controller.

### Request Processing Flow

1. A client sends an HTTP request to the Spring Boot application.
2. The `LimitXInceptor` intercepts the request before it reaches the controller.
3. The interceptor checks whether the request path is included in the configured exclusion list.
4. If the endpoint has a `@RateLimited` annotation, its custom configuration is used.
5. Otherwise, the global rate limiter is used.
6. The `RateLimiterAlgoFactory` creates the appropriate implementation when an annotated endpoint's limiter is first needed.
7. The selected algorithm evaluates whether the request should be accepted.
8. If allowed, the request proceeds to the controller.
9. If rejected, the interceptor returns HTTP `429 Too Many Requests`.

### Architecture Highlights

**1. Strategy-Based Design**

Both algorithms implement a common `RateLimiter` interface:

```java
public interface RateLimiter {
    boolean allowRequest();
}
```

This makes it possible to switch between algorithms without changing the interceptor's request-processing logic.

**2. Factory Pattern**

`RateLimiterAlgoFactory` is responsible for creating the correct rate-limiter implementation based on the selected algorithm.

```java
RateLimiter limiter =
    RateLimiterAlgoFactory.create(config);
```

This separates object creation from the rest of the application.

**3. Spring Boot Auto-Configuration**

`LimitXAutoConfiguration` registers the required beans and enables configuration properties.

The library uses:

```text
META-INF/spring/
org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

to register its auto-configuration class with Spring Boot.

**4. Spring MVC Interceptor**

`LimitXWebConfiguration` registers `LimitXInceptor` with Spring MVC.

This allows rate limiting to happen before controller methods execute.

**5. Independent Endpoint Limiters**

A `ConcurrentHashMap` stores endpoint-specific rate-limiter instances.

Each annotated endpoint receives its own limiter, created when first accessed using `computeIfAbsent()`.

---

## Project Architecture

```text
LimitX
│
├── algorithm
│   ├── TokenBucketRateLimiter.java
│   └── SlidingWindowAlgorithm.java
│
├── annotation
│   └── RateLimited.java
│
├── config
│   ├── LimitXAutoConfiguration.java
│   ├── LimitXInceptor.java
│   ├── LimitXProperties.java
│   └── LimitXWebConfiguration.java
│
├── core
│   ├── RateLimitConfig.java
│   ├── RateLimiter.java
│   └── RateLimiterAlgo.java
│
├── factory
│   └── RateLimiterAlgoFactory.java
│
└── resources
    └── META-INF
        └── spring
            └── org.springframework.boot.autoconfigure
                └── AutoConfiguration.imports
```

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core implementation |
| Spring Boot | Auto-configuration and application integration |
| Spring MVC | HTTP request interception |
| Maven | Dependency management and packaging |
| JUnit 5 | Unit and integration testing |
| ConcurrentHashMap | Thread-safe endpoint limiter storage |
| Java Collections | Internal algorithm data structures |
| GPG | Artifact signing |
| Maven Central | Library distribution |

---

## Error Handling

When a request exceeds its configured rate limit, LimitX rejects it with:

**HTTP Status: `429 Too Many Requests`**

Response:

```json
{
  "error": "Too many requests"
}
```

The interceptor returns this response before the controller method is executed.

---

## Testing

LimitX includes unit tests and Spring Boot integration tests.

The tests cover:

- Token Bucket request acceptance and rejection.
- Sliding Window request acceptance and rejection.
- Token replenishment.
- Window expiration.
- Factory-based algorithm selection.
- Independent limiter instances.
- Concurrent request handling.
- Global rate-limiter bean registration.
- HTTP 429 response when the configured limit is exceeded.

Run the tests using Maven:

```bash
mvn test
```

Or execute a complete verification build:

```bash
mvn clean verify
```

---

## Limitations

- **In-memory state:** Rate-limiter state is maintained inside the application process. It is not shared across multiple application instances.
- **Single-instance enforcement:** In a distributed deployment, each application instance maintains its own counters and timestamps.
- **No client-specific tracking:** The current implementation applies limits globally or per annotated endpoint, rather than maintaining separate limits for individual IP addresses or users.
- **HTTP MVC integration:** The current implementation uses Spring MVC interceptors.

For distributed rate limiting, a shared storage mechanism such as Redis would be a possible future enhancement.

---

## Contributing

Contributions, suggestions, and improvements are welcome!

If you would like to contribute:

1. Fork the repository.
2. Create a new feature branch.
3. Implement your changes.
4. Add or update relevant tests.
5. Submit a pull request.

Repository:

https://github.com/Ridham-patel19/RateLimiterLibrary

---

## License

This project is licensed under the MIT License.

You are free to use, modify, and distribute this library under the terms of the license.

---

## Author

**Ridham Patel**

- GitHub: [@Ridham-patel19](https://github.com/Ridham-patel19)
- Project Repository: [RateLimiterLibrary](https://github.com/Ridham-patel19/RateLimiterLibrary)
- Maven Central: [io.github.ridham-patel19:LimiterX](https://central.sonatype.com/artifact/io.github.ridham-patel19/LimiterX)

---

**LimitX – Simple, reusable, and configurable rate limiting for Spring Boot applications.**
