package io.metersphere.environment.ssl;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class KeyStoreConfig {
    private List<KeyStoreEntry> entry;
    private List<KeyStoreFile> files;

    public String getDefaultAlias() {
        if (CollectionUtils.isNotEmpty(entry)) {
            List<KeyStoreEntry> entryList = this.entry.stream().filter(KeyStoreEntry::isDefault).collect(Collectors.toList());
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

    public String getAlias(String asName) {
        if (CollectionUtils.isNotEmpty(entry)) {
            List<KeyStoreEntry> entryList = this.entry.stream().filter(ks -> StringUtils.equals(asName, ks.getNewAsName())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(entryList) && CollectionUtils.isNotEmpty(files) && files.size() == 1) {
                return entryList.get(0).getOriginalAsName();
            }
        }
        return asName;
    }
}
