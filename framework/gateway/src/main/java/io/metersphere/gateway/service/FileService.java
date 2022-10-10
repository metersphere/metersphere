package io.metersphere.gateway.service;

import io.metersphere.base.domain.FileContent;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.FileMetadataWithBLOBs;
import io.metersphere.base.mapper.FileContentMapper;
import io.metersphere.base.mapper.FileMetadataMapper;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class FileService {
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;

    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public RsaKey checkRsaKey() {
        String key = "ms.login.rsa.key";
        FileContent value = getFileContent(key);
        if (value == null) {
            try {
                RsaKey rsaKey = RsaUtil.getRsaKey();
                byte[] bytes = SerializationUtils.serialize(rsaKey);
                final FileMetadataWithBLOBs fileMetadata = new FileMetadataWithBLOBs();
                fileMetadata.setId(key);
                fileMetadata.setName(key);
                fileMetadata.setSize((long) bytes.length);
                fileMetadata.setCreateTime(System.currentTimeMillis());
                fileMetadata.setUpdateTime(System.currentTimeMillis());
                fileMetadata.setType("RSA_KEY");
                fileMetadata.setLatest(true);
                fileMetadata.setRefId(fileMetadata.getId());
                fileMetadataMapper.insert(fileMetadata);

                FileContent fileContent = new FileContent();
                fileContent.setFileId(fileMetadata.getId());
                fileContent.setFile(bytes);
                fileContentMapper.insert(fileContent);
                return rsaKey;
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return SerializationUtils.deserialize(value.getFile());
    }

    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }
}
