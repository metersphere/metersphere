package io.metersphere.interceptor;

import com.github.pagehelper.util.MetaObjectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                        Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                        Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class LikeStringEscapeInterceptor implements Interceptor {

    /**
     * 检查sql中，是否含有like查询
     */
    private static Pattern LIKE_PARAM_PATTERN = Pattern
            .compile("LIKE\\s*CONCAT\\('%',\\s*\\?,\\s*'%'\\)", Pattern.CASE_INSENSITIVE);

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;
        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        SqlCommandType sqlCommandType = ms.getSqlCommandType();
        StatementType statementType = ms.getStatementType();
        // 只处理 有参数的查询语句
        if (sqlCommandType == SqlCommandType.SELECT
                && statementType == StatementType.PREPARED) {
            escapeParameterIfContainingLike(boundSql);
            return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        }
        return invocation.proceed();
    }

    void escapeParameterIfContainingLike(BoundSql boundSql) {
        if (boundSql == null) {
            return;
        }
        String prepareSql = boundSql.getSql();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 找到 like 后面的参数
        List<Integer> position = findLikeParam(prepareSql);
        if (position == null || position.size() == 0) {
            return;
        }

        List<ParameterMapping> likeParameterMappings = new ArrayList<>();

        // 复制
        MetaObject metaObject = MetaObjectUtil.forObject(boundSql.getParameterObject());
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping pm = parameterMappings.get(i);
            String property = pm.getProperty();
            // 忽略无法处理的属性。例如 __frch_
            if (metaObject.hasGetter(property)) {
                boundSql.setAdditionalParameter(property, metaObject.getValue(property));
                if (position.contains(i)) {
                    likeParameterMappings.add(pm);
                }
            }
        }

        // 覆盖 转义字符
        delegateMetaParameterForEscape(boundSql, likeParameterMappings);
    }


    /**
     * @param boundSql              原 boundSql
     * @param likeParameterMappings 需要转义的参数
     * @return 支持转义的 boundSql
     */
    void delegateMetaParameterForEscape(BoundSql boundSql, List<ParameterMapping> likeParameterMappings) {

        for (ParameterMapping mapping : likeParameterMappings) {
            String property = mapping.getProperty();

            MetaObject metaObject = MetaObjectUtil.forObject(boundSql.getParameterObject());
            Object value = metaObject.getValue(property);
            if (value instanceof String) {
                boundSql.setAdditionalParameter(property, escapeLike((String) value));
            }
        }
    }

    String escapeLike(String value) {
        if (value != null) {
            return value
                    .replaceAll("\\\\", "\\\\\\\\")
                    .replaceAll("_", "\\\\_")
                    .replaceAll("%", "\\\\%");
        }
        return null;
    }


    List<Integer> findLikeParam(String prepareSql) {
        Matcher matcher = LIKE_PARAM_PATTERN.matcher(prepareSql);

        int pos = 0;
        List<Integer> indexes = new ArrayList<>();

        while (matcher.find(pos)) {
            int start = matcher.start();
            int index = StringUtils.countMatches(prepareSql.substring(0, start), "?");
            indexes.add(index);
            pos = matcher.end();
        }
        return indexes;
    }
}