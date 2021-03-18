package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.BlobTrigger;
import com.microsoft.azure.functions.annotation.BindingName;

import com.azure.storage.blob.*;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.io.*;

/**
 * Azure Functions with Blob Trigger.
 */
public class Function 
{
    /** 
     * .wav ファイルが audio コンテナにアップロードされると実行
     * https://docs.microsoft.com/ja-jp/azure/storage/blobs/storage-quickstart-blobs-java
    */
    @FunctionName("audio2text")
    public void run(@BlobTrigger(name = "file", dataType = "binary", path = "audio/{name}.wav", 
                    connection = "AzureWebJobsStorage") byte[] content,
                    @BindingName("name") String filename, final ExecutionContext context) 
    {
      context.getLogger().info("Name: " + filename + " Size: " + content.length + " bytes");

      // 環境変数から値を取得
      String tempfile = System.getenv("TMP")+"\\" + filename;
      String connectStr = System.getenv("AzureWebJobsStorage");

      // Step 1. Blob から audio ファイルをダウンロード
      BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
      BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("audio");
      BlobClient blobClient = containerClient.getBlobClient(filename + ".wav");
      blobClient.downloadToFile(tempfile + ".wav", true);

      // Step 2. audio ファイルから文字起こし
      audio2Text(tempfile, context);

      // Step 3. 抽出したテキストファイルを Blob へ格納
      containerClient = blobServiceClient.getBlobContainerClient("text");
      blobClient = containerClient.getBlobClient(filename + ".txt");
      blobClient.uploadFromFile(tempfile + ".txt", true);

      // 一時ファイルの削除
      File file = new File(tempfile + ".txt");
      file.delete();
    }
    
    // イベント同期用
    private Semaphore syncSemaphore;
    private OutputStreamWriter filewriter;

    /**
     * SpeechService で audio ファイルから文字起こし
     * https://docs.microsoft.com/ja-jp/azure/cognitive-services/speech-service/get-started-speech-to-text
     */
    private void audio2Text(String tempfile, ExecutionContext context) 
    {
      // First initialize the semaphore.
      syncSemaphore = new Semaphore(0);

      try {
        filewriter = new OutputStreamWriter(new FileOutputStream(tempfile + ".txt"), "UTF-8");
      }catch(IOException ioex){
        context.getLogger().warning(ioex.getMessage());
      }
      
      String key = System.getenv("CognitiveServiceApiKey");
      SpeechConfig speechConfig = SpeechConfig.fromSubscription(key, "japaneast");
      speechConfig.setSpeechRecognitionLanguage("ja-JP");

      AudioConfig audioConfig = AudioConfig.fromWavFileInput(tempfile + ".wav");
      SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);
      
      recognizer.recognized.addEventListener((s, e) -> {
          if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
              String transcript = e.getResult().getText();
              context.getLogger().info("RECOGNIZED: Text=" + transcript);
              try {
              filewriter.write(transcript);
              }catch(IOException ioex){
                context.getLogger().warning(ioex.getMessage());
              }
          }
          else if (e.getResult().getReason() == ResultReason.NoMatch) {
              context.getLogger().warning("NOMATCH: Speech could not be recognized.");
          }
      });

      recognizer.canceled.addEventListener((s, e) -> {
          context.getLogger().info("CANCELED: Reason=" + e.getReason());

          if (e.getReason() == CancellationReason.Error) {
              context.getLogger().warning("CANCELED: ErrorCode=" + e.getErrorCode());
              context.getLogger().warning("CANCELED: ErrorDetails=" + e.getErrorDetails());
              context.getLogger().warning("CANCELED: Did you update the subscription info?");
          }

          syncSemaphore.release();
      });

      recognizer.sessionStopped.addEventListener((s, e) -> {
          context.getLogger().info("\n    Session stopped event.");
          try{
            filewriter.close();
          }catch(IOException ioex){
            context.getLogger().warning(ioex.getMessage());
          }
          syncSemaphore.release();
          recognizer.close();

          // 一時ファイルの削除
          File file = new File(tempfile + ".wav");
          file.delete();
      });

      try {
        // Starts continuous recognition.
        recognizer.startContinuousRecognitionAsync().get();
        // Waits for completion.
        syncSemaphore.acquire();
        // Stops recognition.
        recognizer.stopContinuousRecognitionAsync().get();
      }
      catch(ExecutionException e) {
        context.getLogger().warning(e.getMessage());
      }
      catch(InterruptedException e) {
        context.getLogger().warning(e.getMessage());
      }
    }
}
