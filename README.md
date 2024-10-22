<h1 align="center">Lattice Kotlin</h1>

<p align="center">
    <img alt="Static Badge" src="https://img.shields.io/badge/kotlin-v1.9.23-blue?logo=kotlin">
    <img alt="Static Badge" src="https://img.shields.io/badge/build-passing-green?logo=github">
    <img alt="Static Badge" src="https://img.shields.io/badge/release-v1.0.0-blue?logo=adguard">
    <img alt="Static Badge" src="https://img.shields.io/badge/Evm-support-orange?logo=ethereum">
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
<h2>GitHub Pages</h2>

https://wylu1037.github.io/lattice-kt/

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