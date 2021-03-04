package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 级联选择器-数据格式
 *
 * @author song.tianyang
 * @Date 2021/3/4 10:47 上午
 * @Description
 */
@Getter
@Setter
public class CascaderDTO {
    private String value;
    private String label;
    private List<CascaderDTO> children;
}
