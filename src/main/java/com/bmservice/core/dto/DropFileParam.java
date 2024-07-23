package com.bmservice.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class DropFileParam {

    @JsonProperty("path")
    @NotNull
    @NotEmpty
    private String path;
}
