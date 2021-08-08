package com.frontbackend.thymeleaf.bootstrap.upload;

import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
public class FileDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;

  @Column(name = "file_name")
  private String file_name;

  @Column(name = "emails")
  private String emails;
}
