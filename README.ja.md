# EasyFK OSS

<p align="center">
  <b>EasyBuild エコシステムの統合ファイルストレージサービス</b><br/>
  1 つのアップロード API であらゆるオブジェクトストレージに接続 — 保存先の切り替えは設定だけで、コードは変えません。
</p>

<p align="center">
  <a href="https://www.easybuild.pro/"><img src="https://img.shields.io/badge/%E3%82%A8%E3%82%B3%E3%82%B7%E3%82%B9%E3%83%86%E3%83%A0-EasyBuild-blueviolet?style=flat-square" alt="EasyBuild"/></a>
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?style=flat-square" alt="Spring Boot 3.5.x"/>
  <img src="https://img.shields.io/badge/%E3%83%90%E3%83%BC%E3%82%B8%E3%83%A7%E3%83%B3-3.5.16-lightgrey?style=flat-square" alt="3.5.16"/>
  <img src="https://img.shields.io/badge/%E3%83%A9%E3%82%A4%E3%82%BB%E3%83%B3%E3%82%B9-Apache--2.0-green?style=flat-square" alt="Apache-2.0"/>
</p>

<p align="center">
  <a href="README.md">English</a> &nbsp;·&nbsp; <a href="README.zh-CN.md">简体中文</a> &nbsp;·&nbsp; <a href="README.zh-TW.md">繁體中文</a> &nbsp;·&nbsp; <b>日本語</b>
</p>

---

> **エコシステムにおける位置づけ**
>
> EasyFK OSS は **EasyBuild（易构）エコシステム** を構成するコンポーネントの 1 つです。コンパイルおよび実行に必要な基盤機能・規約・バージョン管理は、すべてこのエコシステムから提供されます。統一レスポンスモデルと共通ユーティリティは `easyfk-core`、キャッシュと状態保存は `cache-redis`、本番向け Web 設定は `web-prd`、依存バージョンは `easyfk-dependencies` の BOM で管理されています。エコシステムの外で単独利用すると、これらの基盤が不足します。
>
> エコシステム全体の紹介、中核基盤である EasyFK、関連コンポーネント、オープンソースの業務モジュールについては **[https://www.easybuild.pro/](https://www.easybuild.pro/)** をご覧ください。

## 概要

業務システムでとかく作り直しになりがちで、つまずきやすい領域 — ファイルを実際どこに置くのか、後から保存先を変えたいときどうするのか、大きなファイルを途切れさせずに送るにはどうするのか。EasyFK OSS は、その部分を 1 つのモジュールにまとめたものです。

アプリケーション側がやることは 2 つだけ。ファイルをアップロードし、URL を受け取る。そのデータが MinIO に置かれるのか、Alibaba Cloud OSS に置かれるのか、ローカルディスクに置かれるのかは設定次第で、コードを書き換える必要はありません。

解決するのは次の 3 点です。

- **ベンダーロックインしない** — 1 つの API で複数のオブジェクトストレージに対応。クラウド事業者の変更や、パブリッククラウドからオンプレミスへの移行は設定変更だけで済みます。
- **ファイルが散らからない** — 業務ラインとファイル種別ごとに自動で振り分けて保存するため、機能ごとのファイルが混ざりません。
- **大きなファイルも安定** — 大容量ファイルはチャンク分割でアップロードし、通信が切れても再開でき、保存済みのファイルは再送しません。

想定される用途: 業務システムの添付ファイル・画像・動画のアップロード、素材ライブラリやファイルセンター、複数のストレージバックエンドを要する案件や将来クラウド事業者を変更する可能性がある案件、オンプレミスとパブリッククラウドの混在環境、複数の業務ラインで 1 つのストレージサービスを共有する構成。

## 主な特長

- **ストレージの差し替えが可能** — MinIO、Alibaba Cloud OSS、Tencent Cloud COS、Huawei Cloud OBS、Qiniu、Baidu BOS、Amazon S3（S3 互換サービスを含む）、ローカルディスク。設定変更だけで切り替わり、業務コードはそのままです。
- **API が統一されている** — 通常アップロードとチャンクアップロードが同じ設定・同じ呼び出し方を共有します。
- **大容量ファイルに強い** — チャンク分割により 1 リクエストあたりのサイズ制限やタイムアウトを回避し、再開・状態照会・中断に対応します。
- **重複排除** — 同じファイルが既にあれば、再送せずに既存の URL を返します。
- **画像処理を自動化** — 設定に応じてアップロード時に圧縮とサムネイル生成を行い、呼び出し側で画像仕様を気にする必要がありません。
- **業務単位で分類保存** — 業務名を渡すだけで、対応するディレクトリに自動で格納されます。
- **クラスタ構成に対応** — アップロード状態を集中管理するため、複数インスタンス構成でもノードをまたいで再開できます。
- **すぐに導入できる** — EasyFK のレスポンス・例外・ログ・設定の流儀に沿っており、すべてのエンドポイントに OpenAPI の記述があります。

## 対応ストレージ

| ストレージ | 設定サンプル |
|------------|--------------|
| MinIO | `config/application-minio.yml` |
| Alibaba Cloud OSS | `config/application-aliyun.yml` |
| Tencent Cloud COS | `config/application-tencent.yml` |
| Huawei Cloud OBS | `config/application-huawei.yml` |
| Qiniu | `config/application-qiniu.yml` |
| Baidu BOS | `config/application-baidu.yml` |
| Amazon S3 / S3 互換 | `config/application-amazon.yml` |
| ローカルディスク | `config/application-local.yml` |

## 動作環境

- Java 21
- Spring Boot 3.5.x
- Redis — チャンクアップロードを使う場合のみ必要（アップロード状態と重複排除の記録に使用）

## ドキュメント

依存関係の追加、ストレージの設定、エンドポイント仕様、フロントエンド実装例、FAQ は開発ガイドにまとめています。

**[docs/oss开发手册.md](docs/oss开发手册.md)**（中国語）

## コントリビュート

Issue と Pull Request を歓迎します。小さな修正以外は、実装前に Issue で方針を相談していただけると助かります。

## ライセンス

[Apache License 2.0](LICENSE) の下で公開しています。

## リンク

- EasyBuild エコシステム: <https://www.easybuild.pro/>
- EasyFK 中核基盤: <https://www.easybuild.pro/easyfk>
- 組織ページ: <https://github.com/easybuild-jack>
