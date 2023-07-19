<template>
  <MsCard :loading="loading" :title="title" @save="beforeSave" @save-and-continue="beforeSave(true)">
    <a-form ref="formRef" :model="form" layout="vertical">
      <a-form-item
        :label="t('system.resourcePool.name')"
        field="name"
        :rules="[{ required: true, message: t('system.resourcePool.nameRequired') }]"
        class="form-item"
        asterisk-position="end"
      >
        <a-input
          v-model:model-value="form.name"
          :placeholder="t('system.resourcePool.namePlaceholder')"
          :max-length="250"
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('system.resourcePool.desc')" field="description" class="form-item">
        <a-textarea
          v-model:model-value="form.description"
          :placeholder="t('system.resourcePool.descPlaceholder')"
          :max-length="250"
        ></a-textarea>
      </a-form-item>
      <a-form-item :label="t('system.resourcePool.serverUrl')" field="serverUrl" class="form-item">
        <a-input
          v-model:model-value="form.serverUrl"
          :placeholder="t('system.resourcePool.rootUrlPlaceholder')"
          :max-length="250"
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('system.resourcePool.orgRange')" field="orgType" class="form-item">
        <a-radio-group v-model:model-value="form.orgType">
          <a-radio value="allOrg">
            {{ t('system.resourcePool.orgAll') }}
            <a-tooltip :content="t('system.resourcePool.orgRangeTip')" position="top" mini>
              <icon-question-circle class="text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
            </a-tooltip>
          </a-radio>
          <a-radio value="set">{{ t('system.resourcePool.orgSetup') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="isSpecifiedOrg"
        :label="t('system.resourcePool.orgSelect')"
        class="form-item"
        field="testResourceDTO.orgIds"
        :rules="[{ required: true, message: t('system.resourcePool.orgRequired') }]"
        asterisk-position="end"
      >
        <a-select
          v-model="form.testResourceDTO.orgIds"
          :placeholder="t('system.resourcePool.orgPlaceholder')"
          multiple
          allow-clear
        >
          <a-option v-for="org of orgOptons" :key="org.id" :value="org.value">{{ org.label }}</a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        :label="t('system.resourcePool.use')"
        field="use"
        class="form-item"
        :rules="[{ required: true, message: t('system.resourcePool.useRequired') }]"
        asterisk-position="end"
      >
        <a-checkbox-group v-model:model-value="form.use">
          <a-checkbox v-for="use of useList" :key="use.value" :value="use.value">{{ t(use.label) }}</a-checkbox>
        </a-checkbox-group>
      </a-form-item>
      <template v-if="isCheckedPerformance">
        <a-form-item :label="t('system.resourcePool.mirror')" field="testResourceDTO.loadTestImage" class="form-item">
          <a-input
            v-model:model-value="form.testResourceDTO.loadTestImage"
            :placeholder="t('system.resourcePool.mirrorPlaceholder')"
            :max-length="250"
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.resourcePool.testHeap')" field="testResourceDTO.loadTestHeap" class="form-item">
          <a-input
            v-model:model-value="form.testResourceDTO.loadTestHeap"
            :placeholder="t('system.resourcePool.testHeapPlaceholder')"
            :max-length="250"
          ></a-input>
          <div class="mt-[4px] text-[12px] text-[var(--color-text-4)]">
            {{ t('system.resourcePool.testHeapExample', { heap: defaultHeap }) }}
            <MsIcon
              type="icon-icon_corner_right_up"
              class="cursor-pointer text-[rgb(var(--primary-6))]"
              @click="fillHeapByDefault"
            ></MsIcon>
          </div>
        </a-form-item>
      </template>

      <template v-if="isCheckedUI">
        <a-form-item
          :label="t('system.resourcePool.uiGrid')"
          field="testResourceDTO.uiGrid"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.uiGridRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.uiGrid"
            :placeholder="t('system.resourcePool.uiGridPlaceholder')"
            :max-length="250"
          ></a-input>
          <div class="mt-[4px] text-[12px] text-[var(--color-text-4)]">
            {{ t('system.resourcePool.uiGridExample', { grid: defaultGrid }) }}
          </div>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.girdConcurrentNumber')"
          field="girdConcurrentNumber"
          class="form-item"
        >
          <a-input-number
            v-model:model-value="form.testResourceDTO.girdConcurrentNumber"
            :min="1"
            :max="9999999"
            :step="1"
            mode="button"
            class="w-[160px]"
          ></a-input-number>
        </a-form-item>
      </template>

      <a-form-item v-if="isShowTypeItem" :label="t('system.resourcePool.type')" field="type" class="form-item">
        <a-radio-group v-model:model-value="form.type" type="button" @change="changeResourceType">
          <a-radio value="Node">Node</a-radio>
          <a-radio value="Kubernetes">Kubernetes</a-radio>
        </a-radio-group>
      </a-form-item>
      <template v-if="isShowNodeResources">
        <a-form-item field="addType" class="form-item">
          <template #label>
            <div class="flex items-center">
              {{ t('system.resourcePool.addResource') }}
              <a-tooltip :content="t('system.resourcePool.changeAddTypeTip')" position="tl" mini>
                <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
              </a-tooltip>
            </div>
          </template>
          <a-popconfirm
            v-if="!getIsVisited()"
            class="ms-pop-confirm"
            position="br"
            :ok-text="t('system.resourcePool.batchAddTipConfirm')"
            @popup-visible-change="handlePopChange"
          >
            <template #cancel-text>
              <div> </div>
            </template>
            <template #content>
              <div class="font-semibold text-[var(--color-text-1)]">
                {{ t('system.resourcePool.changeAddTypePopTitle') }}
              </div>
              <div class="mt-[8px] w-[290px] text-[12px] text-[var(--color-text-2)]">
                {{ t('system.resourcePool.changeAddTypeTip') }}
              </div>
            </template>
            <a-radio-group v-model:model-value="form.addType" type="button" @change="handleTypeChange">
              <a-radio value="single">{{ t('system.resourcePool.singleAdd') }}</a-radio>
              <a-radio value="multiple">{{ t('system.resourcePool.batchAdd') }}</a-radio>
            </a-radio-group>
          </a-popconfirm>
          <a-radio-group v-else v-model:model-value="form.addType" type="button" @change="handleTypeChange">
            <a-radio value="single">{{ t('system.resourcePool.singleAdd') }}</a-radio>
            <a-radio value="multiple">{{ t('system.resourcePool.batchAdd') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <MsBatchForm
          v-show="form.addType === 'single'"
          ref="batchFormRef"
          :models="batchFormModels"
          form-mode="create"
          add-text="system.resourcePool.addResource"
          :default-vals="defaultVals"
          max-height="250px"
        ></MsBatchForm>
        <!-- TODO:代码编辑器懒加载 -->
        <div v-show="form.addType === 'multiple'">
          <MsCodeEditor v-model:model-value="editorContent" width="100%" height="400px" theme="MS-text">
            <template #title>
              <a-form-item
                :label="t('system.resourcePool.batchAddResource')"
                asterisk-position="end"
                class="hide-wrapper mb-0"
                required
              >
              </a-form-item>
            </template>
          </MsCodeEditor>
          <div class="mb-[24px] text-[12px] text-[var(--color-text-4)]">
            {{ t('system.resourcePool.nodeConfigEditorTip') }}
          </div>
        </div>
      </template>
      <template v-else-if="isShowK8SResources">
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.ip')"
          field="testResourceDTO.ip"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.ipRequired') }]"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.ip"
            :placeholder="t('system.resourcePool.testResourceDTO.ipPlaceholder')"
            :max-length="250"
          ></a-input>
          <div class="mt-[4px] text-[12px] text-[var(--color-text-4)]">
            {{ t('system.resourcePool.testResourceDTO.ipSubTip', { ip: '100.0.0.100', domain: 'example.com' }) }}
          </div>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.token')"
          field="testResourceDTO.token"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.tokenRequired') }]"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.token"
            :placeholder="t('system.resourcePool.testResourceDTO.tokenPlaceholder')"
            :max-length="250"
          ></a-input>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.nameSpaces')"
          field="testResourceDTO.nameSpaces"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.nameSpacesRequired') }]"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.nameSpaces"
            :placeholder="t('system.resourcePool.testResourceDTO.nameSpacesPlaceholder')"
            :max-length="250"
            class="mr-[8px] flex-1"
          ></a-input>
          <a-tooltip
            :content="t('system.resourcePool.testResourceDTO.downloadRoleYamlTip')"
            :disabled="isFillNameSpaces"
          >
            <span>
              <a-button type="outline" :disabled="!isFillNameSpaces" @click="downloadYaml('role')">
                {{ t('system.resourcePool.testResourceDTO.downloadRoleYaml') }}
              </a-button>
            </span>
          </a-tooltip>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.apiTestImage')"
          field="testResourceDTO.apiTestImage"
          class="form-item"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.apiTestImage"
            :placeholder="t('system.resourcePool.testResourceDTO.apiTestImagePlaceholder')"
            :max-length="250"
          ></a-input>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.deployName')"
          field="testResourceDTO.deployName"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.deployNameRequired') }]"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.deployName"
            :placeholder="t('system.resourcePool.testResourceDTO.deployNamePlaceholder')"
            :max-length="250"
            class="mr-[8px] flex-1"
          ></a-input>
          <a-tooltip
            :content="t('system.resourcePool.testResourceDTO.downloadDeployYamlTip')"
            :disabled="isFillNameSpacesAndDeployName"
          >
            <span>
              <a-dropdown :popup-max-height="false" @select="downloadYaml($event as YamlType)">
                <a-button type="outline" :disabled="!isFillNameSpacesAndDeployName">
                  {{ t('system.resourcePool.testResourceDTO.downloadRoleYaml') }}<icon-down />
                </a-button>
                <template #content>
                  <a-doption value="DaemonSet">DaemonSet.yml</a-doption>
                  <a-doption value="Deployment">Deployment.yml</a-doption>
                </template>
              </a-dropdown>
            </span>
          </a-tooltip>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.concurrentNumber')"
          field="testResourceDTO.concurrentNumber"
          class="form-item"
        >
          <a-input-number
            v-model:model-value="form.testResourceDTO.concurrentNumber"
            :min="1"
            :max="9999999"
            :step="1"
            mode="button"
            class="w-[160px]"
          ></a-input-number>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.podThreads')"
          field="testResourceDTO.podThreads"
          class="form-item"
        >
          <a-input-number
            v-model:model-value="form.testResourceDTO.podThreads"
            :min="1"
            :max="9999999"
            :step="1"
            mode="button"
            class="w-[160px]"
          ></a-input-number>
        </a-form-item>
      </template>
    </a-form>
    <template #footerLeft>
      <a-button v-if="isCheckedPerformance" type="text" @click="showJobDrawer = true">
        {{ t('system.resourcePool.customJobTemplate') }}
        <a-tooltip :content="t('system.resourcePool.jobTemplateTip')" position="tl" mini>
          <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
        </a-tooltip>
      </a-button>
    </template>
  </MsCard>
  <JobTemplateDrawer v-model:visible="showJobDrawer" :value="form.testResourceDTO.jobDefinition" />
</template>

<script setup lang="ts">
  import { computed, Ref, ref, watchEffect } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message, FormInstance, SelectOptionData } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBatchForm from '@/components/bussiness/ms-batch-form/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import JobTemplateDrawer from './components/jobTemplateDrawer.vue';
  import { getYaml, YamlType, job } from './template';
  import { downloadStringFile, sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { addPool, getPoolInfo } from '@/api/modules/setting/resourcePool';

  import type { MsBatchFormInstance, FormItemModel } from '@/components/bussiness/ms-batch-form/types';
  import type { AddResourcePoolParams, NodesListItem } from '@/models/setting/resourcePool';

  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();
  const title = ref('');
  const loading = ref(false);
  const defaultForm = {
    id: '',
    name: '',
    enable: true,
    description: '',
    serverUrl: '',
    orgType: 'allOrg',
    use: ['performance', 'API'],
    type: 'Node',
    addType: 'single',
    testResourceDTO: {
      loadTestImage: '',
      loadTestHeap: '',
      uiGrid: '',
      girdConcurrentNumber: 1,
      podThreads: 1,
      concurrentNumber: 1,
      nodesList: [] as NodesListItem[],
      ip: '',
      token: '',
      nameSpaces: '',
      jobDefinition: job,
      apiTestImage: '',
      deployName: '',
      orgIds: [] as string[],
    },
  };
  const form = ref({ ...defaultForm });
  const formRef = ref<FormInstance | null>(null);
  const orgOptons = ref<SelectOptionData>([]);
  const useList = [
    {
      label: 'system.resourcePool.usePerformance',
      value: 'performance',
    },
    {
      label: 'system.resourcePool.useAPI',
      value: 'API',
    },
    {
      label: 'system.resourcePool.useUI',
      value: 'UI',
    },
  ];
  const defaultGrid = 'http://selenium-hub:4444';

  async function initPoolInfo() {
    try {
      loading.value = true;
      const res = await getPoolInfo(route.query.id);
      const { testResourceDTO } = res;
      const { girdConcurrentNumber, podThreads, concurrentNumber } = testResourceDTO;
      form.value = {
        ...res,
        addType: 'single',
        orgType: res.allOrg ? 'allOrg' : 'set',
        use: [res.loadTest ? 'performance' : '', res.apiTest ? 'API' : '', res.uiTest ? 'UI' : ''].filter((e) => e),
        testResourceDTO: {
          ...testResourceDTO,
          girdConcurrentNumber: girdConcurrentNumber || 1,
          podThreads: podThreads || 1,
          concurrentNumber: concurrentNumber || 1,
        },
      };
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const isEdit = ref(false);
  watchEffect(() => {
    // 判断当前是编辑还是新增
    if (route.query.id) {
      title.value = t('menu.settings.system.resourcePoolEdit');
      isEdit.value = true;
      initPoolInfo();
    } else {
      title.value = t('menu.settings.system.resourcePoolDetail');
      isEdit.value = false;
    }
  });

  const defaultHeap = '-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m';
  function fillHeapByDefault() {
    form.value.testResourceDTO.loadTestHeap = defaultHeap;
  }

  const visitedKey = 'changeAddResourceType';
  const { addVisited, getIsVisited } = useVisit(visitedKey);

  /**
   * 切换类型提示确认框隐藏时，设置已访问标志
   * @param visible 显示/隐藏
   */
  function handlePopChange(visible: boolean) {
    if (!visible) {
      addVisited();
    }
  }

  /**
   * 控制表单项显示隐藏逻辑计算器
   */
  // 是否选择应用组织为指定组织
  const isSpecifiedOrg = computed(() => form.value.orgType === 'set');
  // 是否勾选了性能测试
  const isCheckedPerformance = computed(() => form.value.use.includes('performance'));
  // 是否勾选了UI测试
  const isCheckedUI = computed(() => form.value.use.includes('UI'));
  // 是否勾选了接口测试
  const isCheckedAPI = computed(() => form.value.use.includes('API'));
  // 是否显示类型切换表单项
  const isShowTypeItem = computed(() => ['API', 'performance'].some((s) => form.value.use.includes(s)));
  // 是否显示Node资源配置信息
  const isShowNodeResources = computed(() => form.value.type === 'Node' && isShowTypeItem.value);
  // 是否显示K8S资源配置信息
  const isShowK8SResources = computed(() => form.value.type === 'Kubernetes' && isShowTypeItem.value);
  // 是否填写了命名空间
  const isFillNameSpaces = computed(() => form.value.testResourceDTO.nameSpaces?.trim() !== '');
  // 是否填写了命名空间及Deploy Name
  const isFillNameSpacesAndDeployName = computed(
    () => isFillNameSpaces.value && form.value.testResourceDTO.deployName?.trim() !== ''
  );

  const batchFormRef = ref<MsBatchFormInstance | null>(null);
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      filed: 'ip',
      type: 'input',
      label: 'system.resourcePool.ip',
      rules: [{ required: true, message: t('system.resourcePool.ipRequired') }],
      placeholder: 'system.resourcePool.ipPlaceholder',
    },
    {
      filed: 'port',
      type: 'input',
      label: 'system.resourcePool.port',
      rules: [{ required: true, message: t('system.resourcePool.portRequired') }],
      placeholder: 'system.resourcePool.portPlaceholder',
    },
    {
      filed: 'monitor',
      type: 'input',
      label: 'system.resourcePool.monitor',
      rules: [{ required: true, message: t('system.resourcePool.monitorRequired') }],
      placeholder: 'system.resourcePool.monitorPlaceholder',
    },
    {
      filed: 'concurrentNumber',
      type: 'inputNumber',
      label: 'system.resourcePool.concurrentNumber',
      rules: [
        { required: true, message: t('system.resourcePool.concurrentNumberRequired') },
        {
          validator: (val, cb) => {
            if (val <= 0) {
              cb(t('system.resourcePool.concurrentNumberMin'));
            }
          },
        },
      ],
      placeholder: 'system.resourcePool.concurrentNumberPlaceholder',
      min: 1,
      max: 9999999,
    },
  ]);

  // 动态表单默认值
  const defaultVals = computed(() => {
    const { nodesList } = form.value.testResourceDTO;
    return nodesList.map((node) => node);
  });

  // 代码编辑器内容
  const editorContent = ref('');

  // 代码编辑器内容根据动态表单的内容拼接
  watchEffect(() => {
    const { nodesList } = form.value.testResourceDTO;
    let res = '';
    for (let i = 0; i < nodesList.length; i++) {
      const node = nodesList[i];
      // 按顺序拼接：ip、port、monitor、concurrentNumber
      if (Object.values(node).every((e) => e !== '')) {
        res += `${node.ip},${node.port},${node.monitor},${node.concurrentNumber}\r`;
      }
    }
    editorContent.value = res;
  });

  /**
   * 提取动态表单项输入的内容
   */
  function setBatchFormRes() {
    const res = batchFormRef.value?.getFormResult<NodesListItem>();
    if (res?.length) {
      form.value.testResourceDTO.nodesList = res.map((e) => e);
    }
  }

  /**
   * 解析代码编辑器内容
   */
  function analyzeCode() {
    const arr = editorContent.value.split('\r'); // 以回车符分割
    // 将代码编辑器内写的内容抽取出来
    arr.forEach((e, i) => {
      e = e.replaceAll('\n', ''); // 去除换行符
      if (e.trim() !== '') {
        // 排除空串
        const line = e.split(',');
        if (line.every((s) => s.trim() !== '') && !Number.isNaN(Number(line[3]))) {
          const item = {
            ip: line[0],
            port: line[1],
            monitor: line[2],
            concurrentNumber: Number(line[3]),
          };
          if (i === 0) {
            // 第四个是concurrentNumber，需要是数字
            form.value.testResourceDTO.nodesList = [item];
          } else {
            form.value.testResourceDTO.nodesList.push(item);
          }
        }
      }
    });
  }

  /**
   * 切换资源添加类型
   * @param val 切换的类型
   */
  function handleTypeChange(val: string | number | boolean) {
    if (val === 'single') {
      // 从批量添加切换至单个添加，需要解析代码编辑器内容
      analyzeCode();
    } else if (val === 'multiple') {
      // 从单个添加切换到批量添加，需要先提取组件的输入框内容
      setBatchFormRes();
    }
  }

  function changeResourceType(val: string | number | boolean) {
    if (val === 'Kubernetes') {
      setBatchFormRes();
    }
  }

  const apiImageTag = ref('dev');
  /**
   * 下载 yaml 文件
   * @param type 文件类型
   */
  function downloadYaml(type: YamlType) {
    let name = '';
    let yamlStr = '';
    const { nameSpaces, deployName, apiTestImage } = form.value.testResourceDTO;
    let apiImage = `registry.cn-qingdao.aliyuncs.com/metersphere/node-controller:${apiImageTag.value}`;
    if (apiTestImage) {
      apiImage = apiTestImage;
    }
    switch (type) {
      case 'role':
        name = 'Role.yml';
        yamlStr = getYaml('role', '', nameSpaces, '');
        break;
      case 'Deployment':
        name = 'Deployment.yml';
        yamlStr = getYaml('Deployment', deployName, nameSpaces, apiImage);
        break;
      case 'DaemonSet':
        name = 'Daemonset.yml';
        yamlStr = getYaml('DaemonSet', deployName, nameSpaces, apiImage);
        break;
      default:
        throw new Error('文件类型不在可选范围');
    }
    downloadStringFile('text/yaml', yamlStr, name);
  }

  const showJobDrawer = ref(false);
  const isContinueAdd = ref(false);

  /**
   * 重置表单信息
   */
  function resetForm() {
    form.value = { ...defaultForm };
  }

  /**
   * 拼接添加资源池参数
   */
  function makeResourcePoolParams(): AddResourcePoolParams {
    const { type, testResourceDTO } = form.value;
    const {
      ip,
      token, // k8s token
      nameSpaces, // k8s 命名空间
      concurrentNumber, // k8s 最大并发数
      podThreads, // k8s 单pod最大线程数
      jobDefinition, // k8s job自定义模板
      apiTestImage, // k8s api测试镜像
      deployName, // k8s api测试部署名称
      nodesList,
      loadTestImage,
      loadTestHeap,
      uiGrid,
      girdConcurrentNumber,
    } = testResourceDTO;
    // Node资源
    const nodeResourceDTO = {
      nodesList: type === 'Node' ? nodesList : [],
    };
    // K8S资源
    const k8sResourceDTO =
      type === 'Kubernetes'
        ? {
            ip,
            token,
            nameSpaces,
            concurrentNumber,
            podThreads,
            jobDefinition,
            apiTestImage,
            deployName,
          }
        : {};
    // 性能测试资源
    const performanceDTO = isCheckedPerformance.value
      ? {
          loadTestImage,
          loadTestHeap,
          ...nodeResourceDTO,
          ...k8sResourceDTO,
        }
      : {};
    // 接口测试资源
    const apiDTO = isCheckedAPI.value
      ? {
          ...nodeResourceDTO,
          ...k8sResourceDTO,
        }
      : {};
    // ui 测试资源
    const uiDTO = isCheckedUI.value
      ? {
          uiGrid,
          girdConcurrentNumber,
        }
      : {};
    return {
      ...form.value,
      type: isShowTypeItem.value ? form.value.type : '',
      allOrg: form.value.orgType === 'allOrg',
      apiTest: form.value.use.includes('API'), // 是否支持api测试
      loadTest: form.value.use.includes('performance'), // 是否支持性能测试
      uiTest: form.value.use.includes('UI'), // 是否支持ui测试
      testResourceDTO: { ...performanceDTO, ...apiDTO, ...uiDTO, orgIds: form.value.testResourceDTO.orgIds },
    };
  }

  async function save() {
    try {
      loading.value = true;
      const params = makeResourcePoolParams();
      await addPool(params);
      Message.success(t('system.resourcePool.addSuccess'));
      if (isContinueAdd.value) {
        resetForm();
      } else {
        await sleep(300);
        router.push({ name: 'settingSystemResourcePool' });
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 校验批量添加的资源信息
   * @param cb 校验通过后的回调函数
   */
  function validateBtachNodes(cb: () => void) {
    if (
      form.value.testResourceDTO.nodesList.some((e) => {
        return Object.values(e).every((v) => v !== '') && e.concurrentNumber > 0;
      }) &&
      typeof cb === 'function'
    ) {
      cb();
    } else {
      setTimeout(() => {
        scrollIntoView(document.querySelector('.ms-code-editor'), { block: 'center' });
      }, 0);
      Message.error(t('system.resourcePool.nodeResourceRequired'));
    }
  }

  function beforeSave(isContinue = false) {
    isContinueAdd.value = isContinue;
    formRef.value?.validate().then((res) => {
      if (!res) {
        // 整个表单校验，除了批量动态的表单项
        if (isShowNodeResources.value) {
          // 如果显示node 资源，需要对批量动态表单项进行校验
          if (form.value.addType === 'single') {
            // node 资源单个添加时，调用批量表单的校验
            return batchFormRef.value?.formValidate((batchRes: any) => {
              form.value.testResourceDTO.nodesList = batchRes;
              save();
            });
          }
          // node 资源批量添加时，先将代码编辑器的值解析到表单对象中，再校验
          analyzeCode();
          validateBtachNodes(save);
          return false;
        }
        return save();
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }
</script>

<style lang="less" scoped>
  .form-item {
    width: 732px;
  }
  :deep(.hide-wrapper) {
    .arco-form-item-wrapper-col {
      @apply hidden;
    }
    .arco-form-item-label-col {
      @apply mb-0;
    }
  }
</style>
@/models/setting/resourcePool @/api/modules/setting/resourcePool
