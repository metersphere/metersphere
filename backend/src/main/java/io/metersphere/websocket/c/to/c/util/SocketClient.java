package io.metersphere.websocket.c.to.c.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: jason
 * @Date: 2020-12-23
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SocketClient {
    private Integer userId;
    private String username;
}
