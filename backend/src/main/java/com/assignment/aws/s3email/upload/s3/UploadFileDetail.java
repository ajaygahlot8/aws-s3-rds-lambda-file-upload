package com.assignment.aws.s3email.upload.s3;

import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
public class UploadFileDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;

  @Column(name = "name")
  private String name;

  @Column(name = "emails")
  private String emails;
}
