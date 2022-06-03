package com.viaplaygroup.hackday.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@Entity
@Cacheable
public class TransferEntity extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "transferSequence",
            sequenceName = "transfer_id_seq",
            allocationSize = 1,
            initialValue = 1000000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE  , generator = "transferSequence")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    @JoinColumn(name = "source_media_id", referencedColumnName = "id")
    public MediaEntity sourceMedia;

    @Column
    public String fileFullPath;

    @Column
    public String status;

    @Column
    public String storage;

    public MediaEntity getSourceMedia() {
        return sourceMedia;
    }

    public void setSourceMedia(MediaEntity sourceMedia) {
        this.sourceMedia = sourceMedia;
    }
}
