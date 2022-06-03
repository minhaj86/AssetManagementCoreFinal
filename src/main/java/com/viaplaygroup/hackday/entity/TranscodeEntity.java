package com.viaplaygroup.hackday.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;

@Entity
@Cacheable
public class TranscodeEntity   extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "transcodeSequence",
            sequenceName = "transcode_id_seq",
            allocationSize = 1,
            initialValue = 1000000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE  , generator = "transcodeSequence")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE)
    @JoinColumn(name = "source_media_id", referencedColumnName = "id")
    public MediaEntity sourceMedia;

    @Column
    public String fileFullPath;

    @Column
    public String status;

    @Column
    public String profile;

    public MediaEntity getSourceMedia() {
        return sourceMedia;
    }

    public void setSourceMedia(MediaEntity sourceMedia) {
        this.sourceMedia = sourceMedia;
    }
}
