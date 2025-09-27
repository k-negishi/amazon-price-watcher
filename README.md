# AmazonPriceWatcher

![kotlin](https://img.shields.io/badge/Kotlin-2.2.20-yellowgreen?logo=kotlin&)
![spring](https://img.shields.io/badge/Spring-3.5.6-green?logo=spring)
![aws](https://img.shields.io/badge/AWS-Lambda-blue?logo=aws)

AmazonPriceWatcher は Amazon 商品価格を日次で取得し、前日より値下がりしている商品があれば 1 通の LINE 通知を送信する AWS Lambda アプリケーションです。Spring Cloud Function を利用し、DynamoDB に価格履歴を保存します。

## アーキテクチャ概要
- AWS Lambda (Spring Cloud Function, Kotlin/JVM21)
- Coroutine ベースの非同期処理
- スクレイピング: OkHttp + Jsoup
- EventBridge で日次トリガー
- DynamoDB (TTL 7 日)
- LINE Messaging API で通知
- SAM による IaC
- Testcontainers (LocalStack + WireMock) による E2E テスト

## セットアップ
1. Docker を起動し DynamoDB Local を用意します。
   ```bash
   docker compose -f docker/docker-compose.local.yml up -d
   docker/scripts/local-init.sh
   ```
2. アプリケーションをローカルで起動します。
   ```bash
   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   ```
   `LineNotifierStub` により通知内容はログへ出力されます。

## テスト & カバレッジ
```bash
./gradlew unitTest e2eTest koverXmlReport
```
- `unitTest`: UseCase 層のユニットテスト (Kotest + MockK)
- `e2eTest`: Testcontainers (LocalStack + WireMock) による E2E テスト
- `koverXmlReport`: `app/build/reports/kover/coverage.xml` にレポート出力

## CI/CD
GitHub Actions による自動テスト・自動デプロイを行う。
- `test-and-coverage` ジョブでテスト + カバレッジ測定 + Coverage バッジ更新
- `deploy` ジョブで OIDC 経由の `sam build` / `sam deploy`

## デプロイ手順（ローカル確認）
```bash
./gradlew bootJar
sam build --use-container
sam deploy --guided
```

