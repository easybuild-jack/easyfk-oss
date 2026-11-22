# EasyFK OSS

<p align="center">
  <b>易構生態的統一檔案儲存服務</b><br/>
  一套上傳介面串接所有物件儲存 —— 換儲存只要改設定，不用改程式碼。
</p>

<p align="center">
  <a href="https://www.easybuild.pro/"><img src="https://img.shields.io/badge/%E7%94%9F%E6%85%8B-EasyBuild-blueviolet?style=flat-square" alt="EasyBuild"/></a>
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?style=flat-square" alt="Spring Boot 3.5.x"/>
  <img src="https://img.shields.io/badge/%E7%89%88%E6%9C%AC-3.5.16-lightgrey?style=flat-square" alt="3.5.16"/>
  <img src="https://img.shields.io/badge/%E6%8E%88%E6%AC%8A-Apache--2.0-green?style=flat-square" alt="Apache-2.0"/>
</p>

<p align="center">
  <a href="README.md">English</a> &nbsp;·&nbsp; <a href="README.zh-CN.md">简体中文</a> &nbsp;·&nbsp; <b>繁體中文</b> &nbsp;·&nbsp; <a href="README.ja.md">日本語</a>
</p>

---

> **關於本專案在生態中的位置**
>
> EasyFK OSS 是 **易構（EasyBuild）生態** 中的一個元件。它編譯與執行所依賴的基礎能力、技術規範與版本治理都來自這個生態：統一回應模型與通用工具來自 `easyfk-core`，快取與狀態儲存來自 `cache-redis`，Web 正式環境設定來自 `web-prd`，相依版本由 `easyfk-dependencies` BOM 統一約束。脫離易構生態單獨使用，會缺少這些基礎支撐。
>
> 易構生態的完整介紹、核心底座 EasyFK（易架構）、配套元件與開源業務模組，請見 **[https://www.easybuild.pro/](https://www.easybuild.pro/)**。

## 專案簡介

業務系統裡有一類事情，自己做容易做歪、踩的坑也多：檔案到底存在哪裡、之後想換儲存怎麼辦、大檔案怎麼傳才不會斷。EasyFK OSS 就是把這一塊收進同一個模組。

業務程式碼只做兩件事——上傳一個檔案，拿到一個存取網址。檔案最後落在 MinIO、阿里雲還是本機磁碟，由設定決定，業務程式碼不用改。

它解決三個問題：

- **不被廠商綁住**：一套介面串接多家物件儲存，換雲端廠商、或從公有雲搬到自建環境，只要改設定。
- **檔案有序可管**：依業務線與檔案類型自動歸檔，不同功能的檔案不會混在一起。
- **大檔案傳得穩**：大檔案分片上傳，斷線可以續傳，已存過的檔案不重複傳。

典型情境：業務系統的附件、圖片與影片上傳；素材庫與檔案中心；需要多種儲存後端、或未來可能更換雲端廠商的專案；自建與公有雲混合部署；多條業務線共用一套儲存服務。

## 主要特點

- **儲存平台可插拔**：MinIO、阿里雲 OSS、騰訊雲 COS、華為雲 OBS、七牛雲、百度雲 BOS、Amazon S3（含相容協定）、本機磁碟，改設定就切換，業務零改動。
- **介面統一**：直接上傳與分片上傳共用同一套設定與呼叫習慣。
- **大檔案友善**：分片上傳避開單次請求的容量與逾時限制，支援斷線續傳、狀態查詢與取消上傳。
- **相同檔案免重傳**：檔案已存在時直接回傳既有網址，不再重複傳輸。
- **圖片自動處理**：上傳時依設定壓縮、產生縮圖，呼叫端不必另外處理圖片規格。
- **依業務分類儲存**：帶上業務識別即自動歸檔到對應目錄。
- **支援叢集部署**：上傳狀態集中存放，多台實例下仍可跨節點續傳。
- **開箱即用**：沿用 EasyFK 的回應、例外、日誌與設定風格，所有介面自帶 OpenAPI 描述。

## 支援的儲存平台

| 平台 | 設定範例 |
|------|----------|
| MinIO | `config/application-minio.yml` |
| 阿里雲 OSS | `config/application-aliyun.yml` |
| 騰訊雲 COS | `config/application-tencent.yml` |
| 華為雲 OBS | `config/application-huawei.yml` |
| 七牛雲 | `config/application-qiniu.yml` |
| 百度雲 BOS | `config/application-baidu.yml` |
| Amazon S3 及相容協定 | `config/application-amazon.yml` |
| 本機磁碟 | `config/application-local.yml` |

## 環境需求

- Java 21
- Spring Boot 3.5.x
- Redis —— 只有分片上傳需要（存放上傳狀態與免重傳紀錄）

## 文件

相依引入、平台設定、介面說明、前端串接範例與常見問題，請見開發手冊：

**[docs/oss开发手册.md](docs/oss开发手册.md)**（簡體中文）

## 參與貢獻

歡迎提出 Issue 與 Pull Request。除了小幅修正，建議先開 Issue 討論做法，再開始寫程式。

## 授權條款

本專案以 [Apache License 2.0](LICENSE) 授權釋出。

## 相關連結

- 易構生態：<https://www.easybuild.pro/>
- EasyFK 核心底座：<https://www.easybuild.pro/easyfk>
- 組織首頁：<https://github.com/easybuild-jack>
