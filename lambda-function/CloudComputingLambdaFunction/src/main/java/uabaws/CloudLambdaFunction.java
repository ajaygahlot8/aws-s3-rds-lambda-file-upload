package uabaws;

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
import java.util.Optional;


public class CloudLambdaFunction implements RequestHandler<S3Event, String> {

  Connection databaseConnection;

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger log = context.getLogger();
    S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);
    log.log("S3 event: " + s3Event.toString());
    String recordBucketS3 = record.getS3().getBucket().getName();
    String keyS3 = record.getS3().getObject().getUrlDecodedKey();
    log.log("bucket: " + recordBucketS3);
    Optional<String> receiverEmails = Optional.ofNullable(fetchEmails(log, keyS3));
    if (receiverEmails.isPresent()) {
      String url = "https://my-content-20-21.s3.us-east-2.amazonaws.com/";
      String emailBody = "<h2>Cloud Computing Assignment</h2>"
          + "<p>Download here : <a href='" + url +keyS3+"'>"
          + url +keyS3+"</a>";
      try {

         final String sender = "Abhi8399@gmail.com";
         final String emailSubject = "Download link for uploaded file";
         final String textBody = "This email was sent via Amazon SES "
            + "using the AWS SDK for Java processed from AWS Lambda.";

        AmazonSimpleEmailService emailClient =
            AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(Regions.US_EAST_2).build();
        java.lang.String contentType = "UTF-8";
        SendEmailRequest requestData = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(receiverEmails.get().split(",")))
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

    return keyS3;
  }

  private String fetchEmails(LambdaLogger log, String srcKey) {
    try {
      databaseConnection = DriverManager.getConnection("jdbc:postgresql://mydatabase1.cfxa8jgitzoa.us-east-2.rds.amazonaws.com:5432/file_upload_db?user=postgres&password=postgres");
      if (databaseConnection != null) {
        try (Statement readStatement = databaseConnection.createStatement()) {
          java.lang.String fetchFileUploadDataQuery = "SELECT * FROM file_upload where name_file ='" + srcKey + "';";
          ResultSet resultData = readStatement.executeQuery(fetchFileUploadDataQuery);
          if (resultData.next()) {
            return resultData.getString("emails");
          }
          resultData.close();
        }
        databaseConnection.close();
      }
    } catch (SQLException ex) {
      // Handle any errors
      handleException(log, ex);
    } finally {
      System.out.println("Connection close.");
      if (databaseConnection != null) try {
        databaseConnection.close();
      } catch (SQLException ignore) {
        log.log("error: " + ignore.getMessage());
      }
    }
    return null;
  }

  private void handleException(LambdaLogger log, SQLException ex) {
    log.log("error: " + ex.getMessage());
    log.log("errorstate: " + ex.getSQLState());
    log.log("errorcode: " + ex.getErrorCode());
  }
}
