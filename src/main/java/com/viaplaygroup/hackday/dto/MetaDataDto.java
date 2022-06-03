package com.viaplaygroup.hackday.dto;

import java.io.Serializable;

public class MetaDataDto implements Serializable {

    public String producer;

    public String director;

    public String actor;

    public String yearOfRelease;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("\n");
        sb.append("Producer: '").append(producer).append('\'');
        sb.append("<br>Director: '").append(director).append('\'');
        sb.append("<br>Actor: '").append(actor).append('\'');
        sb.append("<br>YearOfRelease: '").append(yearOfRelease).append('\'');
        return sb.toString();
    }
}
