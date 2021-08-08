package lambda;

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


public class LambdaFunction implements RequestHandler<S3Event, String> {

  Connection dbConnection;

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger logSendEmail = context.getLogger();
    S3EventNotification.S3EventNotificationRecord s3bucketdata = s3Event.getRecords().get(0);
    String srcBucket = s3bucketdata.getS3().getBucket().getName();
    String awsS3BucketKey = s3bucketdata.getS3().getObject().getUrlDecodedKey();
    Optional<String> dbData = Optional.ofNullable(getEmailsFromDatabaseRDS(logSendEmail, awsS3BucketKey));
    if (dbData.isPresent()) {
      String url = "https://myfiles-123.s3.us-east-2.amazonaws.com/";
      String body = "Download"
          + "<p>Click<a href='" + url +awsS3BucketKey+"'>"
          + url +awsS3BucketKey+"</a></p>";
      processAndSendEmail(logSendEmail, dbData, body);
    } else {
      logSendEmail.log("No dbData found " + awsS3BucketKey);
    }
    return awsS3BucketKey;
  }

  private void processAndSendEmail(LambdaLogger logSendEmail, Optional<String> data, String bodyEmail) {
    try {
      AmazonSimpleEmailService client =
          AmazonSimpleEmailServiceClientBuilder.standard()
              .withRegion(Regions.US_EAST_2).build();
      SendEmailRequest request = new SendEmailRequest()
          .withDestination(
              new Destination().withToAddresses(data.get().split(",")))
          .withMessage(new Message()
              .withBody(new Body()
                  .withHtml(new Content()
                      .withCharset("UTF-8").withData(bodyEmail))
                  .withText(new Content()
                      .withCharset("UTF-8").withData("Download file from below link")))
              .withSubject(new Content()
                  .withCharset("UTF-8").withData("Download File from S3")))
          .withSource("sreelekhakosaraju@gmail.com");

      client.sendEmail(request);
      logSendEmail.log("Email sent successfylly!");
    } catch (Exception ex) {
      logSendEmail.log("The email was not sent. Error message: "
          + ex.getMessage());
    }
  }

  private String getEmailsFromDatabaseRDS(LambdaLogger log, String srcKey) {
    try {
      dbConnection = DriverManager.getConnection("jdbc:postgresql://" + "database-1.cogxg378u5fd.us-east-2.rds.amazonaws.com" + ":" + "5432" + "/" + "uploadfiledetail_db" + "?user=" + "postgres" + "&password=" + "postgres");
      if (dbConnection != null) {
        try (Statement readStatement = dbConnection.createStatement()) {
          ResultSet resultSet = readStatement.executeQuery("SELECT * FROM upload_file_detail where name ='" + srcKey + "';");
          if (resultSet.next()) {
            return resultSet.getString("emails");
          }
          resultSet.close();
        }
        dbConnection.close();
      }
    } catch (SQLException ex) {
      log.log("error: " + ex.getMessage());
    } finally {
      if (dbConnection != null) try {
        dbConnection.close();
      } catch (SQLException ignore) {
        log.log("error: " + ignore.getMessage());
      }
    }
    return null;
  }

}
