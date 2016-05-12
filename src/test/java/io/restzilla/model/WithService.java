/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package io.restzilla.model;

import io.restzilla.RestQuery;
import io.restzilla.RestResource;

import javax.persistence.Entity;

@Entity
@RestResource(queries = @RestQuery(parameters = "custom=true", method = "findAllByService"))
public class WithService extends BaseEntity {
    
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
