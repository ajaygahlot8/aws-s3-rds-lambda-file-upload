package sendemails;

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


public class SendEmail implements RequestHandler<S3Event, String> {

  Connection conn;

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger log = context.getLogger();
    S3EventNotification.S3EventNotificationRecord s3data = s3Event.getRecords().get(0);
    String srcBucket = s3data.getS3().getBucket().getName();
    String key = s3data.getS3().getObject().getUrlDecodedKey();
    Optional<String> data = Optional.ofNullable(findReceiverEmails(log, key));
    if (data.isPresent()) {
      String url = "https://upload-12345-zxs.s3.us-east-2.amazonaws.com/";
      String HTMLBODY = "<h1>Download email triggered from lambda</h1>"
          + "<p>Click here <a href='" + url + key + "'>"
          + url + key + "</a>";
      sendEmail(log, data, HTMLBODY);
    } else {
      log.log("No data " + key);
    }
    return key;
  }

  private void sendEmail(LambdaLogger log, Optional<String> data, String HTMLBODY) {
    try {

      String SENDER = "Raasianumukonda1@gmail.com";
      final String EMAIL_SUBJECT = "Assignment Email Alert";
      final String TEXTBODY = "Email was sent using AWS Lambda";

      AmazonSimpleEmailService client =
          AmazonSimpleEmailServiceClientBuilder.standard()
              .withRegion(Regions.US_EAST_2).build();
      SendEmailRequest request = new SendEmailRequest()
          .withDestination(
              new Destination().withToAddresses(data.get().split(",")))
          .withMessage(new Message()
              .withBody(new Body()
                  .withHtml(new Content()
                      .withCharset("UTF-8").withData(HTMLBODY))
                  .withText(new Content()
                      .withCharset("UTF-8").withData(TEXTBODY)))
              .withSubject(new Content()
                  .withCharset("UTF-8").withData(EMAIL_SUBJECT)))
          .withSource(SENDER);

      client.sendEmail(request);
      log.log("Email sent!");
    } catch (Exception ex) {
      log.log("The email was not sent. Error message: "
          + ex.getMessage());
    }
  }

  private String findReceiverEmails(LambdaLogger log, String srcKey) {
    try {
      conn = getRemoteConnection();
      if (conn != null) {
        try (Statement readStatement = conn.createStatement()) {
          String query = "SELECT * FROM file_detail where file_name ='" + srcKey + "';";
          ResultSet resultSet = readStatement.executeQuery(query);
          if (resultSet.next()) {
            String emails = resultSet.getString("emails");
            log.log("emails: " + emails);
            return emails;
          }
          resultSet.close();
        }
        conn.close();
      }
    } catch (SQLException ex) {
      log.log("exception: " + ex.getMessage());
    } finally {
      if (conn != null) try {
        conn.close();
      } catch (SQLException ignore) {
        log.log("error: " + ignore.getMessage());
      }
    }
    return null;
  }

  private static Connection getRemoteConnection() {
    try {

      String dbName = "filedetail_db";
      String user = "postgres";
      String password = "postgres";
      String host = "db-1.cxqg6q2lngmq.us-east-2.rds.amazonaws.com";
      String port = "5432";
      String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?user=" + user + "&password=" + password;
      return DriverManager.getConnection(jdbcUrl);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
