package com.assignment.aws.s3email.upload.s3;

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
public class S3Controller {

  private final AmazonS3 aws3ClientData;

  private final UploadFileDetailRepository uploadFileDetailRepository;

  @Value("${bucketName}")
  private String bucketName;

  public S3Controller(AmazonS3 aws3ClientData, UploadFileDetailRepository uploadFileDetailRepository) {
    this.aws3ClientData = aws3ClientData;
    this.uploadFileDetailRepository = uploadFileDetailRepository;
  }

  @PostMapping
  public String fileupload(@RequestParam("emails") String emails, @RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) {
    File file = converter(multipartFile);
    String uniqueFileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
    UploadFileDetail fileDetail = new UploadFileDetail();
    fileDetail.setName(uniqueFileName);
    fileDetail.setEmails(emails);
    uploadFileDetailRepository.save(fileDetail);
    PutObjectRequest request = new PutObjectRequest(bucketName, uniqueFileName, file);
    CannedAccessControlList publicReadRole = CannedAccessControlList.PublicRead;
    request.withCannedAcl(publicReadRole);
    aws3ClientData.putObject(request);
    redirectAttributes.addFlashAttribute("message", "Success");
    return "redirect:/";
  }
  
  private File converter(MultipartFile file) {
    File convertedFile = new File(file.getOriginalFilename());
    try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
      fos.write(file.getBytes());
    } catch (IOException e) {
      log.error("Error", e);
    }
    return convertedFile;
  }


  @GetMapping
  public String getDefaultPage() {
    return "index";
  }

}