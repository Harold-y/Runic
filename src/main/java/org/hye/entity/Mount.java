package org.hye.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Mount implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer mtnId;

    private String mtnName;

    private String mtnPath;


}
