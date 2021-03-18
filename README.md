## 日本語音声ファイルをトリガーに文字起こしをする Java アプリケーション

## 概要
　Azure Blob ストレージの audio コンテナに音声ファイルを配置すると、それをトリガーとして起動する Azure Functions Java アプリケーション。音声ファイルを Cognitive Speech Service に渡し、 得られた日本語の音声テキストを text コンテナに配置します。

### 実行環境
- Azure App Service Windows ベース環境
- Zulu JDK11

### 開発環境
- Visual Studio Code
- JDK 11

クイックスタート: Visual Studio Code を使用して Azure に Java 関数を作成する
https://docs.microsoft.com/ja-jp/azure/azure-functions/create-first-function-vs-code-java


Zulu JDK11 インストール

%JAVA_HOME%
C:\Program Files\Zulu\zulu-11

%PATH%
%JAVA_HOME%

Apache Maven インストール
%PATH%
C:\Program Files\apache-maven-3.6.3\bin

Blob ストレージの依存関係 pom.xml

    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-storage-blob</artifactId>
        <version>12.10.0</version>
    </dependency>

Speech SDK JAVA 開発環境を設定する
https://docs.microsoft.com/ja-jp/azure/cognitive-services/speech-service/quickstarts/setup-platform?tabs=dotnet%2Cwindows%2Cjre%2Cbrowser&pivots=programming-language-java
