package com.viaplaygroup.hackday.dto;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

public class MediaDto {
    public Long id;
    public String fileName;
    public String filePath;

    public String assetTitle;

    public String assetId;
}
