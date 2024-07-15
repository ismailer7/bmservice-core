package com.bmservice.core;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class IspDto {

    @NotEmpty
    private String name;

    private int status;

}
