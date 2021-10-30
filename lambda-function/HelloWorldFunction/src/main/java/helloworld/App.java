package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import fi.solita.clamav.ClamAVClient;

import java.io.FileInputStream;
import java.io.IOException;


public class App implements RequestHandler<S3Event, String> {


  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger logger = context.getLogger();
    logger.log("EVENT: " + s3Event.toString());
    S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);

    String srcBucket = record.getS3().getBucket().getName();
    String srcKey = record.getS3().getObject().getUrlDecodedKey();

    logger.log("srcBucket: " + srcBucket);
    logger.log("srcKey: " + srcKey);

    S3Service s3Service = new S3Service();

    byte[] file = s3Service.getObjectBytes(srcBucket, srcKey);

    ClamAVClient cl = new ClamAVClient("18.220.115.195", 3310);
    try {
      byte[] reply = cl.scan(file);
      if (ClamAVClient.isCleanReply(reply)) {
        System.out.println("clean");
        s3Service.tagExistingObject(srcBucket, srcKey, "status", "SAFE");
      } else {
        System.out.println("unclean");
        s3Service.tagExistingObject(srcBucket, srcKey, "status", "VIRUS DETECTED");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return srcKey;
  }


}
