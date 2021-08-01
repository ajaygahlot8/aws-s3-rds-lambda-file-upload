package com.frontbackend.thymeleaf.bootstrap.model;

import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
public class Upload {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Integer id;

  @Column(name = "file_name")
  private String file_name;

  @Column(name = "emails")
  private String emails;
}
