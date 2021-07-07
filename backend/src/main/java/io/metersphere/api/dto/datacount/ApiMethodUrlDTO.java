package io.metersphere.api.dto.datacount;

import java.util.Objects;

/**
 * @author song.tianyang
 * @Date 2021/6/21 4:15 下午
 */
public class ApiMethodUrlDTO {
    public String url;
    public String method;

    public ApiMethodUrlDTO(String url, String method) {
        this.url = url == null ? "" : url;
        this.method = method == null ? "" : method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiMethodUrlDTO that = (ApiMethodUrlDTO) o;
        return url.equals(that.url) && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}
