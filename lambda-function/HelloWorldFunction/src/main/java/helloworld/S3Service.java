package helloworld;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.services.s3.model.Tagging;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectTaggingRequest;

public class S3Service {

  private S3Client getClient() {

    Region region = Region.US_WEST_2;
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

      // Return the byte[] from this object.
      ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
      return objectBytes.asByteArray();

    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return null;
  }

  // Returns the names of all images in the given bucket.
  public List<String> listBucketObjects(String bucketName) {

    S3Client s3 = getClient();
    String keyName;

    List<String> keys = new ArrayList<>();

    try {
      ListObjectsRequest listObjects = ListObjectsRequest
          .builder()
          .bucket(bucketName)
          .build();

      ListObjectsResponse res = s3.listObjects(listObjects);
      List<S3Object> objects = res.contents();

      for (S3Object myValue: objects) {
        keyName = myValue.key();
        keys.add(keyName);
      }
      return keys;

    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    return null;
  }

  // This method tags an existing object.
  public void tagExistingObject( String bucketName, String key, String label, String labelValue) {

    try {
      S3Client s3 = getClient();
      // First need to get existing tag set; otherwise the existing tags are overwritten.
      GetObjectTaggingRequest getObjectTaggingRequest = GetObjectTaggingRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      GetObjectTaggingResponse response = s3.getObjectTagging(getObjectTaggingRequest);

      // Get the existing immutable list - cannot modify this list.
      List<Tag> existingList = response.tagSet();
      ArrayList<Tag> newTagList = new ArrayList(new ArrayList<>(existingList));

      // Create a new tag.
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

  //Delete tags from the given object.
  public void deleteTagFromObject(String bucketName, String key) {

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