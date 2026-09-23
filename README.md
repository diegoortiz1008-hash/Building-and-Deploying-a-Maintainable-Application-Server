# Maintainable Application Server

A small Java 17 application server built with the standard library. It serves HTML, CSS, JavaScript and binary assets, while applications register GET endpoints through lambda functions. The server deliberately handles one client at a time.

## Architecture

```text
Application -> WebFramework -> Router -> lambda handler
                         \-> HttpServer -> StaticFileService
```

Think of it as an office building: the `HttpServer` is the entrance and receptionist, the `Router` is the directory, route lambdas are individual offices, and `StaticFileService` is the document archive. Environment variables are the building's configuration panel. The `/shutdown` endpoint is the closing procedure: the current visitor receives a response before the building closes.

| Component | Responsibility |
| --- | --- |
| `Application` | Declares static-file location and application routes. |
| `WebFramework` | Exposes `get`, `staticfiles`, `start`, and `stop`. |
| `Router` | Maps a GET path to a lambda handler. |
| `HttpServer` | Parses a connection and writes an HTTP response sequentially. |
| `Request` / `Response` | Isolate query values and response metadata from socket code. |
| `StaticFileService` | Provides static files only when no route matches. |

This separation keeps HTTP mechanics stable while new business endpoints are added in `Application`, without changing the connection loop.

## Run locally

```bash
./mvnw.cmd clean package
java -jar target/application-server.jar
```

Open `http://localhost:8080/`. Useful direct URLs are:

- `http://localhost:8080/index.html`
- `http://localhost:8080/styles.css`
- `http://localhost:8080/images/logo.png`
- `http://localhost:8080/hello?name=Pedro`
- `http://localhost:8080/pi`

An unknown path returns `404 Not Found` with a plain-text body.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | Port bound on all interfaces; cloud platforms provide it. |
| `GREETING_PREFIX` | `Hello` | Prefix used by `/hello`. |
| `APP_ENV` | `development` | Enables `/shutdown` only in development. |
| `STATIC_FILES_PATH` | classpath `/webroot` | Optional external static-files directory. |

For a local graceful shutdown visit `http://localhost:8080/shutdown`. In production, configure `APP_ENV=production`; `/shutdown` is not registered and returns 404.

## Container deployment

The project is ready for any container platform that supplies `PORT`, such as Render, Railway, or AWS App Runner.

```bash
docker build -t maintainable-application-server .
docker run --rm -p 8080:8080 -e APP_ENV=production -e GREETING_PREFIX=Hello maintainable-application-server
```

On the cloud platform, set `APP_ENV=production` and optionally `GREETING_PREFIX`; do not configure or commit credentials. The public deployment URL is pending because a cloud account/platform has not yet been provided for this repository.

## Verification performed

`./mvnw.cmd test` passes. A packaged local run verified `/hello?name=Pedro` (`Hello Pedro`), `/pi`, `/styles.css`, a 404 for `/missing`, graceful `/shutdown` in development, and a 404 for `/shutdown` with `APP_ENV=production`. Unit tests cover decoded multiple query values, routing, and text/binary static resources.
