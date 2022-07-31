package com.maskting.backend.domain;

import javax.persistence.*;

@Entity
public class Profile {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String path;

    private String name;
}
