package com.viaplaygroup.hackday.entity;

import io.quarkiverse.hibernate.types.json.JsonBinaryType;
import io.quarkiverse.hibernate.types.json.JsonType;
import io.quarkiverse.hibernate.types.json.JsonTypes;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Set;

@Entity
@Cacheable
@TypeDef(name = JsonTypes.JSON, typeClass = JsonType.class)
@TypeDef(name = JsonTypes.JSON_BIN, typeClass = JsonBinaryType.class)
public class AssetEntity extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
            name = "assetSequence",
            sequenceName = "asset_id_seq",
            allocationSize = 1,
            initialValue = 1000000)
    @GeneratedValue(strategy = GenerationType.SEQUENCE  , generator = "assetSequence")
    public Long id;

    @Column(length = 40, unique = true)
    public String title;

    public String type;

    @Type(type = JsonTypes.JSON_BIN)
    @Column(columnDefinition = JsonTypes.JSON_BIN)
    public MetaDataEntity metadata;

    @OneToMany(mappedBy = "asset", orphanRemoval = true)
    public Set<MediaEntity> media;

    public AssetEntity() {
    }

    public AssetEntity(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "AssetEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", metadata=" + metadata +
                ", media=" + media +
                '}';
    }
}
