package com.frontbackend.thymeleaf.bootstrap.upload;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
public class UploadS3Controller {

  private final AmazonS3 awsS3Client;

  private final FileDetailRepository fileDetailRepository;

  @Value("${bucketName}")
  private String bucketName;

  public UploadS3Controller(AmazonS3 amazonS3Client, FileDetailRepository uploadRepository) {
    this.awsS3Client = amazonS3Client;
    this.fileDetailRepository = uploadRepository;
  }

  @PostMapping
  public String fileupload(@RequestParam("emails") String emails, @RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) {
    File file = convertofile(multipartFile);
    String uniqueFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
    FileDetail fileDetail = new FileDetail();
    fileDetail.setFile_name(uniqueFileName);
    fileDetail.setEmails(emails);
    fileDetailRepository.save(fileDetail);
    PutObjectRequest request = new PutObjectRequest(bucketName, uniqueFileName, file);
    CannedAccessControlList publicReadRole = CannedAccessControlList.PublicRead;
    request.withCannedAcl(publicReadRole);
    awsS3Client.putObject(request);
    redirectAttributes.addFlashAttribute("message", "File sent to your emails please check mailbox!");
    return "redirect:/";
  }

  @GetMapping
  public String main() {
    return "index";
  }
  
  private File convertofile(MultipartFile file) {
    File convertedFile = new File(file.getOriginalFilename());
    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
      fos.write(file.getBytes());
    } catch (IOException e) {
      log.error("Error occurred", e);
    }
    return convertedFile;
  }

}