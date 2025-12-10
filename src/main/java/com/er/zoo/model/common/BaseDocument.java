package com.er.zoo.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseDocument {
    @Id
    protected String id;

    @JsonIgnore
    @CreatedDate
    protected Instant created;
    @JsonIgnore
    @LastModifiedDate
    protected Instant updated;

    @Version
    @JsonIgnore
    protected Long version;
}
