package io.metersphere.metadata.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.BodyFile;
import io.metersphere.base.domain.FileAssociation;
import io.metersphere.base.domain.FileAssociationExample;
import io.metersphere.base.mapper.FileAssociationMapper;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileAssociationService {
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void save(FileAssociation fileAssociation) {
        fileAssociationMapper.insert(fileAssociation);
    }

    public void deleteByFileId(String fileId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileMetadataIdEqualTo(fileId);
        fileAssociationMapper.deleteByExample(example);
    }

    public void deleteByResourceId(String sourceId) {
        if (StringUtils.isNotEmpty(sourceId)) {
            FileAssociationExample example = new FileAssociationExample();
            example.createCriteria().andSourceIdEqualTo(sourceId);
            fileAssociationMapper.deleteByExample(example);
        }
    }

    public void deleteByResourceIds(List<String> sourceIds) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdIn(sourceIds);
        fileAssociationMapper.deleteByExample(example);
    }

    public void deleteByResourceItemId(String id) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceItemIdEqualTo(id);
        fileAssociationMapper.deleteByExample(example);
    }


    public List<FileAssociation> getByFileId(String fileId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andFileMetadataIdEqualTo(fileId);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceId(String sourceId) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceIdEqualTo(sourceId);
        return fileAssociationMapper.selectByExample(example);
    }

    public List<FileAssociation> getByResourceItemId(String id) {
        FileAssociationExample example = new FileAssociationExample();
        example.createCriteria().andSourceItemIdEqualTo(id);
        return fileAssociationMapper.selectByExample(example);
    }

    public void save(List<BodyFile> files, String type, String sourceId) {
        if (!CollectionUtils.isEmpty(files)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            FileAssociationMapper batchMapper = sqlSession.getMapper(FileAssociationMapper.class);
            files.forEach(item -> {
                FileAssociation fileAssociation = new FileAssociation();
                fileAssociation.setId(UUID.randomUUID().toString());
                fileAssociation.setFileMetadataId(item.getFileId());
                fileAssociation.setFileType(item.getFileType());
                fileAssociation.setType(type);
                fileAssociation.setProjectId(item.getProjectId());
                fileAssociation.setSourceItemId(item.getId());
                fileAssociation.setSourceId(sourceId);
                batchMapper.insert(fileAssociation);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public void saveApi(String id, MsTestElement request, String type) {
        this.deleteByResourceId(id);
        if (StringUtils.isNotEmpty(id) && request != null && StringUtils.equalsIgnoreCase(request.getType(), HTTPSamplerProxy.class.getSimpleName())) {
            MsHTTPSamplerProxy samplerProxy = (MsHTTPSamplerProxy) request;
            List<BodyFile> files = getRefFiles(samplerProxy.getBody());
            this.save(files, type, id);
        }
    }

    public void saveScenario(String id, MsTestElement scenario) {
        this.deleteByResourceId(id);
        List<BodyFile> files = new ArrayList<>();
        if (scenario != null && scenario instanceof MsScenario) {
            MsScenario msScenario = (MsScenario) scenario;
            if (!CollectionUtils.isEmpty(msScenario.getVariables())) {
                msScenario.getVariables().stream().filter(ScenarioVariable::isCSVValid).forEach(keyValue -> {
                    files.addAll(keyValue.getFiles().stream().filter(BodyFile::isRef).collect(Collectors.toList()));
                });
            }
        }
        if (StringUtils.isNotEmpty(id) && scenario != null &&
                !CollectionUtils.isEmpty(scenario.getHashTree())) {
            this.getHashTree(scenario.getHashTree(), files);
            if (!CollectionUtils.isEmpty(files)) {
                List<BodyFile> list = files.stream().distinct().collect(Collectors.toList());
                this.save(list, FileAssociationType.SCENARIO.name(), id);
            }
        }
    }

    public void saveEnvironment(String id, String config, String type) {
        this.deleteByResourceId(id);
        List<BodyFile> files = new ArrayList<>();
        if (StringUtils.isNotEmpty(config)) {
            JSONObject commonConfig = JSONObject.parseObject(config).getJSONObject("commonConfig");
            List<ScenarioVariable> list = JSONObject.parseArray(commonConfig.getString("variables"), ScenarioVariable.class);
            list.stream().filter(ScenarioVariable::isCSVValid).forEach(keyValue -> {
                files.addAll(keyValue.getFiles().stream().filter(BodyFile::isRef).collect(Collectors.toList()));
            });
        }
        if (!CollectionUtils.isEmpty(files)) {
            List<BodyFile> list = files.stream().distinct().collect(Collectors.toList());
            this.save(list, type, id);
        }
    }

    private void getHashTree(List<MsTestElement> testElements, List<BodyFile> files) {
        testElements.forEach(item -> {
            if (StringUtils.equalsIgnoreCase(item.getType(), HTTPSamplerProxy.class.getSimpleName())) {
                MsHTTPSamplerProxy samplerProxy = (MsHTTPSamplerProxy) item;
                List<BodyFile> itemFiles = getRefFiles(samplerProxy.getBody());
                files.addAll(itemFiles);
            } else if (!CollectionUtils.isEmpty(item.getHashTree())) {
                this.getHashTree(item.getHashTree(), files);
            }
        });
    }

    private List<BodyFile> getRefFiles(Body body) {
        List<BodyFile> files = new ArrayList<>();
        if (body != null && !CollectionUtils.isEmpty(body.getKvs())) {
            body.getKvs().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                files.addAll(keyValue.getFiles().stream().filter(BodyFile::isRef).collect(Collectors.toList()));
            });
        }
        if (body != null && !CollectionUtils.isEmpty(body.getBinary())) {
            body.getBinary().stream().filter(KeyValue::isFile).filter(KeyValue::isEnable).forEach(keyValue -> {
                files.addAll(keyValue.getFiles().stream().filter(BodyFile::isRef).collect(Collectors.toList()));
            });
        }
        return files;
    }
}
