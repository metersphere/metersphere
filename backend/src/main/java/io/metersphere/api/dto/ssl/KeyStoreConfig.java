package io.metersphere.api.dto.ssl;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class KeyStoreConfig {
    private List<KeyStoreEntry> entrys;
    private List<KeyStoreFile> files;

    public String getDefaultAlias() {
        if (CollectionUtils.isNotEmpty(entrys)) {
            List<KeyStoreEntry> entryList = this.entrys.stream().filter(KeyStoreEntry::isDefault).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(entryList)) {
                if (StringUtils.isNotEmpty(entryList.get(0).getNewAsName())) {
                    return entryList.get(0).getNewAsName();
                } else {
                    return entryList.get(0).getOriginalAsName();
                }
            }
        }
        return null;
    }
}
