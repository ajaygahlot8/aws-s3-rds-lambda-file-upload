package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import fi.solita.clamav.ClamAVClient;

import java.io.IOException;

public class App implements RequestHandler<S3Event, String> {

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger logger = context.getLogger();
    logger.log("EVENT: " + s3Event.toString());
    S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);

    String bucketName = record.getS3().getBucket().getName();
    String fileName = record.getS3().getObject().getUrlDecodedKey();

    logger.log("bucketName: " + bucketName);
    logger.log("fileName: " + fileName);

    S3Service s3Service = new S3Service();

    byte[] file = s3Service.getObjectBytes(bucketName, fileName);

    ClamAVClient cl = new ClamAVClient("18.220.115.195", 3310);
    logger.log("connected to clam av server successfully");

    try {
      byte[] reply = cl.scan(file);
      if (ClamAVClient.isCleanReply(reply)) {
        logger.log("file scan complete :clean ");
        s3Service.deleteTag(bucketName, fileName);
        s3Service.createTag(bucketName, fileName, "status", "SAFE");
      } else {
        logger.log("file scan complete :virus detected ");
        s3Service.deleteTag(bucketName, fileName);
        s3Service.createTag(bucketName, fileName, "status", "VIRUS DETECTED");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileName;
  }
}
