# Amazon Price Checker (Step Functions + Ktor Refactor)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.8.21-yellowgreen?logo=kotlin)](https://kotlinlang.org/)
[![Ktor](https://img.shields.io/badge/Ktor-2.3.0-blue)](https://ktor.io/)
[![AWS](https://img.shields.io/badge/AWS-Step%20Functions-orange?logo=aws)](https://aws.amazon.com/step-functions/)
[![Terraform](https://img.shields.io/badge/Terraform-1.0%2B-blueviolet?logo=terraform)](https://www.terraform.io/)

Amazon Price Checker は、Amazon の商品価格を監視し、価格変動を通知するアプリケーションです。
このプロジェクトは、オリジナルのモノリシックなLambdaアプリケーションを、AWS Step Functionsを中心としたマイクロサービスアーキテクチャにリファクタリングしたものです。

## アーキテクチャ概要

- **オーケストレーション**: AWS Step Functions
- **Lambdaランタイム**: Kotlin/JVM (Ktor + Koin) on Container Image
- **インフラ管理**: Terraform
- **データベース**: Amazon DynamoDB
- **ローカル開発環境**: Docker Compose (DynamoDB Local)

```
┌───────────────────────────┐
│ EventBridge (Daily Trigger) │
└────────────┬──────────────┘
             │
             ▼
┌───────────────────────────┐
│ Step Functions State Machine │
└────────────┬──────────────┘
             │
 ┌───────────┴───────────┐
 │      Parallel State     │
 │  ┌──────────────────┐   │
 │  │ FetchPrice Lambda│   │
 │  └──────────────────┘   │
 │  ┌──────────────────┐   │
 │  │GetPriceHistory L.│   │
 │  └──────────────────┘   │
 └───────────┬───────────┘
             │
             ▼
┌───────────────────────────┐
│ComparePriceAndNotify L.   │
└───────────────────────────┘
```

## セットアップ (ローカル開発)

1.  **Dockerの起動**
    Dockerがインストールされていることを確認し、Dockerデーモンを起動します。

2.  **DynamoDB Localの起動**
    プロジェクトルートで以下のコマンドを実行し、アプリケーションとDynamoDB Localを起動します。
    ```bash
    docker-compose up --build
    ```
    これにより、`app`ディレクトリ内のコードがコンテナ化され、`http://localhost:8080`でアクセス可能になります（Lambdaとしてではなく、Ktorサーバーとして）。

## デプロイ手順

このプロジェクトはTerraformで管理されています。デプロイにはAWSの認証情報と、適切なIAMロールのARNが必要です。

1.  **Terraformの初期化**
    ```bash
    cd price-checker-refactor/terraform
    terraform init
    ```

2.  **Terraformの適用**
    Lambda関数にアタッチするIAMロールのARNを環境変数などに設定した上で、`terraform apply`を実行します。
    ```bash
    export TF_VAR_iam_role_arn="arn:aws:iam::123456789012:role/your-lambda-role"
    terraform apply
    ```

3.  **コンテナイメージのビルドとプッシュ**
    CI/CDパイプライン（例: GitHub Actions）をセットアップし、各Lambdaのコンテナイメージをビルドして、Terraformが作成したECRリポジトリにプッシュします。

## テスト

ユニットテストを実行するには、以下のコマンドを使用します。
```bash
cd price-checker-refactor/app
./gradlew test
```
