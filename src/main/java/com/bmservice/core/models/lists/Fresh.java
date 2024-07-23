package com.bmservice.core.models.lists;

import lombok.Data;

@Data
public class Fresh {

    private int id;
    private String email;
    private String fname;
    private String lname;
    private String offersExcluded;
    private int listId;

    private String schema;
    private String table;


}
