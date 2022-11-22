package io.metersphere.service.ext;

import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.commons.enums.FileAssociationTypeEnums;
import io.metersphere.metadata.service.FileAssociationService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExtFileAssociationService extends FileAssociationService {

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
        if (StringUtils.isNotEmpty(id) && scenario != null && !CollectionUtils.isEmpty(scenario.getHashTree())) {
            this.getHashTree(scenario.getHashTree(), files);
            if (!CollectionUtils.isEmpty(files)) {
                List<BodyFile> list = files.stream().distinct().collect(Collectors.toList());
                this.save(list, FileAssociationTypeEnums.SCENARIO.name(), id);
            }
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
