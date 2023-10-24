<template>
  <MsCard
    :loading="loading"
    :title="title"
    :is-edit="isEdit"
    has-breadcrumb
    @save="saveHandler"
    @save-and-continue="saveHandler(true)"
  >
    <template #headerRight>
      <div class="rightBtn">
        <a-button v-show="isPreview" type="outline" class="text-[var(--color-text-1)]" @click="togglePreview">{{
          t('system.orgTemplate.templatePreview')
        }}</a-button>
        <a-button v-show="!isPreview" type="outline" class="text-[var(--color-text-1)]" @click="togglePreview">{{
          t('system.orgTemplate.exitPreview')
        }}</a-button>
      </div>
    </template>
    <!-- 非预览模式 -->
    <div v-if="isPreview" class="nonPreview">
      <a-form ref="formRef" :model="templateForm" layout="vertical">
        <a-form-item :label="t('system.orgTemplate.templateName')" field="name" asterisk-position="end" required>
          <a-input
            v-model:model-value="templateForm.name"
            :placeholder="t('system.orgTemplate.templateNamePlaceholder')"
            :max-length="255"
            show-word-limit
            class="max-w-[732px]"
          ></a-input>
        </a-form-item>
        <a-form-item field="remark" :label="t('system.orgTemplate.description')" asterisk-position="end">
          <a-textarea
            v-model="templateForm.remark"
            :max-length="255"
            :placeholder="t('system.orgTemplate.resDescription')"
            :auto-size="{
              maxRows: 1,
            }"
            class="max-w-[732px]"
          ></a-textarea>
        </a-form-item>
      </a-form>
      <!-- 已有字段表 -->
      <TemplateManagementTable ref="templateFieldTableRef" />
    </div>
    <!-- 预览模式 -->
    <PreviewTemplate v-else :select-field="selectFiledToTem" />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模版-模版管理-创建&编辑
   */
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import TemplateManagementTable from './templateManagementTable.vue';
  import PreviewTemplate from './viewTemplate.vue';

  import { createOrganizeTemplateInfo, updateOrganizeTemplateInfo } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
  import { sleep } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';

  import type { ActionTemplateManage, CustomField } from '@/models/setting/template';
  import { SettingRouteEnum } from '@/enums/routeEnum';

  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const appStore = useAppStore();
  useLeaveUnSaveTip();

  const title = ref('');
  const loading = ref(false);

  const isEdit = ref(false);
  watchEffect(() => {
    if (route.query.id) {
      title.value = t('menu.settings.organization.templateManagementEdit');
      isEdit.value = true;
    } else {
      title.value = t('menu.settings.organization.templateManagementDetail');
      isEdit.value = false;
    }
  });

  const initTemplateForm = {
    name: '',
    remark: '',
  };

  const templateForm = ref({ ...initTemplateForm });

  const templateFieldTableRef = ref();

  const formRef = ref<FormInstance>();

  // 获取模板参数
  function getTemplateParams(): ActionTemplateManage {
    const result: CustomField[] = templateFieldTableRef.value.getCustomFields();
    const { name, remark } = templateForm.value;
    return {
      name,
      remark,
      customFields: result,
      scopeId: appStore.currentOrgId,
      scene: route.query.type,
    };
  }

  function resetForm() {
    templateForm.value = { ...initTemplateForm };
  }

  const isContinueFlag = ref(false);

  async function save() {
    try {
      loading.value = true;
      const params = getTemplateParams();
      if (isEdit.value) {
        await updateOrganizeTemplateInfo(params);
        Message.success(t('system.resourcePool.updateSuccess'));
      } else {
        await createOrganizeTemplateInfo(params);
        Message.success(t('system.orgTemplate.addSuccess'));
      }
      if (isContinueFlag.value) {
        resetForm();
      } else {
        await sleep(300);
        router.push({ name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_DETAIL });
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 保存
  async function saveHandler(isContinue = false) {
    isContinueFlag.value = isContinue;
    formRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        save();
      } else {
        return scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
      }
    });
  }

  const selectFiledToTem = ref([]); // 非预览模式模板已选择字段
  // 是否预览模式
  const isPreview = ref<boolean>(true); // 默认非预览模式
  function togglePreview() {
    isPreview.value = !isPreview.value;
    if (!isPreview.value) {
      selectFiledToTem.value = templateFieldTableRef.value.getSelectFiled();
    }
  }
</script>

<style scoped lang="less">
  .rightBtn {
    :deep(.arco-btn-outline) {
      border-color: var(--color-text-input-border) !important;
      color: var(--color-text-1) !important;
    }
  }
</style>
