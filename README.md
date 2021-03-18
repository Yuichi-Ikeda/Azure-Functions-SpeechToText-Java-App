# 日本語音声ファイルから文字起こしをする Java アプリケーション

## 概要
　Azure Blob ストレージの audio コンテナに音声ファイルを配置すると、それをトリガーとして起動する Azure Functions Java アプリケーション。音声ファイルを Cognitive Speech Service に渡し、 得られた日本語の音声テキストを text コンテナに配置します。

 <img src="/images/workflow.png" title="workflow">

### 実行環境
- Azure Functions : Windows ベース環境
- Java 11

※ ビルド、デプロイ後に Azure Functions の構成メニュー、アプリケーション設定で環境変数として CognitiveServiceApiKey の登録が必要です

### 開発環境
- Visual Studio Code
- Zulu JDK11
- Apache Maven

## 開発環境の準備

### [クイックスタート: Visual Studio Code を使用して Azure に Java 関数を作成する](https://docs.microsoft.com/ja-jp/azure/azure-functions/create-first-function-vs-code-java)

Windows 10 上に開発環境を準備する場合、環境変数の登録内容

1. Zulu JDK11 のインストール

    %JAVA_HOME% = C:\Program Files\Zulu\zulu-11

    %PATH% へ %JAVA_HOME% を追加

2. Apache Maven の インストール

    %PATH% へ C:\Program Files\apache-maven-3.6.3\bin を追加

3. ローカルデバッグ用の設定ファイル

```json:settings.json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "<Azure Blob 接続文字列>",
    "CognitiveServiceApiKey": "<Cognitive.SpeechService API キー>",
    "FUNCTIONS_WORKER_RUNTIME": "java"
  },
  "ConnectionStrings": {}
}
```
