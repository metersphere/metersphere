package io.metersphere.api.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public abstract class MsAbstractParser<T> extends ApiImportAbstractParser<T> {

    protected List<MsHTTPSamplerProxy> parseMsHTTPSamplerProxy(JSONObject testObject, String tag) {
        JSONObject requests = testObject.getJSONObject(tag);
        List<MsHTTPSamplerProxy> msHTTPSamplerProxies =  new ArrayList<>();
        requests.keySet().forEach(requestName -> {
            JSONObject requestObject = requests.getJSONObject(requestName);
            String path = requestObject.getString("url");
            String method = requestObject.getString("method");
            MsHTTPSamplerProxy request = buildRequest(requestName, path, method);
            parseBody(requestObject, request.getBody());
            parseHeader(requestObject, request.getHeaders());
            parsePath(request);
            msHTTPSamplerProxies.add(request);
        });
        return msHTTPSamplerProxies;
    }

    private void parsePath(MsHTTPSamplerProxy request) {
        if (StringUtils.isNotBlank(request.getPath())) {
            String[] split = request.getPath().split("\\?");
            String path = split[0];
            parseQueryParameters(split, request.getArguments());
            request.setPath(path);
        } else {
            request.setPath("/");
        }
    }

    private void parseQueryParameters(String[] split, List<KeyValue> arguments) {
        if (split.length > 1) {
            try {
                String queryParams = split[1];
                queryParams = URLDecoder.decode(queryParams, "UTF-8");
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    arguments.add(new KeyValue(kv[0], kv.length < 2 ? null : kv[1]));
                }
            } catch (UnsupportedEncodingException e) {
                LogUtil.info(e.getMessage(), e);
                return;
            }
        }
    }
    private void parseHeader(JSONObject requestObject, List<KeyValue> msHeaders) {
        JSONArray headers = requestObject.getJSONArray("headers");
        if (CollectionUtils.isNotEmpty(headers)) {
            for (int i = 0; i < headers.size(); i++) {
                JSONObject header = headers.getJSONObject(i);
                msHeaders.add(new KeyValue(header.getString("name"), header.getString("value")));
            }
        }
    }

    private void parseBody(JSONObject requestObject, Body msBody) {
        if (requestObject.containsKey("body")) {
            Object body = requestObject.get("body");
            if (body instanceof JSONArray) {
                JSONArray bodies = requestObject.getJSONArray("body");
                if (bodies != null) {
                    StringBuilder bodyStr = new StringBuilder();
                    for (int i = 0; i < bodies.size(); i++) {
                        String tmp = bodies.getString(i);
                        bodyStr.append(tmp);
                    }
                    msBody.setType(Body.RAW);
                    msBody.setRaw(bodyStr.toString());
                }
            } else if (body instanceof JSONObject) {
                JSONObject bodyObj = requestObject.getJSONObject("body");
                if (bodyObj != null) {
                    ArrayList<KeyValue> kvs = new ArrayList<>();
                    bodyObj.keySet().forEach(key -> {
                        kvs.add(new KeyValue(key, bodyObj.getString(key)));
                    });
                    msBody.setKvs(kvs);
                    msBody.setType(Body.WWW_FROM);
                }
            }
        }
    }


    /**
     * 删除没有用例的节点
     * @param nodeTree
     * @param ids
     * @return
     */
    public void cutDownTree(List<NodeTree> nodeTree, Set<String> ids) {
        Iterator<NodeTree> iterator = nodeTree.iterator();
        while (iterator.hasNext()) {
            NodeTree item = iterator.next();
            if (cutDownTree(item, ids)) {
                iterator.remove();
            }
        }
    }

    private boolean cutDownTree(NodeTree nodeTree, Set<String> ids) {
        boolean delete = true;
        if (ids.contains(nodeTree.getId())) {
            delete = false;
        }

        List<NodeTree> children = nodeTree.getChildren();

        if (CollectionUtils.isNotEmpty(children)) {
            Iterator<NodeTree> iterator = children.iterator();
            while (iterator.hasNext()) {
                NodeTree item = iterator.next();
                if (cutDownTree(item, ids)) {
                    iterator.remove();
                } else {
                    delete = false;
                }
            }
        }
        return delete;
    }

    public Map<String, NodeTree> getNodeMap(List<NodeTree> nodeTree) {
        Map<String, NodeTree> nodeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(nodeTree)) {
            nodeTree.forEach(item -> {
                getNodeMap(nodeMap, item);
            });
        }
        return nodeMap;
    }

    private void getNodeMap(Map<String, NodeTree> nodeMap, NodeTree nodeTree) {
        nodeMap.put(nodeTree.getId(), nodeTree);
        List<NodeTree> children = nodeTree.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            children.forEach(item -> {
                getNodeMap(nodeMap, item);
            });
        }
    }
}
