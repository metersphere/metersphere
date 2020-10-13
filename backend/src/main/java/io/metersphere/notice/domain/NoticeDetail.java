package io.metersphere.notice.domain;

import io.metersphere.base.domain.Notice;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NoticeDetail extends Notice {
    private List<String> userIds = new ArrayList<>();
}
