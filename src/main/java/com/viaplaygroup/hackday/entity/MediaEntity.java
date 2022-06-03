package com.viaplaygroup.hackday.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@Entity
@Cacheable
public class MediaEntity  extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "mediaSequence",
            sequenceName = "media_id_seq",
            allocationSize = 1,
            initialValue = 1000000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE  , generator = "mediaSequence")
    public Long id;

    @Column
    public String fileName;

    @Column
    public String filePath;

    @ManyToOne (fetch = FetchType.LAZY)
//    @JoinColumn(name = "id")
    public AssetEntity asset;

    @Override
    public String toString() {
        return "MediaEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
