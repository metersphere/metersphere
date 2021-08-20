package io.metersphere.api.dto.share;

import io.metersphere.base.domain.ShareInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/2/23 5:04 下午
 * @Description
 */
@Getter
@Setter
public class ApiDocumentShareRequest extends ShareInfo {
    private List<String> shareApiIdList;
}
