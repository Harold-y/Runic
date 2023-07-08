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
public class File implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer fId;

    private String fName;

    private String fPath;

    private Integer fKeyId;

    private String fEncryptedType;


}
