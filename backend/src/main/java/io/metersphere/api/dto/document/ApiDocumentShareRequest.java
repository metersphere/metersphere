package io.metersphere.api.dto.document;

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
public class ApiDocumentShareRequest {
    private String shareType;
    private List<String> shareApiIdList;
}
