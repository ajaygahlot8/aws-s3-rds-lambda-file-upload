package com.frontbackend.thymeleaf.bootstrap.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.frontbackend.thymeleaf.bootstrap.model.Upload;
import com.frontbackend.thymeleaf.bootstrap.repository.UploadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/")
public class FileUploadController {

  private final AmazonS3 amazonS3Client;

  private final UploadRepository uploadRepository;

  @Value("${app.awsServices.bucketName}")
  private String bucketName;

  public FileUploadController(AmazonS3 amazonS3Client, UploadRepository uploadRepository) {
    this.amazonS3Client = amazonS3Client;
    this.uploadRepository = uploadRepository;
  }

  @GetMapping
  public String main() {
    return "index";
  }

  @PostMapping
  public String upload(@RequestParam("emails") String emails, @RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) {
    File file = convertMultiPartFileToFile(multipartFile);

    String uniqueFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();

    prepareUploadAndSaveInDatabase(uniqueFileName, emails);

    uploadFileToS3bucket(uniqueFileName, file, bucketName);

    redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + multipartFile.getOriginalFilename() + "!");
    return "redirect:/";
  }

  private void prepareUploadAndSaveInDatabase(String uniqueFileName, String emails) {
    Upload upload = new Upload();
    upload.setFile_name(uniqueFileName);
    upload.setEmails(emails);
    uploadRepository.save(upload);
  }

  private void uploadFileToS3bucket(String fileName, File file, String bucketName) {
    PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
    request.withCannedAcl(CannedAccessControlList.PublicRead);
    amazonS3Client.putObject(request);
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