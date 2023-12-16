package org.example.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author mavenr
 * @Classname ProcessInfo
 * @Description TODO
 * @Date 2023/11/10 10:55
 */
@Data
@Builder
public class ProcessInfo {

    private String info;

    private String pid;
}
