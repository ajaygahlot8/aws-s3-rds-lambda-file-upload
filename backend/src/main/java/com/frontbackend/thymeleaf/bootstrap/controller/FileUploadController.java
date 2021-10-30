package com.frontbackend.thymeleaf.bootstrap.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import fi.solita.clamav.ClamAVClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/")
public class FileUploadController {

  private final AmazonS3 amazonS3Client;


  @Value("${app.awsServices.bucketName}")
  private String bucketName;

  public FileUploadController(AmazonS3 amazonS3Client) {
    this.amazonS3Client = amazonS3Client;
  }

  @GetMapping
  public String main(Model model) {
    Map<String, String> statusMap = new HashMap<String, String>();
    ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
    for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
      GetObjectTaggingRequest getTaggingRequest = new GetObjectTaggingRequest(bucketName, os.getKey());
      GetObjectTaggingResult tags = amazonS3Client.getObjectTagging(getTaggingRequest);
      Tag tag = tags.getTagSet().get(0);
      statusMap.put(os.getKey(), tag.getValue());
    }
    model.addAttribute("statusMap", statusMap);
    return "index";
  }

  @PostMapping
  public String upload(@RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) throws IOException {
    File file = convertMultiPartFileToFile(multipartFile);

    String uniqueFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

    uploadFileToS3bucket(uniqueFileName, file, bucketName);

    redirectAttributes.addFlashAttribute("message", "File uploaded successfully :  " + multipartFile.getOriginalFilename() + "!");
    return "redirect:/";
  }

  private void uploadFileToS3bucket(String fileName, File file, String bucketName) throws IOException {

    ClamAVClient cl = new ClamAVClient("18.220.115.195", 3310);
    byte[] reply = cl.scan(new FileInputStream(file));
    if (ClamAVClient.isCleanReply(reply)) {
      System.out.println("clean");
    }else {
      System.out.println("unclean");
    }
//    List<Tag> tags = new ArrayList<>();
//    tags.add(new Tag("status", "PROCESSING"));
//    ObjectTagging objectTag = new ObjectTagging(tags);
//    PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
//    request.withTagging(objectTag);
//    request.withCannedAcl(CannedAccessControlList.PublicRead);
//    amazonS3Client.putObject(request);
  }

  private File convertMultiPartFileToFile(MultipartFile file) {
    File convertedFile = new File(file.getOriginalFilename());
    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
      fos.write(file.getBytes());
    } catch (IOException e) {
      log.error("Error converting multipartFile to file", e);
    }
    return convertedFile;
  }

}