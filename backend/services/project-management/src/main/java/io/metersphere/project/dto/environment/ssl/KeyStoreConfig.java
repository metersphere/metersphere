package io.metersphere.project.dto.environment.ssl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class KeyStoreConfig {
    @Schema(description = "证书条目")
    private List<KeyStoreEntry> entry;
    @Schema(description = "证书文件")
    private List<KeyStoreFile> files;

    public String getDefaultAlias() {
        if (CollectionUtils.isNotEmpty(entry)) {
            List<KeyStoreEntry> entryList = this.entry.stream().filter(KeyStoreEntry::isDefault).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(entryList)) {
                if (StringUtils.isNotEmpty(entryList.getFirst().getNewAsName())) {
                    return entryList.getFirst().getNewAsName();
                } else {
                    return entryList.getFirst().getOriginalAsName();
                }
            }
        }
        return null;
    }

    public String getAlias(String asName) {
        if (CollectionUtils.isNotEmpty(entry)) {
            List<KeyStoreEntry> entryList = this.entry.stream().filter(ks -> StringUtils.equals(asName, ks.getNewAsName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(entryList) && CollectionUtils.isNotEmpty(files) && files.size() == 1) {
                return entryList.getFirst().getOriginalAsName();
            }
        }
        return asName;
    }
}
