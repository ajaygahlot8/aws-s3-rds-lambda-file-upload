package com.uab.aws.db.model;

import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
public class FileUpload {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;

  @Column(name = "name_file")
  private String nameFile;

  @Column(name = "emails")
  private String emails;
}
