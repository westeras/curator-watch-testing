package com.westeras.curator.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Model object to be written into watched Zookeeper node
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SampleZNodeObject {

    private String id;
    private Long timestamp;
    private String hostname;

}
