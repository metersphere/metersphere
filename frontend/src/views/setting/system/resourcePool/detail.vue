<template>
  <MsCard
    :loading="loading"
    :title="title"
    :is-edit="isEdit"
    :save-text="isEdit ? t('common.update') : t('system.resourcePool.add')"
    :save-and-continue-text="t('system.resourcePool.addAndContinue')"
    :handle-back="handleBack"
    has-breadcrumb
    @save="beforeSave"
    @save-and-continue="beforeSave(true)"
  >
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
          :max-length="255"
          @change="() => setIsSave(false)"
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('common.desc')" field="description" class="form-item">
        <a-textarea
          v-model:model-value="form.description"
          :placeholder="t('system.resourcePool.descPlaceholder')"
          :max-length="1000"
          @change="() => setIsSave(false)"
        ></a-textarea>
      </a-form-item>
      <a-form-item :label="t('system.resourcePool.serverUrl')" field="serverUrl" class="form-item">
        <template #label>
          <div class="flex items-center">
            {{ t('system.resourcePool.serverUrl') }}
            <a-tooltip :content="t('system.resourcePool.serverUrlTip')" position="tl" mini>
              <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
            </a-tooltip>
          </div>
        </template>
        <a-input
          v-model:model-value="form.serverUrl"
          :placeholder="t('system.resourcePool.rootUrlPlaceholder')"
          :max-length="255"
          @change="() => setIsSave(false)"
        ></a-input>
      </a-form-item>
      <a-form-item :label="t('system.resourcePool.orgRange')" field="orgType" class="form-item">
        <a-radio-group v-model:model-value="form.orgType" @change="() => setIsSave(false)">
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
          @change="() => setIsSave(false)"
        >
          <a-option v-for="org of orgOptions" :key="org.id" :value="org.id">{{ org.name }}</a-option>
        </a-select>
      </a-form-item>
      <!-- <a-form-item
        :label="t('system.resourcePool.use')"
        field="use"
        class="form-item"
        :rules="[{ required: true, message: t('system.resourcePool.useRequired') }]"
        asterisk-position="end"
      >
        <a-checkbox-group v-model:model-value="form.use" @change="() => setIsSave(false)">
          <a-checkbox v-for="use of useList" :key="use.value" :value="use.value">{{ t(use.label) }}</a-checkbox>
        </a-checkbox-group>
        <MsFormItemSub
          v-if="form.use.length === 3"
          :text="t('system.resourcePool.allUseTip')"
          :show-fill-icon="false"
        />
      </a-form-item> -->
      <!--TODO:暂无性能测试-->
      <!-- <template v-if="isCheckedPerformance">
        <a-form-item :label="t('system.resourcePool.mirror')" field="testResourceDTO.loadTestImage" class="form-item">
          <a-input
            v-model:model-value="form.testResourceDTO.loadTestImage"
            :placeholder="t('system.resourcePool.mirrorPlaceholder')"
            :max-length="255"
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.resourcePool.testHeap')" field="testResourceDTO.loadTestHeap" class="form-item">
          <a-input
            v-model:model-value="form.testResourceDTO.loadTestHeap"
            :placeholder="t('system.resourcePool.testHeapPlaceholder')"
            :max-length="255"
          ></a-input>
          <MsFormItemSub
            :text="t('system.resourcePool.testHeapExample', { heap: defaultHeap })"
            @fill="fillHeapByDefault"
          />
        </a-form-item>
      </template> -->
      <!--TODO:暂无UI测试-->
      <!-- <template v-if="isCheckedUI">
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
            :max-length="255"
          ></a-input>
          <div class="mt-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
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
      </template> -->

      <a-form-item v-if="isShowTypeItem" :label="t('system.resourcePool.type')" field="type" class="form-item">
        <a-radio-group v-model:model-value="form.type" type="button" @change="changeResourceType">
          <a-radio value="Node">Node</a-radio>
          <a-radio value="Kubernetes">Kubernetes</a-radio>
        </a-radio-group>
      </a-form-item>
      <template v-if="isShowNodeResources">
        <div
          class="mb-[8px] flex w-full items-center justify-between"
          :class="`${licenseStore.hasLicense() ? '' : 'has-license-class'} form-item !w-full`"
        >
          {{ t('system.resourcePool.addResource') }}
          <a-popconfirm
            v-if="!getIsVisited()"
            v-xpack
            class="ms-pop-confirm--hidden-cancel"
            position="bl"
            popup-container="#typeRadioGroupRef"
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
              <div class="mt-[8px] w-[290px] text-[12px] leading-[16px] text-[var(--color-text-2)]">
                {{ t('system.resourcePool.changeAddTypeTip') }}
              </div>
            </template>
            <div id="typeRadioGroupRef" v-xpack class="relative">
              <a-radio-group v-model:model-value="form.addType" type="button" @change="handleTypeChange">
                <a-radio value="single">{{ t('system.resourcePool.singleAdd') }}</a-radio>
                <a-radio v-xpack value="multiple">
                  <a-tooltip :content="t('system.resourcePool.changeAddTypeTip')" position="tl" mini
                    ><div>{{ t('system.resourcePool.batchAdd') }}</div></a-tooltip
                  ></a-radio
                >
              </a-radio-group>
            </div>
          </a-popconfirm>
          <a-radio-group v-else v-model:model-value="form.addType" v-xpack type="button" @change="handleTypeChange">
            <a-radio value="single">{{ t('system.resourcePool.singleAdd') }}</a-radio>
            <a-radio v-xpack value="multiple">
              <a-tooltip :content="t('system.resourcePool.changeAddTypeTip')" position="tl" mini>
                <span>
                  {{ t('system.resourcePool.batchAdd') }}
                </span>
              </a-tooltip>
            </a-radio>
          </a-radio-group>
        </div>
        <MsBatchForm
          v-show="form.addType === 'single'"
          ref="batchFormRef"
          :models="batchFormModels"
          form-mode="create"
          add-text="system.resourcePool.addResource"
          :default-vals="defaultVals"
          :hide-add="!isXpack"
          max-height="250px"
          :add-tool-tip="licenseStore.hasLicense() ? '' : t('system.resourcePool.supportMultiResource')"
          @change="() => setIsSave(false)"
        ></MsBatchForm>
        <!-- TODO:代码编辑器懒加载 -->
        <div v-show="form.addType === 'multiple'">
          <MsCodeEditor
            v-model:model-value="editorContent"
            width="100%"
            height="400px"
            theme="MS-text"
            :show-theme-change="false"
            :show-full-screen="false"
            @change="() => setIsSave(false)"
          >
          </MsCodeEditor>
          <div class="mb-[24px] mt-[4px] text-[12px] leading-[16px] text-[rgb(var(--warning-6))]">
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
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.ip"
            :placeholder="t('system.resourcePool.testResourceDTO.ipPlaceholder')"
            :max-length="255"
            @change="() => setIsSave(false)"
          ></a-input>
          <div class="mt-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
            {{ t('system.resourcePool.testResourceDTO.ipSubTip', { ip: '100.0.0.100', domain: 'example.com' }) }}
          </div>
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.token')"
          field="testResourceDTO.token"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.tokenRequired') }]"
          asterisk-position="end"
        >
          <a-input-password
            v-model:model-value="form.testResourceDTO.token"
            :placeholder="t('system.resourcePool.testResourceDTO.tokenPlaceholder')"
            :max-length="1500"
            autocomplete="new-password"
            @change="() => setIsSave(false)"
          />
        </a-form-item>
        <a-form-item
          :label="t('system.resourcePool.testResourceDTO.namespace')"
          field="testResourceDTO.namespace"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.nameSpacesRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.namespace"
            :placeholder="t('system.resourcePool.testResourceDTO.nameSpacesPlaceholder')"
            :max-length="255"
            class="mr-[8px] flex-1"
            @change="() => setIsSave(false)"
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
          v-if="isCheckedAPI"
          :label="t('system.resourcePool.testResourceDTO.deployName')"
          field="testResourceDTO.deployName"
          class="form-item"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.deployNameRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="form.testResourceDTO.deployName"
            :placeholder="t('system.resourcePool.testResourceDTO.deployNamePlaceholder')"
            :max-length="255"
            class="mr-[8px] flex-1"
            @change="() => setIsSave(false)"
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
          field="testResourceDTO.concurrentNumber"
          :rules="[{ required: true, message: t('system.resourcePool.concurrentNumberRequired') }]"
          class="form-item"
          asterisk-position="end"
        >
          <template #label>
            <div class="inline-flex max-w-[calc(100%-12px)] items-center gap-[4px]">
              <div>{{ t('system.resourcePool.testResourceDTO.concurrentNumber') }}</div>
              <a-tooltip v-if="!isXpack" position="tl" mini>
                <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
                <template #content>
                  <div>
                    {{ t('system.resourcePool.concurrentNumberTip') }}
                    <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]" @click="goTry">
                      {{ t('system.authorized.applyTrial') }}
                    </span>
                  </div>
                </template>
              </a-tooltip>
            </div>
          </template>
          <a-input-number
            v-model:model-value="form.testResourceDTO.concurrentNumber"
            :min="1"
            :max="isXpack ? 9999999 : 10"
            :step="1"
            mode="button"
            class="w-[160px]"
            model-event="input"
            :disabled="!isXpack"
            @change="() => setIsSave(false)"
          ></a-input-number>
        </a-form-item>
        <a-form-item
          field="testResourceDTO.podThreads"
          :rules="[{ required: true, message: t('system.resourcePool.testResourceDTO.podThreadsRequired') }]"
          class="form-item"
          asterisk-position="end"
        >
          <template #label>
            <div class="inline-flex max-w-[calc(100%-12px)] items-center gap-[4px]">
              <div>{{ t('system.resourcePool.testResourceDTO.podThreads') }}</div>
              <a-tooltip v-if="!isXpack" position="tl" mini>
                <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />

                <template #content>
                  <div>
                    {{ t('system.resourcePool.testResourceDTO.podThreadsTip') }}
                    <span class="ml-2 inline-block cursor-pointer text-[rgb(var(--primary-4))]" @click="goTry">
                      {{ t('system.authorized.applyTrial') }}
                    </span>
                  </div>
                </template>
              </a-tooltip>
            </div>
          </template>
          <a-input-number
            v-model:model-value="form.testResourceDTO.podThreads"
            :min="1"
            :max="9999999"
            :step="1"
            mode="button"
            class="w-[160px]"
            model-event="input"
            :disabled="!isXpack"
            @change="() => setIsSave(false)"
          ></a-input-number>
        </a-form-item>
      </template>
    </a-form>
    <!-- <template #footerLeft>
      <MsButton v-if="isShowK8SResources" @click="showJobDrawer = true">
        {{ t('system.resourcePool.customJobTemplate') }}
        <a-tooltip :content="t('system.resourcePool.jobTemplateTip')" position="tl" mini>
          <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
        </a-tooltip>
      </MsButton>
    </template> -->
  </MsCard>
  <!-- <JobTemplateDrawer v-model:visible="showJobDrawer" v-model:value="form.testResourceDTO.jobDefinition" /> -->
</template>

<script setup lang="ts">
  /**
   * @description 系统设置-资源池详情
   */
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep, isEmpty } from 'lodash-es';

  // import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsBatchForm from '@/components/business/ms-batch-form/index.vue';
  import type { FormItemModel } from '@/components/business/ms-batch-form/types';

  // import JobTemplateDrawer from './components/jobTemplateDrawer.vue';
  import { getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import { addPool, getPoolInfo, updatePoolInfo } from '@/api/modules/setting/resourcePool';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';
  import useLicenseStore from '@/store/modules/setting/license';
  import { downloadStringFile, sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { NodesListItem, UpdateResourcePoolParams } from '@/models/setting/resourcePool';
  import { SettingRouteEnum } from '@/enums/routeEnum';

  import { getYaml, YamlType } from './template';

  const licenseStore = useLicenseStore();
  const isXpack = computed(() => licenseStore.hasLicense());
  const route = useRoute();
  const router = useRouter();
  const { t } = useI18n();
  const appStore = useAppStore();
  const { setIsSave } = useLeaveUnSaveTip();

  const title = ref('');
  const loading = ref(false);
  const defaultForm = {
    id: '',
    name: '',
    enable: true,
    description: '',
    serverUrl: '',
    orgType: 'allOrg',
    use: ['API'],
    type: 'Node',
    addType: 'single',
    testResourceDTO: {
      uiGrid: '',
      girdConcurrentNumber: 1,
      podThreads: 3,
      concurrentNumber: 10,
      singleTaskConcurrentNumber: 3,
      nodesList: [
        {
          ip: '',
          port: '',
          concurrentNumber: 10,
          singleTaskConcurrentNumber: 3,
        },
      ],
      ip: '',
      token: '',
      namespace: '',
      // jobDefinition: job,
      deployName: '',
      orgIds: [] as string[],
    },
  };
  const form = ref({ ...cloneDeep(defaultForm) });
  const formRef = ref<FormInstance | null>(null);
  const orgOptions = ref<SelectOptionData>([]);
  // TODO:第一版只有接口测试
  const useList = ref([
    // {
    //   label: 'system.resourcePool.usePerformance',
    //   value: 'performance',
    // },
    {
      label: 'system.resourcePool.useAPI',
      value: 'API',
    },
    // {
    //   label: 'system.resourcePool.useUI',
    //   value: 'UI',
    // },
  ]);
  // const defaultGrid = 'http://selenium-hub:4444';
  const maxConcurrentNumber = computed(() => {
    if (isXpack.value) {
      return 9999999;
    }
    return 10;
  });

  const maxSingleTaskConcurrentNumber = computed(() => {
    if (isXpack.value) {
      return 9999999;
    }
    return 3;
  });

  onBeforeMount(async () => {
    orgOptions.value = await getSystemOrgOption();
    if (!isXpack.value) {
      useList.value = useList.value.filter((item) => item.value === 'API');
      nextTick(() => {
        setIsSave(true); // 初始化时设置为已保存状态
      });
    }
  });

  async function initPoolInfo() {
    try {
      loading.value = true;
      const res = await getPoolInfo(route.query.id);
      const { testResourceReturnDTO } = res;
      const { girdConcurrentNumber, podThreads, concurrentNumber, orgIdNameMap, singleTaskConcurrentNumber } =
        testResourceReturnDTO;
      form.value = {
        ...res,
        addType: 'single',
        orgType: res.allOrg ? 'allOrg' : 'set',
        use: ['API', res.uiTest ? 'UI' : ''].filter((e) => e), // TODO:暂时只有接口测试
        testResourceDTO: {
          ...testResourceReturnDTO,
          girdConcurrentNumber: girdConcurrentNumber || 1,
          podThreads: podThreads || 3,
          concurrentNumber: concurrentNumber || 10,
          singleTaskConcurrentNumber: singleTaskConcurrentNumber || 3,
          orgIds: orgIdNameMap?.map((e) => e.id) || [],
        },
      };
      nextTick(() => {
        setIsSave(true); // 初始化时设置为已保存状态
      });
    } catch (error) {
      // eslint-disable-next-line no-console
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

  // const defaultHeap = '-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m';
  // function fillHeapByDefault() {
  //   form.value.testResourceDTO.loadTestHeap = defaultHeap;
  // }

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
  // 是否勾选了UI测试
  const isCheckedUI = computed(() => form.value.use.includes('UI'));
  // 是否勾选了接口测试
  const isCheckedAPI = computed(() => form.value.use.includes('API'));
  // 是否显示类型切换表单项
  const isShowTypeItem = computed(() => ['API'].some((s) => form.value.use.includes(s)));
  // 是否显示Node资源配置信息
  const isShowNodeResources = computed(() => form.value.type === 'Node' && isShowTypeItem.value);
  // 是否显示K8S资源配置信息
  const isShowK8SResources = computed(() => form.value.type === 'Kubernetes' && isShowTypeItem.value);
  // 是否填写了命名空间
  const isFillNameSpaces = computed(() => form.value.testResourceDTO.namespace?.trim() !== '');
  // 是否填写了命名空间及Deploy Name
  const isFillNameSpacesAndDeployName = computed(
    () => isFillNameSpaces.value && form.value.testResourceDTO.deployName?.trim() !== ''
  );

  // watch(
  //   () => isShowK8SResources.value,
  //   (val) => {
  //     if (val && !form.value.testResourceDTO.jobDefinition) {
  //       // 在编辑场景下，如果资源池非 k8s 的话，jobDefinition 会是 null，选中 k8s 资源的时候需要赋默认值
  //       form.value.testResourceDTO.jobDefinition = job;
  //     }
  //   }
  // );

  const batchFormRef = ref<InstanceType<typeof MsBatchForm>>();
  const batchFormModels: Ref<FormItemModel[]> = ref([
    {
      field: 'ip',
      type: 'input',
      label: 'system.resourcePool.ip',
      rules: [{ required: true, message: t('system.resourcePool.ipRequired') }],
      placeholder: 'system.resourcePool.ipPlaceholder',
    },
    {
      field: 'port',
      type: 'input',
      label: 'system.resourcePool.port',
      rules: [{ required: true, message: t('system.resourcePool.portRequired') }],
      placeholder: 'system.resourcePool.portPlaceholder',
    },
    {
      field: 'concurrentNumber',
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
      max: maxConcurrentNumber.value,
      tooltip: licenseStore.hasLicense() ? '' : t('system.resourcePool.concurrentNumberMinToolTip'),
      defaultValue: 10,
      disabled: !licenseStore.hasLicense(),
    },
    {
      field: 'singleTaskConcurrentNumber',
      type: 'inputNumber',
      label: 'system.resourcePool.singleTaskConcurrentNumber',
      rules: [
        { required: true, message: t('system.resourcePool.singleConcurrentNumberRequired') },
        {
          validator: (val, cb) => {
            if (val <= 0) {
              cb(t('system.resourcePool.singleConcurrentNumberMin'));
            }
          },
        },
      ],
      placeholder: 'system.resourcePool.singleConcurrentNumberPlaceholder',
      min: 1,
      max: maxSingleTaskConcurrentNumber.value,
      tooltip: licenseStore.hasLicense() ? '' : t('system.resourcePool.singleConcurrentNumberMinToolTip'),
      defaultValue: 3,
      disabled: !licenseStore.hasLicense(),
    },
  ]);

  // 动态表单默认值
  const defaultVals = computed(() => {
    const { nodesList } = form.value.testResourceDTO;
    return nodesList.map((node) => cloneDeep(node));
  });

  // 代码编辑器内容
  const editorContent = ref('');

  // 代码编辑器内容根据动态表单的内容拼接
  watchEffect(() => {
    const { nodesList } = form.value.testResourceDTO;
    let res = '';
    for (let i = 0; i < nodesList?.length; i++) {
      const node = nodesList[i];
      // 按顺序拼接：ip、port、monitor、concurrentNumber、singleTaskConcurrentNumber
      if (!Object.values(node).every((e) => isEmpty(e))) {
        res += `${node.ip},${node.port === undefined ? '' : node.port},${
          node.concurrentNumber === undefined ? '' : node.concurrentNumber
        },${node.singleTaskConcurrentNumber === undefined ? '' : node.singleTaskConcurrentNumber}\r`;
      }
    }
    editorContent.value = res;
  });

  /**
   * 提取动态表单项输入的内容
   */
  function setBatchFormRes() {
    const res = batchFormRef.value?.getFormResult();
    if (res?.length) {
      form.value.testResourceDTO.nodesList = res.map((e) => e) as NodesListItem[];
    }
  }

  /**
   * 解析代码编辑器内容
   */
  function analyzeCode() {
    const arr = editorContent.value.replaceAll('\r', '\n').split('\n'); // 先将回车符替换成换行符，避免粘贴的代码是以回车符分割的，然后以换行符分割
    // 将代码编辑器内写的内容抽取出来
    arr.forEach((e, i) => {
      if (e.trim() !== '') {
        // 排除空串
        const line = e.split(',');
        if (line.every((s) => s.trim() !== '') && !Number.isNaN(Number(line[2]))) {
          const item = {
            ip: line[0],
            port: line[1],
            concurrentNumber: Number(line[2]),
            singleTaskConcurrentNumber: Number(line[3]),
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
    setIsSave(false);
  }

  function changeResourceType(val: string | number | boolean) {
    if (val === 'Kubernetes') {
      setBatchFormRes();
    } else {
      form.value.testResourceDTO.concurrentNumber = 10;
      form.value.testResourceDTO.singleTaskConcurrentNumber = 3;
      form.value.testResourceDTO.nodesList = [
        {
          ip: '',
          port: '',
          concurrentNumber: 10,
          singleTaskConcurrentNumber: 3,
        },
      ];
    }
    setIsSave(false);
  }

  /**
   * 下载 yaml 文件
   * @param type 文件类型
   */
  function downloadYaml(type: YamlType) {
    let name = '';
    let yamlStr = '';
    const { namespace, deployName } = form.value.testResourceDTO;
    // 镜像内的版本号需要去掉尾部的 -xxx
    const apiImage = `registry.cn-qingdao.aliyuncs.com/metersphere/task-runner:${appStore.version.substring(
      0,
      appStore.version.lastIndexOf('-')
    )}`;
    switch (type) {
      case 'role':
        name = 'Role.yml';
        yamlStr = getYaml('role', '', namespace, '');
        break;
      case 'Deployment':
        name = 'Deployment.yml';
        yamlStr = getYaml('Deployment', deployName, namespace, apiImage);
        break;
      case 'DaemonSet':
        name = 'Daemonset.yml';
        yamlStr = getYaml('DaemonSet', deployName, namespace, apiImage);
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
    form.value = { ...cloneDeep(defaultForm) };
  }

  /**
   * 拼接添加资源池参数
   */
  function makeResourcePoolParams(): UpdateResourcePoolParams {
    const { type, testResourceDTO } = form.value;
    const {
      ip,
      token, // k8s token
      namespace, // k8s 命名空间
      concurrentNumber, // k8s 最大并发数
      podThreads, // k8s 单pod最大线程数
      // jobDefinition, // k8s job自定义模板
      deployName, // k8s api测试部署名称
      nodesList,
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
            namespace,
            concurrentNumber,
            podThreads,
            deployName: isCheckedAPI.value ? deployName : null, // 勾选了接口测试才需要传
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

    // const jobDTO = isShowK8SResources.value ? { jobDefinition } : {};
    return {
      ...form.value,
      type: isShowTypeItem.value ? form.value.type : 'Node', // 默认给 Node，后台需要
      allOrg: form.value.orgType === 'allOrg',
      apiTest: form.value.use.includes('API'), // 是否支持api测试
      uiTest: form.value.use.includes('UI'), // 是否支持ui测试
      testResourceDTO: { ...apiDTO, ...uiDTO, orgIds: form.value.testResourceDTO.orgIds },
    };
  }

  async function save() {
    try {
      loading.value = true;
      const params = makeResourcePoolParams();
      if (isEdit.value) {
        await updatePoolInfo(params);
        Message.success(t('system.resourcePool.updateSuccess'));
      } else {
        await addPool(params);
        Message.success(t('system.resourcePool.addSuccess'));
      }
      if (isContinueAdd.value) {
        resetForm();
      } else {
        setIsSave(true);
        await sleep(300);
        router.push({ name: 'settingSystemResourcePool' });
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 校验批量添加的资源信息
   * @param cb 校验通过后的回调函数
   */
  function validateBatchNodes(cb: () => void) {
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
          validateBatchNodes(save);
          return false;
        }
        return save();
      }
      return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
    });
  }

  function handleBack() {
    router.replace({ name: SettingRouteEnum.SETTING_SYSTEM_RESOURCE_POOL });
  }

  function goTry() {
    window.open('https://jinshuju.net/f/CzzAOe', '_blank');
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
  .has-license-class {
    @apply mb-2;
    :deep(.arco-form-item-content-wrapper) {
      min-height: 0;
    }
    :deep(.arco-form-item-wrapper-col) {
      min-height: 0;
    }
    :deep(.arco-form-item-content) {
      min-height: 0;
    }
  }
</style>
