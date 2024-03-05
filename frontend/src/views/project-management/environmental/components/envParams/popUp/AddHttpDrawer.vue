<template>
  <MsDrawer
    :width="891"
    :visible="visible"
    unmount-on-close
    :mask="false"
    @confirm="handleAddOrUpdate"
    @cancel="emit('close')"
  >
    <template #title>
      <div>{{ title }}</div>
    </template>
    <a-form ref="httpRef" layout="vertical" :model="form">
      <a-form-item
        class="mb-[16px]"
        asterisk-position="end"
        field="hostname"
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
          <a-select v-model="form.protocol" :style="{ width: '160px' }" default-value="http">
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
      <!-- 接口模块选择 -->
      <!-- <a-form-item
        v-if="showApiModule"
        class="mb-[16px]"
        field="apiModule"
        asterisk-position="end"
        :label="t('project.environmental.http.apiModuleSelect')"
        :rules="[{ required: true, message: t('project.environmental.http.hostNameRequired') }]"
      >
        <a-select v-model:model-value="form.apiModule" multiple :placeholder="t('common.pleaseSelect')">
          <a-option value="none">{{ t('project.environmental.http.none') }}</a-option>
        </a-select>
      </a-form-item> -->
      <a-form-item class="mb-[16px]" field="description" :label="t('project.environmental.http.description')">
        <a-input v-model="form.description" />
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
      <!-- 展示模块 -->
      <!-- TODO 模块还没有加 -->
      <!-- <a-form-item class="mb-[16px]" field="description" :label="t('project.environmental.http.selectApiModule')">
        <ApiTree
          v-model:focus-node-key="focusNodeKey"
          :placeholder="t('project.environmental.http.selectApiModule')"
          :selected-keys="selectedKeys"
          :data="moduleTree"
          :field-names="{
            title: 'name',
            key: 'id',
            children: 'children',
            count: 'count',
          }"
          :tree-checkable="true"
          :hide-more-action="true"
        >
          <template #tree-slot-title="nodeData">
            <div class="inline-flex w-full">
              <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            </div>
          </template>
          <template #tree-slot-extra="nodeData">
            <span><MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, nodeData)" /></span>
          </template>
        </ApiTree>
      </a-form-item> -->
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
    </a-form>
    <RequestHeader v-model:params="form.headers" />
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { defineModel } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { MsTreeFieldNames, MsTreeNodeData, MsTreeSelectedData } from '@/components/business/ms-tree/types';
  import RequestHeader from '../../requestHeader/index.vue';

  // import ApiTree from './apiTree.vue';
  import { useI18n } from '@/hooks/useI18n';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { getGenerateId } from '@/utils';

  import { HttpForm } from '@/models/projectManagement/environmental';

  const props = defineProps<{
    currentId: string;
    isCopy: boolean;
  }>();

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

  const initForm = {
    id: '',
    hostname: '',
    type: 'NONE',
    headers: [],
    path: '',
    condition: 'CONTAINS',
    description: '',
    protocol: 'http',
  };

  const form = ref<HttpForm>({ ...initForm });
  const hostType = ref<string>('http://');

  const httpRef = ref();

  const showPathInput = computed(() => form.value.type === 'PATH');

  const visible = defineModel('visible', { required: true, type: Boolean, default: false });

  const { t } = useI18n();

  function resetForm() {
    form.value = { ...initForm };
  }

  const handleAddOrUpdate = () => {
    const index = store.currentEnvDetailInfo.config.httpConfig.findIndex((item) => item.id === form.value.id);
    // 编辑
    if (index > -1 && !props.isCopy) {
      store.currentEnvDetailInfo.config.httpConfig.splice(index + 1, 1, form.value);
      // 复制
    } else if (index > -1 && props.isCopy) {
      const insertItem = {
        ...form.value,
        id: getGenerateId(),
        hostname: `copy_${form.value.hostname}`,
        order: store.currentEnvDetailInfo.config.httpConfig.length + 1,
      };
      store.currentEnvDetailInfo.config.httpConfig.splice(index, 0, insertItem);
      // 添加
    } else {
      const { protocol, hostname, condition, path } = form.value;
      const httpItem = {
        ...form.value,
        hostname: `${protocol}://${hostname}`,
        pathMatchRule: {
          path,
          condition,
        },
        id: getGenerateId(),
        order: store.currentEnvDetailInfo.config.httpConfig.length + 1,
      };
      store.currentEnvDetailInfo.config.httpConfig.push(httpItem);
    }
    emit('close');
  };

  const moduleTree = ref([
    {
      id: 'root',
      name: '未规划请求',
      type: 'MODULE',
      parentId: 'NONE',
      children: [
        {
          id: '4112912223068160',
          name: '随便写的',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'OPTIONS',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1150192243335168',
          name: '文件儿',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1165379247169536',
          name: '901',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1165705664684032',
          name: '888',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2125544956010496',
          name: '0129-1',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2126988065021952',
          name: '0129-2',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2171827523477504',
          name: 'fffggg',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2297223388766208',
          name: '0129-3',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '4034709458542592',
          name: '测试一下百度',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'PATCH',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1017890070175744',
          name: 'TTTTTCCCCCPPPPP',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '864679996792832',
          name: '委托',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '867463135600640',
          name: '登入',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '868236229713920',
          name: '买入',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '927008562290689',
          name: '账号校验1',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '943707395039232',
          name: 'ddd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1068845561815040',
          name: 'TCP测试2',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'TCP',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1131706704060416',
          name: 'aaa',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '921184586637312',
          name: '读取系统日期22',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '642853526609920',
          name: 'Test',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '851760736174080',
          name: 'dd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '853891039952896',
          name: 'fasdfd',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1118186147643392',
          name: 'eeee',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1120161832599552',
          name: 'eeeeqqq',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1162149432885248',
          name: 'a',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '1177147458682880',
          name: '这是Curl导入的请求',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2226957725106176',
          name: 'test12',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2601169635672064',
          name: 'testvvvv',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '885467640184832',
          name: 'okko',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '891635213221888',
          name: 'gs',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'HTTP',
            method: 'GET',
          },
          count: 0,
          path: '/',
        },
        {
          id: '640018849742848',
          name: '0228',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
        {
          id: '2178166898065408',
          name: '0304',
          type: 'API',
          parentId: 'root',
          children: [],
          attachInfo: {
            protocol: 'SPX',
          },
          count: 0,
          path: '/',
        },
      ],
      attachInfo: {},
      count: 0,
      path: '/未规划请求',
    },
  ]);

  const moreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.copyStep',
      eventTag: 'copyStep',
    },
  ];

  const selectedKeys = ref<string[]>([]);
  const focusNodeKey = ref<string>('');

  function handleMoreActionSelect(item: ActionsItem, node: MsTreeNodeData) {}

  const title = ref<string>('');
  watchEffect(() => {
    title.value = props.currentId ? t('project.environmental.http.edit') : t('project.environmental.http.add');
    if (props.currentId) {
      const currentItem = store.currentEnvDetailInfo.config.httpConfig.find(
        (item) => item.id === props.currentId
      ) as HttpForm;
      if (currentItem) {
        form.value = {
          ...currentItem,
        };
      }
    } else {
      resetForm();
    }
  });
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
