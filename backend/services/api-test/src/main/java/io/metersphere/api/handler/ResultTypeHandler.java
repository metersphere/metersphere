package io.metersphere.api.handler;


import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.handler.BaseTypeHandler;
import io.metersphere.sdk.dto.api.result.RequestResult;
import io.metersphere.sdk.util.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultTypeHandler extends BaseTypeHandler<RequestResult> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RequestResult parameter, JdbcType jdbcType) throws SQLException {
        byte[] d = JSON.toJSONBytes(parameter);
        ps.setBytes(i, d);
    }

    @Override
    public RequestResult getNullableResult(ResultSet rs, String columnName) throws SQLException {
        byte[] values = rs.getBytes(columnName);
        return getResults(values);
    }

    @Override
    public RequestResult getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        byte[] values = rs.getBytes(columnIndex);
        return getResults(values);
    }

    @Override
    public RequestResult getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        byte[] values = cs.getBytes(columnIndex);
        return getResults(values);
    }

    private RequestResult getResults(byte[] values) {
        if (ArrayUtils.isNotEmpty(values)) {
            return ApiDataUtils.parseObject(new String(values), RequestResult.class);
        }
        return null;
    }


}
