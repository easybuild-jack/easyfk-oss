# EasyFK OSS

<p align="center">
  <b>Unified file storage service for the EasyBuild ecosystem</b><br/>
  One upload API for every object storage — switch backends by configuration, not by code.
</p>

<p align="center">
  <a href="https://www.easybuild.pro/"><img src="https://img.shields.io/badge/ecosystem-EasyBuild-blueviolet?style=flat-square" alt="EasyBuild"/></a>
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?style=flat-square" alt="Spring Boot 3.5.x"/>
  <img src="https://img.shields.io/badge/version-3.5.16-lightgrey?style=flat-square" alt="Version 3.5.16"/>
  <img src="https://img.shields.io/badge/license-Apache--2.0-green?style=flat-square" alt="Apache-2.0"/>
</p>

<p align="center">
  <b>English</b> &nbsp;·&nbsp; <a href="README.zh-CN.md">简体中文</a> &nbsp;·&nbsp; <a href="README.zh-TW.md">繁體中文</a> &nbsp;·&nbsp; <a href="README.ja.md">日本語</a>
</p>

---

> **About this project's place in the ecosystem**
>
> EasyFK OSS is a component of the **EasyBuild (易构) ecosystem**. The base capabilities, conventions and version governance it compiles and runs against all come from that ecosystem: the unified response model and common utilities from `easyfk-core`, caching and state storage from `cache-redis`, production web configuration from `web-prd`, and dependency versions from the `easyfk-dependencies` BOM. Used outside the ecosystem, these foundations are missing.
>
> For an overview of the ecosystem, the EasyFK core foundation, related components and open-source business modules, see **[https://www.easybuild.pro/](https://www.easybuild.pro/)**.

## Overview

EasyFK OSS takes care of the part of a business system that is easy to rebuild badly and easy to get wrong: where files actually go, how to move them somewhere else later, and how to upload large files without breaking.

Application code only does two things — upload a file, get back a URL. Whether the bytes land in MinIO, Alibaba Cloud OSS or on a local disk is a matter of configuration, not code.

It addresses three problems:

- **No vendor lock-in** — one API in front of many object storage services. Moving to another cloud vendor, or from public cloud to a private deployment, is a configuration change.
- **Files stay organised** — uploads are filed automatically by business line and file type, so different features never mix their files together.
- **Large files behave** — chunked upload for big files, resume after an interrupted connection, and no re-transfer for files already stored.

Typical use cases: attachments, images and video for business applications; media libraries and file centers; projects that need multiple storage backends or may change cloud vendors later; hybrid private-cloud and public-cloud deployments; several business lines sharing one storage service.

## Features

- **Pluggable backends** — MinIO, Alibaba Cloud OSS, Tencent Cloud COS, Huawei Cloud OBS, Qiniu, Baidu BOS, Amazon S3 (and S3-compatible services), local disk. Switch by configuration, business code untouched.
- **Consistent API** — direct upload and chunked upload share one configuration and one calling convention.
- **Large-file friendly** — chunked upload avoids per-request size and timeout limits, with resume, status query and abort.
- **Deduplication** — identical files return the existing URL instead of being transferred again.
- **Automatic image handling** — optional compression and thumbnail generation at upload time, no extra work for the caller.
- **Business-aware layout** — pass a business name and files are filed under the matching directory.
- **Cluster ready** — upload state is stored centrally, so resume works across service instances.
- **Ready to adopt** — follows EasyFK conventions for responses, exceptions, logging and configuration, with OpenAPI descriptions on every endpoint.

## Supported storage backends

| Backend | Configuration sample |
|---------|----------------------|
| MinIO | `config/application-minio.yml` |
| Alibaba Cloud OSS | `config/application-aliyun.yml` |
| Tencent Cloud COS | `config/application-tencent.yml` |
| Huawei Cloud OBS | `config/application-huawei.yml` |
| Qiniu | `config/application-qiniu.yml` |
| Baidu BOS | `config/application-baidu.yml` |
| Amazon S3 & S3-compatible | `config/application-amazon.yml` |
| Local disk | `config/application-local.yml` |

## Requirements

- Java 21
- Spring Boot 3.5.x
- Redis — only required for chunked upload (upload state and deduplication records)

## Documentation

Installation, backend configuration, endpoint reference, front-end examples and FAQ are covered in the developer guide:

**[docs/oss开发手册.md](docs/oss开发手册.md)** *(written in Chinese)*

## Contributing

Issues and pull requests are welcome. For anything beyond a small fix, please open an issue first so the approach can be discussed before code is written.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

## Links

- EasyBuild ecosystem: <https://www.easybuild.pro/>
- EasyFK core foundation: <https://www.easybuild.pro/easyfk>
- Organization: <https://github.com/easybuild-jack>
