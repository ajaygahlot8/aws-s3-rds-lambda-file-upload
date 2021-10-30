package helloworld;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;

public class S3Service {

  private S3Client getClient() {

    Region region = Region.US_EAST_2;
    return S3Client.builder()
        .region(region)
        .build();
  }

  public byte[] getObjectBytes(String bucketName, String keyName) {

    S3Client s3 = getClient();

    try {

      GetObjectRequest objectRequest = GetObjectRequest
          .builder()
          .key(keyName)
          .bucket(bucketName)
          .build();

      ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
      return objectBytes.asByteArray();

    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return null;
  }

  public void createTag(String bucketName, String key, String label, String labelValue) {

    try {
      S3Client s3 = getClient();
      GetObjectTaggingRequest getObjectTaggingRequest = GetObjectTaggingRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      GetObjectTaggingResponse response = s3.getObjectTagging(getObjectTaggingRequest);

      List<Tag> existingList = response.tagSet();
      ArrayList<Tag> newTagList = new ArrayList(new ArrayList<>(existingList));

      Tag myTag = Tag.builder()
          .key(label)
          .value(labelValue)
          .build();

      // push new tag to list.
      newTagList.add(myTag);
      Tagging tagging = Tagging.builder()
          .tagSet(newTagList)
          .build();

      PutObjectTaggingRequest taggingRequest = PutObjectTaggingRequest.builder()
          .key(key)
          .bucket(bucketName)
          .tagging(tagging)
          .build();

      s3.putObjectTagging(taggingRequest);
      System.out.println(key + " was tagged with " + label);

    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  public void deleteTag(String bucketName, String key) {

    try {

      DeleteObjectTaggingRequest deleteObjectTaggingRequest = DeleteObjectTaggingRequest.builder()
          .key(key)
          .bucket(bucketName)
          .build();

      S3Client s3 = getClient();
      s3.deleteObjectTagging(deleteObjectTaggingRequest);

    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }
}