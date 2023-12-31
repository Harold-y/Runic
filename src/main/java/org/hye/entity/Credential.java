package org.hye.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Credential implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "cred_id", type = IdType.AUTO)
    private Integer credId;

    private Integer credAdminId;

    private String credAccessKey;

    private String credIssueTime;


}
