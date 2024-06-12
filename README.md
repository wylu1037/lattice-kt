<h1 align="center">Lattice Kotlin</h1>

<p align="center">
    <a href="#rust">kotlin</a>  &#xa0; | &#xa0;
    <a href="#blockchain">blockchain</a>  &#xa0; | &#xa0;
    <a href="#contract">contract</a>  &#xa0; | &#xa0;
    <a href="wasm">wasm</a>
</p>

<h1>简介</h1>
Kotlin 语言版本的链 SDK。

<h1 id="WebAssembly ">WebAssembly</h1>
WASM

<h1>计划</h1>

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