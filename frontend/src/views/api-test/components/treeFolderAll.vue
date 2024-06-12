<template>
  <MsFolderAll
    v-model:isExpandAll="isExpandAll"
    :active-folder="props.activeFolder"
    :folder-name="props.folderName"
    :all-count="props.allCount"
    @set-active-folder="(val: string) => emit('setActiveFolder', val)"
  >
    <template #expandLeft>
      <a-dropdown v-model:popup-visible="visible" :hide-on-select="false">
        <MsButton
          v-show="!props.notShowOperation"
          type="icon"
          status="secondary"
          class="!mr-[4px] p-[4px]"
          @click="visible = !visible"
        >
          <MsIcon :type="`${showExpandApi ? 'icon-icon_more_outlined' : 'icon-icon_protocol'}`" />
        </MsButton>
        <template #content>
          <!-- 有 展开请求的开关 -->
          <template v-if="showExpandApi">
            <a-doption class="api-expend w-full">
              {{ t('apiScenario.api') }}
              <a-switch v-model:model-value="isExpandApi" size="small" @click.stop @change="emit('changeApiExpand')" />
            </a-doption>
            <a-dsubmenu>
              <template #default>{{ t('ms.paramsInput.protocol') }}</template>
              <template #content>
                <a-checkbox
                  class="checkbox-all"
                  :model-value="isCheckedAll"
                  :indeterminate="indeterminate"
                  @change="handleChangeAll"
                  >{{ t('common.all') }}
                </a-checkbox>
                <a-checkbox-group direction="vertical" :model-value="selectedProtocols" @change="handleGroupChange">
                  <a-checkbox v-for="item in allProtocolList" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </a-checkbox>
                </a-checkbox-group>
              </template>
            </a-dsubmenu>
          </template>
          <!-- 没有 展开请求的开关 -->
          <template v-else>
            <a-checkbox
              class="checkbox-all"
              :model-value="isCheckedAll"
              :indeterminate="indeterminate"
              @change="handleChangeAll"
              >{{ t('common.all') }}
            </a-checkbox>
            <a-checkbox-group direction="vertical" :model-value="selectedProtocols" @change="handleGroupChange">
              <a-checkbox v-for="item in allProtocolList" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-checkbox>
            </a-checkbox-group>
          </template>
        </template>
      </a-dropdown>
    </template>
    <template #expandRight>
      <slot name="expandRight"></slot>
    </template>
  </MsFolderAll>
</template>

<script setup lang="ts">
  import { CheckboxOption } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFolderAll from '@/components/business/ms-folder-all/index.vue';

  import { getProtocolList } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = defineProps<{
    activeFolder?: string; // 选中的节点
    folderName: string; // 名称
    allCount: number; // 总数
    showExpandApi?: boolean; // 展示 展开请求的开关
    notShowOperation?: boolean; // 是否展示操作按钮
  }>();
  const emit = defineEmits<{
    (e: 'setActiveFolder', val: string): void;
    (e: 'changeApiExpand'): void;
    (e: 'selectedProtocolsChange'): void;
  }>();

  const isExpandAll = defineModel<boolean | undefined>('isExpandAll', {
    required: false,
    default: undefined,
  });
  const isExpandApi = defineModel<boolean>('isExpandApi', {
    required: false,
    default: undefined,
  });
  const selectedProtocols = defineModel<string[]>('selectedProtocols', {
    required: true,
  });

  const { t } = useI18n();
  const appStore = useAppStore();

  const visible = ref(false);
  const selectedList = ref<CheckboxOption[]>([]); // 已选
  const allProtocolList = ref<CheckboxOption[]>([]); // 全部
  const isCheckedAll = computed(() => {
    return selectedList.value.length === allProtocolList.value.length;
  });
  const indeterminate = computed(() => {
    return selectedList.value.length > 0 && selectedList.value.length < allProtocolList.value.length;
  });
  const handleChangeAll = (value: boolean | (string | number | boolean)[]) => {
    if (value) {
      selectedList.value = allProtocolList.value;
    } else {
      selectedList.value = [];
    }
  };
  const handleGroupChange = (value: (string | number | boolean)[]) => {
    selectedList.value = allProtocolList.value.filter((e: CheckboxOption) => value.includes(e.value));
  };

  async function initProtocolList() {
    try {
      const res = await getProtocolList(appStore.currentOrgId);
      allProtocolList.value = res.map((e) => ({
        label: e.protocol,
        value: e.protocol,
        polymorphicName: e.polymorphicName,
        pluginId: e.pluginId,
      }));
      selectedList.value = allProtocolList.value;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => selectedList.value,
    (val) => {
      selectedProtocols.value = val.map((e) => e.value as string);
      emit('selectedProtocolsChange');
    }
  );

  watch(
    () => props.notShowOperation,
    (val) => {
      if (val) {
        isExpandAll.value = undefined;
      } else {
        isExpandAll.value = false;
      }
    }
  );

  onBeforeMount(() => {
    initProtocolList();
  });
</script>

<style lang="less" scoped>
  .arco-dropdown {
    padding: 8px;
    .arco-dropdown-list .arco-dropdown-option {
      width: 107px;
    }
    .checkbox-all {
      border-bottom: 1px solid var(--color-text-n8);
    }
    .arco-checkbox {
      padding: 6px 12px;
      line-height: 24px;
    }
    .arco-switch {
      margin-left: 8px;
    }
  }
  .api-expend {
    :deep(.arco-dropdown-option-content) {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }
  }
</style>
