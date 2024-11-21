<template>
  <MsDrawer
    :width="891"
    :visible="visible"
    unmount-on-close
    destroy-on-close
    @confirm="handleAddOrUpdate"
    @cancel="handleCancel"
  >
    <template #title>
      <div>{{ title }}</div>
    </template>
    <a-form ref="httpRef" layout="vertical" :model="form">
      <a-form-item
        class="mb-[16px]"
        asterisk-position="end"
        field="hostname"
        :disabled="store.currentEnvDetailInfo.mock"
        :label="t('project.environmental.http.hostName')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <!-- <a-input
          v-model="form.hostname"
          class="w-[100%]"
          :max-length="255"
          :placeholder="t('project.environmental.http.hostNamePlaceholder')"
        >
          <template #prefix>
            <div class="input-prefix"> http:// </div>
            <a-select :options="['http://', 'https://']" />
          </template>
        </a-input> -->

        <a-input-group class="w-full">
          <a-select
            v-model="form.protocol"
            :style="{ width: '160px' }"
            default-value="http"
            :disabled="store.currentEnvDetailInfo.mock"
          >
            <a-option value="http">http://</a-option>
            <a-option value="https">https://</a-option>
          </a-select>
          <a-input
            v-model="form.hostname"
            class="w-full"
            :max-length="255"
            :placeholder="
              hostType === 'http://'
                ? t('project.environmental.http.httpHostNamePlaceholder')
                : t('project.environmental.http.httpsHostNamePlaceholder')
            "
          />
        </a-input-group>
      </a-form-item>
      <a-form-item class="mb-[16px]" field="type" :label="t('project.environmental.http.enableCondition')">
        <a-select v-model:model-value="form.type">
          <a-option value="NONE">{{ t('project.environmental.http.none') }}</a-option>
          <a-option value="MODULE">{{ t('project.environmental.http.module') }}</a-option>
          <a-option value="PATH">{{ t('project.environmental.http.path') }}</a-option>
        </a-select>
      </a-form-item>
      <!-- 展示模块 -->
      <a-form-item
        v-if="form.type === 'MODULE'"
        class="mb-[16px]"
        field="moduleId"
        :label="t('project.environmental.http.selectApiModule')"
        :rules="[{ required: true, message: t('project.environmental.http.selectModule') }]"
        asterisk-position="end"
      >
        <MsTreeSelect
          v-model:model-value="form.moduleId"
          v-model:data="envTree"
          allow-clear
          :multiple="true"
          tree-check-strictly
          :tree-checkable="true"
          show-contain-child-module
          :placeholder="t('common.pleaseSelect')"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
        />
      </a-form-item>
      <!-- 路径 -->
      <a-form-item
        v-if="showPathInput"
        class="mb-[16px]"
        asterisk-position="end"
        field="path"
        :label="t('project.environmental.http.path')"
        :rules="[{ required: true, message: t('project.environmental.http.pathRequired') }]"
      >
        <a-input-group class="w-full">
          <a-select v-model="form.condition" :style="{ width: '160px' }" default-value="CONTAINS">
            <a-option v-for="item in OPERATOR_MAP" :key="item.value" :value="item.value">{{ t(item.label) }}</a-option>
          </a-select>
          <a-input
            v-model="form.path"
            class="w-full"
            :max-length="255"
            :placeholder="t('project.environmental.http.pathPlaceholder')"
          />
        </a-input-group>
      </a-form-item>
      <a-form-item class="mb-[16px]" field="description" :label="t('common.desc')">
        <a-textarea v-model="form.description" :max-length="1000" auto-size :placeholder="t('common.pleaseInput')" />
      </a-form-item>
      <!-- 选择UI测试模块 -->
      <!-- <a-form-item
        v-if="showUIModule"
        class="mb-[16px]"
        field="enableCondition"
        asterisk-position="end"
        :label="t('project.environmental.http.uiModuleSelect')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <a-select v-model:model-value="form.uiModule" multiple :placeholder="t('common.pleaseSelect')">
          <a-option value="none">{{ t('project.environmental.http.none') }}</a-option>
        </a-select>
      </a-form-item> -->
      <httpHeader
        v-model:params="form.headers"
        :layout="activeLayout"
        :disabled-param-value="false"
        :disabled-except-param="false"
        :type-title="t('project.environmental.requestHeader')"
      />
      <a-form-item class="mt-4" asterisk-position="end" field="path" :label="t('project.environmental.http.authType')">
        <a-radio-group v-model:model-value="form.authConfig.authType" class="mb-[16px]">
          <a-radio :value="RequestAuthType.NONE">No Auth</a-radio>
          <a-radio :value="RequestAuthType.BASIC">Basic Auth</a-radio>
          <a-radio :value="RequestAuthType.DIGEST">Digest Auth</a-radio>
        </a-radio-group>
        <a-form v-if="form.authConfig.authType === 'BASIC'" ref="authFormRef" :model="form" layout="vertical">
          <a-form-item :label="t('apiTestDebug.username')">
            <a-input
              v-model:model-value="form.authConfig.basicAuth.userName"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              class="w-[450px]"
              :max-length="255"
            />
          </a-form-item>
          <a-form-item :label="t('apiTestDebug.password')">
            <a-input-password
              v-model:model-value="form.authConfig.basicAuth.password"
              autocomplete="new-password"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              class="w-[450px]"
            />
          </a-form-item>
        </a-form>
        <a-form v-else-if="form.authConfig.authType == 'DIGEST'" ref="authFormRef" :model="form" layout="vertical">
          <a-form-item :label="t('apiTestDebug.username')">
            <a-input
              v-model:model-value="form.authConfig.digestAuth.userName"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              class="w-[450px]"
              :max-length="255"
            />
          </a-form-item>
          <a-form-item :label="t('apiTestDebug.password')">
            <a-input-password
              v-model:model-value="form.authConfig.digestAuth.password"
              autocomplete="new-password"
              :placeholder="t('apiTestDebug.commonPlaceholder')"
              class="w-[450px]"
            />
          </a-form-item>
        </a-form>
      </a-form-item>
    </a-form>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { Message, ValidatedError } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTreeSelect from '@/components/pure/ms-tree-select/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getEnvModules } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { findNodeByKey, getGenerateId, mapTree } from '@/utils';

  import type { SelectedModule } from '@/models/apiTest/management';
  import type { ModuleTreeNode } from '@/models/common';
  import { HttpForm } from '@/models/projectManagement/environmental';
  import { RequestAuthType } from '@/enums/apiEnum';

  const httpHeader = defineAsyncComponent(() => import('@/views/api-test/components/requestComposition/header.vue'));

  const props = defineProps<{
    currentId: string;
    isCopy: boolean;
  }>();
  const appStore = useAppStore();
  const store = useProjectEnvStore();

  const emit = defineEmits<{
    (e: 'close'): void;
  }>();

  const OPERATOR_MAP = [
    {
      value: 'CONTAINS',
      label: '包含',
    },
    {
      value: 'EQUALS',
      label: '等于',
    },
  ];

  const initForm: HttpForm = {
    id: '',
    hostname: '',
    type: 'NONE',
    headers: [],
    path: '',
    condition: 'CONTAINS',
    description: '',
    url: '',
    protocol: 'http',
    moduleId: [],
    moduleMatchRule: {
      modules: [],
    },
    pathMatchRule: {
      path: '',
      condition: '',
    },
    authConfig: {
      authType: 'NONE',
      basicAuth: {
        userName: '',
        password: '',
        valid: true,
      },
      digestAuth: {
        userName: '',
        password: '',
        valid: true,
      },
    },
  };

  const form = ref<HttpForm>({ ...initForm });
  const hostType = ref<string>('http://');
  const activeLayout = ref<'horizontal' | 'vertical'>('vertical');

  const httpRef = ref();

  const showPathInput = computed(() => form.value.type === 'PATH');

  const visible = defineModel('visible', { required: true, type: Boolean, default: false });

  const envTree = ref<MsTreeNodeData[]>([]);
  const moduleTree = ref<MsTreeNodeData[]>([]);

  const { t } = useI18n();

  function resetForm() {
    form.value = { ...initForm };
  }

  const handleAddOrUpdate = () => {
    httpRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        const index = store.currentEnvDetailInfo.config.httpConfig.findIndex((item) => item.id === form.value.id);
        let modules: { moduleId: string; containChildModule: boolean }[] = [];
        const { protocol, hostname, condition, path } = form.value;
        if (form.value.type === 'MODULE') {
          modules = form.value.moduleId.map((item) => {
            return {
              moduleId: item,
              containChildModule: findNodeByKey<MsTreeNodeData>(envTree.value, item, 'id')?.containChildModule ?? false,
            };
          });
        }

        // 判断是否已存在type为NONE的数据 如果存在则不允许添加 数量大于1 则提示
        const noneData = store.currentEnvDetailInfo.config.httpConfig.filter((item) => item.type === 'NONE');
        if (noneData.length >= 1 && (props.isCopy || !props.currentId) && form.value.type === 'NONE') {
          Message.error(t('project.environmental.http.noneDataExist'));
          return;
        }
        // 编辑
        if (index > -1 && !props.isCopy) {
          const httpItem = {
            ...form.value,
            url: `${protocol}://${hostname}`,
            pathMatchRule: {
              path,
              condition,
            },
            order: store.currentEnvDetailInfo.config.httpConfig.length + 1,
            moduleMatchRule: { modules },
          };
          store.currentEnvDetailInfo.config.httpConfig.splice(index, 1, httpItem);
          // 复制
        } else if (index > -1 && props.isCopy) {
          const insertItem = {
            ...form.value,
            id: getGenerateId(),
            url: `${protocol}://${hostname}`,
            order: store.currentEnvDetailInfo.config.httpConfig.length + 1,
            moduleMatchRule: { modules },
          };
          store.currentEnvDetailInfo.config.httpConfig.splice(index + 1, 0, insertItem);
          // 添加
        } else {
          const httpItem = {
            ...form.value,
            url: `${protocol}://${hostname}`,
            pathMatchRule: {
              path,
              condition,
            },
            id: getGenerateId(),
            order: store.currentEnvDetailInfo.config.httpConfig.length + 1,
            moduleMatchRule: { modules },
          };
          store.currentEnvDetailInfo.config.httpConfig.push(httpItem);
        }
        emit('close');
        resetForm();
      }
    });
  };

  const title = ref<string>('');

  async function initModuleTree(selectedModules?: SelectedModule[]) {
    try {
      const res = await getEnvModules({
        projectId: appStore.currentProjectId,
        selectedModules,
      });
      moduleTree.value = res.moduleTree;
      store.currentEnvDetailInfo.config.httpConfig.forEach((item) => {
        if (item.id === props.currentId) {
          item.moduleMatchRule.modules = res.selectedModules;
        }
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  async function initHttpDetail() {
    title.value = props.currentId ? t('project.environmental.http.edit') : t('project.environmental.http.add');
    if (props.isCopy) {
      title.value = t('project.environmental.http.copy');
    }
    if (props.currentId) {
      const currentItem = store.currentEnvDetailInfo.config.httpConfig.find(
        (item) => item.id === props.currentId
      ) as HttpForm;
      await initModuleTree(currentItem.moduleMatchRule.modules);
      envTree.value = mapTree<ModuleTreeNode>(moduleTree.value, (node) => {
        return {
          ...node,
          containChildModule:
            currentItem.moduleMatchRule.modules.find((item) => item.moduleId === node.id)?.containChildModule ?? false,
          disabled: !!node.parent?.containChildModule,
        };
      });
      if (currentItem) {
        const { path, condition } = currentItem.pathMatchRule;
        const urlPath = currentItem.url.match(/\/\/(.*)/);
        form.value = {
          ...currentItem,
          moduleId: currentItem.moduleMatchRule.modules.map((item) => item.moduleId) || [],
          path,
          condition,
          hostname: urlPath && urlPath?.length > 1 ? `${urlPath[1]}` : '',
        };
      }
    } else {
      await initModuleTree();
      envTree.value = moduleTree.value;
      resetForm();
    }
  }

  watch(
    () => visible.value,
    (val) => {
      if (val) {
        initHttpDetail();
      }
    }
  );

  watch(
    () => form.value.moduleId,
    (newValue) => {
      if (newValue) {
        httpRef.value?.validateField('moduleId');
      }
    }
  );

  const handleCancel = () => {
    visible.value = false;
    resetForm();
  };
</script>

<style lang="less" scoped>
  .input-prefix {
    position: relative;
    right: 10px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-top: 1px solid var(--color-text-7);
    border-bottom: 1px solid var(--color-text-7);
    border-left: 1px solid var(--color-text-7);
    border-radius: 4px 0 0 4px;
    background: var(--color-text-n8);
    flex-direction: column;
  }
  :deep(.arco-input-wrapper) {
    .arco-input-prefix {
      padding-right: 0;
    }
  }
  .path-input {
    display: inline;
    :deep(.arco-input-wrapper) {
      padding-right: 0;
      :focus-within {
        border-color: rgb(var(--primary-7));
        border-top: none;
        border-bottom: none;
        border-left: none;
      }
      .arco-input.arco-input-size-small {
        padding: 0;
      }
    }
    :deep(.arco-input-wrapper:not(:disabled):hover) {
      border-color: rgb(var(--primary-7));
      background-color: var(--color-text-n10);
    }
    :deep(.arco-select) {
      border: 1px solid var(--color-text-n7);
      border-radius: 0 4px 4px 0;
      background: var(--color-text-n8);
    }
    :deep(.arco-select-focused) {
      border-color: rgb(var(--primary-7));
    }
    :deep(.arco-select-view-single) {
      padding: 5px 8px;
      .arco-select-view-value {
        padding-top: 0;
        padding-bottom: 0;
        height: 22px;
        min-height: 22px;
        line-height: 22px;
      }
    }
  }
</style>
