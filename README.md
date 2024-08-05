<h1 align="center">Lattice Kotlin</h1>

<p align="center">
    <a href="#rust">kotlin</a>  &#xa0; | &#xa0;
    <a href="#blockchain">blockchain</a>  &#xa0; | &#xa0;
    <a href="#contract">contract</a>  &#xa0; | &#xa0;
    <a href="wasm">wasm</a>
</p>

<h1>Intro</h1>
Blockchain sdk that written in Kotlin programming language.

## Tech stack

- [x] [Retry](https://github.com/michaelbull/kotlin-retry) A multiplatform higher-order function for retrying operations
  that may fail.

<h1 id="WebAssembly ">WebAssembly</h1>

[WASM](https://kotlinlang.org/docs/wasm-overview.html)

<h1>Plan</h1>

- [ ] [Cli](https://github.com/Kotlin/kotlinx-cli) Pure Kotlin implementation of a generic CLI parser.

<h1>Docs</h1>
<h2>use docker</h2>

```shell
docker run --name lattice-kt-docs -p 8020:80 -d wylu1037/lattice-kt-docs:v1.0.0
```

<h2>use docker compose</h2>

docker-compose

```shell
version: '3'
services:
  lattice-kt-docs:
    image: wylu1037/lattice-kt-docs:v1.0.0
    ports:
      - "80:80"
```