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

## セットアップ
1. ローカル用の環境変数ファイルを用意します。
   ```bash
   cp .env.local.example .env.local
   cp amazon-items.json.example amazon-items.json
   ```
2. `.env.local` と `amazon-items.json` を編集し、実際の値を設定します。

3. アプリケーションをローカルで起動します。
   ```bash
   set -a
   . ./.env.local
   set +a
   export RUN_ON_STARTUP=true
   export SPRING_MAIN_WEB_APPLICATION_TYPE=none
   ./gradlew bootRun
   ```

## ローカル実行（Docker）
```bash
docker compose -f docker/local/docker-compose.yml up --build
```
- `.env.local` の `AMAZON_ITEMS` は JSON 文字列として読み込まれます。
- `.env.local` と `~/.aws` を利用して実AWS/実Amazonへ接続します。

## テスト & カバレッジ
```bash
./gradlew unitTest koverXmlReport
```
- `unitTest`: UseCase 層のユニットテスト (Kotest + MockK)
- `koverXmlReport`: `app/build/reports/kover/coverage.xml` にレポート出力

## CI/CD
GitHub Actions による自動テスト・自動デプロイを行う。
- `test-and-coverage` ジョブでテスト + カバレッジ測定 + Coverage バッジ更新
- `deploy` ジョブで OIDC 経由の `sam build` / `sam deploy`

## デプロイ手順（ローカル確認）
```bash
./gradlew bootJar
sam build --use-container --template-file infra/sam/template.yaml
sam deploy --template-file infra/sam/template.yaml
```

## SSM への .env.prod 反映
```bash
# 必要に応じて .env.deploy に AWS_REGION などを定義
cp .env.deploy.example .env.deploy
./infra/scripts/ssm-upload.sh
```

## Lambda コンテナ（ECR）
```bash
docker build -f docker/lambda/Dockerfile -t <ecr-repo>:<tag> .
docker push <ecr-repo>:<tag>
```
