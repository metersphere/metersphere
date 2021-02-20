package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiDocumentInfoDTO;
import io.metersphere.api.dto.ApiDocumentRequest;
import io.metersphere.api.dto.ApiDocumentSimpleInfoDTO;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.mapper.ext.ExtApiDocumentMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/2/7 10:37 上午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDocumentService {

    @Resource
    ExtApiDocumentMapper extApiDocumentMapper;

    public List<ApiDocumentSimpleInfoDTO> findApiDocumentSimpleInfoByRequest(ApiDocumentRequest request) {
        return extApiDocumentMapper.findApiDocumentSimpleInfoByRequest(request);
    }

    public ApiDocumentInfoDTO conversionModelToDTO(ApiDefinitionWithBLOBs apiModel) {
        ApiDocumentInfoDTO apiInfoDTO = new ApiDocumentInfoDTO();
        JSONObject previewObj = new JSONObject();
        if (apiModel != null) {
            apiInfoDTO.setId(apiModel.getId());
            apiInfoDTO.setName(apiModel.getName());
            apiInfoDTO.setMethod(apiModel.getMethod());
            apiInfoDTO.setUri(apiModel.getPath());
            apiInfoDTO.setStatus(apiModel.getStatus());

            JSONObject requestJsonObj = JSONObject.parseObject(apiModel.getRequest());
            //head赋值
            if (requestJsonObj.containsKey("headers")) {
                JSONArray requestHeadDataArr = new JSONArray();
                //head赋值
                JSONArray headArr = requestJsonObj.getJSONArray("headers");
                for (int index = 0; index < headArr.size(); index++) {
                    JSONObject headObj = headArr.getJSONObject(index);
                    if (headObj.containsKey("name") && headObj.containsKey("value")) {
                        requestHeadDataArr.add(headObj);
                    }
                }
                apiInfoDTO.setRequestHead(requestHeadDataArr.toJSONString());
            }
            //url参数赋值
            JSONArray urlParamArr = new JSONArray();
            if (requestJsonObj.containsKey("arguments")) {
                //urlParam -- query赋值
                JSONArray headArr = requestJsonObj.getJSONArray("arguments");
                for (int index = 0; index < headArr.size(); index++) {
                    JSONObject headObj = headArr.getJSONObject(index);
                    if (headObj.containsKey("name") && headObj.containsKey("value")) {
                        urlParamArr.add(headObj);
                    }
                }
            }
            if (requestJsonObj.containsKey("rest")) {
                //urlParam -- rest赋值
                JSONArray headArr = requestJsonObj.getJSONArray("rest");
                for (int index = 0; index < headArr.size(); index++) {
                    JSONObject headObj = headArr.getJSONObject(index);
                    if (headObj.containsKey("name") && headObj.containsKey("value")) {
                        urlParamArr.add(headObj);
                    }
                }
            }
            apiInfoDTO.setUrlParams(urlParamArr.toJSONString());
            //请求体参数类型
            if (requestJsonObj.containsKey("body")) {
                JSONObject bodyObj = requestJsonObj.getJSONObject("body");
                if (bodyObj.containsKey("type")) {
                    String type = bodyObj.getString("type");
                    if(StringUtils.equals(type,"WWW_FORM")){
                        apiInfoDTO.setRequestBodyParamType("x-www-from-urlencoded");
                    }else if(StringUtils.equals(type,"Form Data")) {
                        apiInfoDTO.setRequestBodyParamType("form-data");
                    }else {
                        apiInfoDTO.setRequestBodyParamType(type);
                    }

                    if (StringUtils.equals(type, "JSON")) {
                        //判断是否是JsonSchema
                        boolean isJsonSchema = false;
                        if(bodyObj.containsKey("format")){
                            String foramtValue = String.valueOf(bodyObj.get("format"));
                            if(StringUtils.equals("JSON-SCHEMA",foramtValue)){
                                isJsonSchema = true;
                            }
                        }
                        if(isJsonSchema){
                            apiInfoDTO.setRequestBodyParamType("JSON-SCHEMA");
                            apiInfoDTO.setJsonSchemaBody(bodyObj);
                        }else {
                            if (bodyObj.containsKey("raw")) {
                                String raw = bodyObj.getString("raw");
                                apiInfoDTO.setRequestBodyStrutureData(raw);
                                previewObj = JSONObject.parseObject(raw);
                            }
                        }
                    } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                        if (bodyObj.containsKey("raw")) {
                            String raw = bodyObj.getString("raw");
                            apiInfoDTO.setRequestBodyStrutureData(raw);
                            previewObj = JSONObject.parseObject(raw);
                        }
                    } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                        if (bodyObj.containsKey("kvs")) {
                            JSONArray bodyParamArr = new JSONArray();
                            JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                            for (int i = 0; i < kvsArr.size(); i++) {
                                JSONObject kv = kvsArr.getJSONObject(i);
                                if (kv.containsKey("name")&&kv.containsKey("value")) {
                                    bodyParamArr.add(kv);
                                    previewObj.put(String.valueOf(kv.get("name")),String.valueOf(kv.get("value")));
                                }
                            }
                            apiInfoDTO.setRequestBodyFormData(bodyParamArr.toJSONString());
                        }
                    } else if (StringUtils.equals(type, "BINARY")) {
                        if (bodyObj.containsKey("binary")) {
                            List<Map<String, String>> bodyParamList = new ArrayList<>();
                            JSONArray kvsArr = bodyObj.getJSONArray("binary");
                            for (int i = 0; i < kvsArr.size(); i++) {
                                JSONObject kv = kvsArr.getJSONObject(i);
                                if (kv.containsKey("description") && kv.containsKey("files")) {
                                    Map<String, String> bodyMap = new HashMap<>();
                                    String name = kv.getString("description");
                                    JSONArray fileArr = kv.getJSONArray("files");
                                    String value = "";
                                    for (int j = 0; j < fileArr.size(); j++) {
                                        JSONObject fileObj = fileArr.getJSONObject(j);
                                        if (fileObj.containsKey("name")) {
                                            value += fileObj.getString("name") + " ;";
                                        }
                                    }
                                    bodyMap.put("name", name);
                                    bodyMap.put("value", value);
                                    bodyMap.put("contentType", "File");
                                    bodyParamList.add(bodyMap);

                                    previewObj.put(String.valueOf(name),String.valueOf(value));
                                }
                            }
                            apiInfoDTO.setRequestBodyFormData(JSONArray.toJSONString(bodyParamList));
                        }
                    }
                }
            }
            JSONObject responseJsonObj = JSONObject.parseObject(apiModel.getResponse());
            //赋值响应头
            if (responseJsonObj.containsKey("headers")) {
                JSONArray responseHeadDataArr = new JSONArray();
                JSONArray headArr = responseJsonObj.getJSONArray("headers");
                for (int index = 0; index < headArr.size(); index++) {
                    JSONObject headObj = headArr.getJSONObject(index);
                    if (headObj.containsKey("name") && headObj.containsKey("value")) {
                        responseHeadDataArr.add(headObj);
                    }
                }
                apiInfoDTO.setResponseHead(responseHeadDataArr.toJSONString());
            }
            // 赋值响应体
            if (responseJsonObj.containsKey("body")) {
                JSONObject bodyObj = responseJsonObj.getJSONObject("body");
                if (bodyObj.containsKey("type")) {
                    String type = bodyObj.getString("type");
                    if(StringUtils.equals(type,"WWW_FORM")){
                        apiInfoDTO.setResponseBodyParamType("x-www-from-urlencoded");
                    }else if(StringUtils.equals(type,"Form Data")) {
                        apiInfoDTO.setResponseBodyParamType("form-data");
                    }else {
                        apiInfoDTO.setResponseBodyParamType(type);
                    }
                    if (StringUtils.equalsAny(type, "JSON", "XML", "Raw")) {
                        if (bodyObj.containsKey("raw")) {
                            String raw = bodyObj.getString("raw");
                            apiInfoDTO.setResponseBodyStrutureData(raw);
                        }
                    } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                        if (bodyObj.containsKey("kvs")) {
                            JSONArray bodyParamArr = new JSONArray();
                            JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                            for (int i = 0; i < kvsArr.size(); i++) {
                                JSONObject kv = kvsArr.getJSONObject(i);
                                if (kv.containsKey("name")) {
                                    bodyParamArr.add(kv);
                                }
                            }
                            apiInfoDTO.setResponseBodyFormData(bodyParamArr.toJSONString());
                        }
                    } else if (StringUtils.equals(type, "BINARY")) {
                        if (bodyObj.containsKey("binary")) {
                            List<Map<String, String>> bodyParamList = new ArrayList<>();
                            JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                            for (int i = 0; i < kvsArr.size(); i++) {
                                JSONObject kv = kvsArr.getJSONObject(i);
                                if (kv.containsKey("description") && kv.containsKey("files")) {
                                    Map<String, String> bodyMap = new HashMap<>();

                                    String name = kv.getString("description");
                                    JSONArray fileArr = kv.getJSONArray("files");
                                    String value = "";
                                    for (int j = 0; j < fileArr.size(); j++) {
                                        JSONObject fileObj = fileArr.getJSONObject(j);
                                        if (fileObj.containsKey("name")) {
                                            value += fileObj.getString("name") + " ;";
                                        }
                                    }
                                    bodyMap.put("name", name);
                                    bodyMap.put("value", value);
                                    bodyParamList.add(bodyMap);
                                }
                            }
                            apiInfoDTO.setResponseBodyFormData(JSONArray.toJSONString(bodyParamList));
                        }
                    }
                }
            }
            // 赋值响应码
            if (responseJsonObj.containsKey("statusCode")) {
                JSONArray responseStatusDataArr = new JSONArray();
                JSONArray statusArr = responseJsonObj.getJSONArray("statusCode");
                for (int index = 0; index < statusArr.size(); index++) {
                    JSONObject statusObj = statusArr.getJSONObject(index);
                    if (statusObj.containsKey("name") && statusObj.containsKey("value")) {
                        responseStatusDataArr.add(statusObj);
                    }
                }
                apiInfoDTO.setResponseCode(responseStatusDataArr.toJSONString());
            }
        }
        apiInfoDTO.setRequestPreviewData(previewObj);
        return apiInfoDTO;
    }
}
