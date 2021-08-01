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
import java.util.Optional;


public class App implements RequestHandler<S3Event, String> {

  Connection conn;

  static final String FROM = "";

  static final String TO = "";

  static final String CONFIGSET = "ConfigSet";

  static final String SUBJECT = "L";

  static final String TEXTBODY = "This email was sent through Amazon SES "
      + "using the AWS SDK for Java.";

  @Override
  public String handleRequest(S3Event s3Event, Context context) {

    LambdaLogger logger = context.getLogger();
    logger.log("EVENT: " + s3Event.toString());
    S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);

    String srcBucket = record.getS3().getBucket().getName();
    String srcKey = record.getS3().getObject().getUrlDecodedKey();

    logger.log("srcBucket: " + srcBucket);
    logger.log("srcKey: " + srcKey);

    Optional<String> emails = Optional.ofNullable(fetchEmails(logger, srcKey));
    if (emails.isPresent()) {
      logger.log("emails" + emails.get() + " found for srcKey: " + srcKey);
      String bucketUrl = "";
      String HTMLBODY = "<h1> ASSIGNMENT</h1>"
          + "<p>Download your file from here : <a href='" + bucketUrl +srcKey+"'>"
          + bucketUrl +srcKey+"</a>";
      try {
        AmazonSimpleEmailService client =
            AmazonSimpleEmailServiceClientBuilder.standard()
                // Replace US_WEST_2 with the AWS Region you're using for
                // Amazon SES.
                .withRegion(Regions.AP_SOUTH_1).build();
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(TO))
            .withMessage(new Message()
                .withBody(new Body()
                    .withHtml(new Content()
                        .withCharset("UTF-8").withData(HTMLBODY))
                    .withText(new Content()
                        .withCharset("UTF-8").withData(TEXTBODY)))
                .withSubject(new Content()
                    .withCharset("UTF-8").withData(SUBJECT)))
            .withSource(FROM);
//            // Comment or remove the next line if you are not using a
//            // configuration set
//            .withConfigurationSetName(CONFIGSET);
        client.sendEmail(request);
        logger.log("Email sent!");
      } catch (Exception ex) {
        logger.log("The email was not sent. Error message: "
            + ex.getMessage());
      }
    } else {
      logger.log("emails not found for srcKey: " + srcKey);
    }

    return srcKey;
  }

  private String fetchEmails(LambdaLogger logger, String srcKey) {
    try {
      conn = getRemoteConnection();
      if (conn != null) {
        try (Statement readStatement = conn.createStatement()) {
          ResultSet resultSet = readStatement.executeQuery("SELECT * FROM upload where file_name ='" + srcKey + "';");
          if (resultSet.next()) {
            String emails = resultSet.getString("emails");
            logger.log("emails: " + emails);
            return emails;
          }
          resultSet.close();
        }
        conn.close();
      }
    } catch (SQLException ex) {
      // Handle any errors
      logger.log("SQLException: " + ex.getMessage());
      logger.log("SQLState: " + ex.getSQLState());
      logger.log("VendorError: " + ex.getErrorCode());
    } finally {
      System.out.println("Closing the connection.");
      if (conn != null) try {
        conn.close();
      } catch (SQLException ignore) {
        logger.log("error: " + ignore.getMessage());
      }
    }
    return null;
  }

  private static Connection getRemoteConnection() {
    try {
      String dbName = "";
      String userName = "";
      String password = "";
      String hostname = "";
      String port = "";
      String jdbcUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
      return DriverManager.getConnection(jdbcUrl);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
