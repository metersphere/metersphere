package io.metersphere.system.util;

import com.google.common.base.CaseFormat;
import io.metersphere.system.domain.User;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Component
public class BatchSaveUtils {
    private static NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BatchSaveUtils(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        BatchSaveUtils.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    public static <T> void batchSave(List<T> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            return;
        }
        // 表名
        String table = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, dtos.get(0).getClass().getSimpleName().toLowerCase());

        BeanPropertySqlParameterSource[] args = dtos.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(BeanPropertySqlParameterSource[]::new);

        List<String> fields = Arrays.stream(FieldUtils.getAllFields(User.class))
                .map(Field::getName)
                .filter(name -> !"serialVersionUID".equalsIgnoreCase(name))
                .toList();
        // 列名
        String columns = StringUtils.join(fields.stream().map(name -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)).toList(), ",");
        // 值
        String values = StringUtils.join(fields, ",:");

        String sql = "INSERT INTO " + table + "(" + columns + ") VALUES (:" + values + ")";
        namedParameterJdbcTemplate.batchUpdate(sql, args);
    }
}
