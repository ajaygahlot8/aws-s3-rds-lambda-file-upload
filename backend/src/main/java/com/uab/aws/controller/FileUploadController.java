package com.uab.aws.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.uab.aws.db.model.FileUpload;
import com.uab.aws.db.repository.FileUploadRepository;
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

  @Value("${bucket}")
  private String bucket;

  private final AmazonS3 clientS3AWS;

  private final FileUploadRepository fileUploadRepository;

  public FileUploadController(AmazonS3 clientS3AWS, FileUploadRepository fileUploadRepository) {
    this.clientS3AWS = clientS3AWS;
    this.fileUploadRepository = fileUploadRepository;
  }

  @PostMapping
  public String upload(@RequestParam("file") MultipartFile originalFile, RedirectAttributes attributes, @RequestParam("emails") String emails) {
    File file = convertToFile(originalFile);
    String name = generateFileName(originalFile);
    saveAndUploadToS3(emails, file, name);
    attributes.addFlashAttribute("data", "Done " + originalFile.getOriginalFilename() + "!");
    return "redirect:/";
  }

  private void saveAndUploadToS3(String emails, File file, String name) {
    save(name, emails);
    upload(name, file, bucket);
  }

  private String generateFileName(MultipartFile file) {
    return System.currentTimeMillis() + "_" + file.getOriginalFilename();
  }

  private void save(String uniqueFileName, String emails) {
    FileUpload fileUpload = prepareFileUploadObject(uniqueFileName, emails);
    fileUploadRepository.save(fileUpload);
  }

  private FileUpload prepareFileUploadObject(String uniqueFileName, String emails) {
    FileUpload fileUpload = new FileUpload();
    fileUpload.setNameFile(uniqueFileName);
    fileUpload.setEmails(emails);
    return fileUpload;
  }

  private void upload(String fileName, File file, String bucketName) {
    PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
    request.withCannedAcl(CannedAccessControlList.PublicRead);
    clientS3AWS.putObject(request);
  }

  private File convertToFile(MultipartFile file) {
    File convertedFile = new File(file.getOriginalFilename());
    try (FileOutputStream fileOutputStream = new FileOutputStream(convertedFile)) {
      fileOutputStream.write(file.getBytes());
    } catch (IOException e) {
      log.error("Error", e);
    }
    return convertedFile;
  }

  @GetMapping
  public String defaultPage() {
    return "index";
  }
}