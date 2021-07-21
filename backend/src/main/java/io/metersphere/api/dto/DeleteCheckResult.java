package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/7/19 10:52 上午
 */
@Getter
@Setter
public class DeleteCheckResult {
    boolean deleteFlag;
    List<String> checkMsg;
}
