package helloworld;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class App implements RequestHandler<S3Event, String> {

  Connection databaseConnection;

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger log = context.getLogger();
    S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
    log.log("S3 event: " + s3Event.toString());
    String recordBucketS3 = record.getS3().getBucket().getName();
    String keyS3 = record.getS3().getObject().getUrlDecodedKey();
    log.log("keyS3: " + keyS3);
    log.log("bucket: " + recordBucketS3);
    String url = "https://encrypt-application.s3.us-east-2.amazonaws.com/";
    List<String> urlSegments = Arrays.asList(keyS3.split("/"));
    if (!urlSegments.isEmpty()) {

      if (keyS3.contains("encrypts/") && keyS3.contains(".encrypt")) {

        try {
          String receiverEmails = urlSegments.get(0);
          String encryptFileName = keyS3.replace("@", "%40");
          String emailBody = "<h2>Cloud Security Assignment</h2>"
              + "<p> File encrypted successfully. </p>" +
              "<p> Download file here : " + url + encryptFileName + "</p>" +
              "<p> Download key here : " + url + encryptFileName.replace(".encrypt", ".metadata") + "</p>";

          final String sender = "ajay.gahlot.08@gmail.com";
          final String emailSubject = "File Encrypted Successfully";
          final String textBody = "This email was sent via Amazon SES "
              + "using the AWS SDK for Java processed from AWS Lambda.";

          AmazonSimpleEmailService emailClient =
              AmazonSimpleEmailServiceClientBuilder.standard()
                  .withRegion(Regions.US_EAST_2).build();
          java.lang.String contentType = "UTF-8";
          SendEmailRequest requestData = new SendEmailRequest()
              .withDestination(
                  new Destination().withToAddresses(receiverEmails))
              .withMessage(new Message()
                  .withBody(new Body()
                      .withHtml(new Content()
                          .withCharset(contentType).withData(emailBody))
                      .withText(new Content()
                          .withCharset(contentType).withData(textBody)))
                  .withSubject(new Content()
                      .withCharset(contentType).withData(emailSubject)))
              .withSource(sender);

          emailClient.sendEmail(requestData);
          log.log("Email processed successfully!");
        } catch (Exception ex) {
          log.log("The email was not sent. Error message: "
              + ex.getMessage());
        }
      } else {
        log.log("Not Found: " + keyS3);
      }
    } else {
      log.log("Email not found , file url invalid");

    }

    return keyS3;
  }

  private void handleException(LambdaLogger log, SQLException ex) {
    log.log("error: " + ex.getMessage());
    log.log("errorstate: " + ex.getSQLState());
    log.log("errorcode: " + ex.getErrorCode());
  }
}

