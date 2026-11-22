# EasyFK OSS

<p align="center">
  <b>易构生态的统一文件存储服务</b><br/>
  一套上传接口对接所有对象存储 —— 换存储只改配置，不改代码。
</p>

<p align="center">
  <a href="https://www.easybuild.pro/"><img src="https://img.shields.io/badge/%E7%94%9F%E6%80%81-EasyBuild-blueviolet?style=flat-square" alt="EasyBuild"/></a>
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?style=flat-square" alt="Spring Boot 3.5.x"/>
  <img src="https://img.shields.io/badge/%E7%89%88%E6%9C%AC-3.5.16-lightgrey?style=flat-square" alt="3.5.16"/>
  <img src="https://img.shields.io/badge/%E8%AE%B8%E5%8F%AF%E8%AF%81-Apache--2.0-green?style=flat-square" alt="Apache-2.0"/>
</p>

<p align="center">
  <a href="README.md">English</a> &nbsp;·&nbsp; <b>简体中文</b> &nbsp;·&nbsp; <a href="README.zh-TW.md">繁體中文</a> &nbsp;·&nbsp; <a href="README.ja.md">日本語</a>
</p>

---

> **关于本项目在生态中的位置**
>
> EasyFK OSS 是 **易构（EasyBuild）生态** 中的一个组件。它编译与运行所依赖的基础能力、技术规范与版本治理均来自该生态：统一响应模型与通用工具来自 `easyfk-core`，缓存与状态存储来自 `cache-redis`，Web 生产环境配置来自 `web-prd`，依赖版本由 `easyfk-dependencies` BOM 统一约束。脱离易构生态单独使用，将缺少这些基础支撑。
>
> 易构生态的完整介绍、核心底座 EasyFK（易架构）、配套组件与开源业务模块，参见 **[https://www.easybuild.pro/](https://www.easybuild.pro/)**。

## 项目简介

业务系统里有一类事情，自己做容易做歪、踩坑又多：文件到底存在哪、以后想换存储怎么办、大文件怎么传才不断。EasyFK OSS 就是把这部分收进一个模块里。

业务代码只做两件事——上传一个文件，拿到一个访问地址。文件最终落在 MinIO、阿里云还是本地磁盘，由配置决定，业务代码不用改。

它解决三个问题：

- **不被厂商绑住**：一套接口对接多家对象存储，换云厂商、或从公有云迁到私有化环境，只改配置。
- **文件有序可管**：按业务线与文件类型自动归档，不同功能的文件互不混杂。
- **大文件传得稳**：大文件分片上传，断网可续传，已存过的文件不重复传。

典型场景：业务系统的附件、图片与视频上传；素材库与文件中心；需要多种存储后端、或未来可能更换云厂商的项目；私有化与公有云混合部署；多条业务线共用一套存储服务。

## 主要特点

- **存储平台可插拔**：MinIO、阿里云 OSS、腾讯云 COS、华为云 OBS、七牛云、百度云 BOS、Amazon S3（含兼容协议）、本地磁盘，改配置即切换，业务零改动。
- **接口统一**：直接上传与分片上传共用一套配置与调用习惯。
- **大文件友好**：分片上传规避单请求体积与超时限制，支持断点续传、状态查询与取消上传。
- **秒传去重**：相同文件直接返回已有地址，不再重复传输。
- **图片自动处理**：上传时按需压缩、生成缩略图，调用方无需另外处理图片规格。
- **按业务分类存储**：带上业务标识即自动归档到对应目录。
- **支持集群部署**：上传状态集中存放，多实例下仍可跨节点续传。
- **开箱即用**：沿用 EasyFK 的响应、异常、日志与配置风格，所有接口自带 OpenAPI 描述。

## 支持的存储平台

| 平台 | 配置样例 |
|------|----------|
| MinIO | `config/application-minio.yml` |
| 阿里云 OSS | `config/application-aliyun.yml` |
| 腾讯云 COS | `config/application-tencent.yml` |
| 华为云 OBS | `config/application-huawei.yml` |
| 七牛云 | `config/application-qiniu.yml` |
| 百度云 BOS | `config/application-baidu.yml` |
| Amazon S3 及兼容协议 | `config/application-amazon.yml` |
| 本地磁盘 | `config/application-local.yml` |

## 环境要求

- Java 21
- Spring Boot 3.5.x
- Redis —— 仅分片上传需要（存放上传状态与秒传记录）

## 文档

依赖引入、平台配置、接口说明、前端对接示例与常见问题，见开发手册：

**[docs/oss开发手册.md](docs/oss开发手册.md)**

## 参与贡献

欢迎提交 Issue 与 Pull Request。除小修正外，建议先开 Issue 讨论方案，再动手写代码。

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。

## 相关链接

- 易构生态：<https://www.easybuild.pro/>
- EasyFK 核心底座：<https://www.easybuild.pro/easyfk>
- 组织主页：<https://github.com/easybuild-jack>
