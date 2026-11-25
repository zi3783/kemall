package com.kemall.iam.api.dto.workspace;

import com.kemall.iam.api.dto.auth.TokenInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 切换工作空间结果：新的工作空间上下文 + 重新签发的 Token。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchWorkspaceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private WorkspaceInfo workspace;

    private TokenInfo tokenInfo;
}
