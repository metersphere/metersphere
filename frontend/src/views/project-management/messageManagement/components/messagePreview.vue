<template>
  <div v-if="!props.isUpdatePreview" class="mb-[8px] flex items-center text-[14px]">
    <div class="font-medium text-[var(--color-text-1)]">{{ props.robot.robotName }}</div>
    <div class="ml-[2px] text-[var(--color-text-4)]">({{ `${props.functionName}-${eventName}` }})</div>
  </div>
  <div
    v-if="props.robot.platform === 'IN_SITE'"
    class="preview-rounded h-full w-[400px] bg-[var(--color-text-n9)] p-[12px] text-[14px]"
  >
    <div class="preview-rounded h-full overflow-scroll bg-[var(--color-text-fff)]">
      <div
        v-if="!props.isUpdatePreview"
        class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[16px]"
      >
        <div class="text-[16px] font-medium leading-[24px] text-[var(--color-text-1)]">
          {{ t('project.messageManagement') }}
        </div>
        <icon-close size="12" />
      </div>
      <div class="flex gap-[12px] p-[16px]">
        <a-avatar>MS</a-avatar>
        <div class="flex flex-1 flex-col overflow-hidden">
          <div class="font-medium text-[var(--color-text-1)]" v-html="subject || '-'"></div>
          <div class="my-[4px] flex-1 text-[var(--color-text-2)]" v-html="template || '-'"></div>
          <div class="text-[var(--color-text-4)]">{{ dayjs().format('YYYY-MM-DD HH:mm:ss') }}</div>
        </div>
      </div>
    </div>
  </div>
  <div
    v-else-if="props.robot.platform === 'MAIL'"
    class="preview-rounded h-full w-[400px] overflow-scroll bg-[var(--color-text-n9)] p-[12px] text-[14px]"
  >
    <div class="mb-[4px] text-[16px] font-medium leading-[24px] text-[var(--color-text-1)]" v-html="subject || '-'">
    </div>
    <div class="mb-[8px] flex flex-col">
      <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
        {{ t('project.messageManagement.emailSender') }}
      </div>
      <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
        {{ `${t('project.messageManagement.emailSendTime')}${dayjs().format('YYYY-MM-DD HH:mm:ss')}` }}
      </div>
      <div class="text-[12px] leading-[16px] text-[var(--color-text-4)]">
        {{ t('project.messageManagement.emailReceiver') }}
      </div>
    </div>
    <div
      class="preview-rounded bg-[var(--color-text-fff)] p-[16px] text-[var(--color-text-2)]"
      v-html="template || '-'"
    >
    </div>
  </div>
  <div
    v-else-if="props.robot.platform === 'WE_COM'"
    class="preview-rounded h-full w-[400px] overflow-scroll bg-[var(--color-text-n9)] p-[12px] text-[14px]"
  >
    <div class="preview-rounded bg-[var(--color-text-fff)]">
      <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[16px_16px_8px_16px]">
        <div class="flex items-center gap-[4px] text-[14px] font-medium text-[var(--color-text-1)]">
          <MsIcon type="icon-logo_wechat-work" size="20"></MsIcon>
          {{ t('project.messageManagement.groupName') }}
        </div>
      </div>
      <div class="flex flex-col px-[16px] pb-[16px]">
        <div class="my-[8px] flex items-center">
          <div class="text-[var(--color-text-4)]">{{ props.functionName }}</div>
          <MsTag size="small" class="ml-[4px] px-[4px] !text-[var(--color-text-4)]">BOT</MsTag>
        </div>
        <div class="relative rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[12px]">
          <div class="preview-rounded absolute left-0 top-0 h-full w-[2px] bg-[rgb(var(--link-6))]"></div>
          <div class="text-[var(--color-text-2)]" v-html="`【${subject}】${template || '-'}`"></div>
        </div>
      </div>
    </div>
  </div>
  <div
    v-else-if="props.robot.platform === 'DING_TALK'"
    class="preview-rounded h-full w-[400px] overflow-scroll bg-[var(--color-text-n9)] text-[14px]"
  >
    <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[12px_12px_4px_12px]">
      <div class="flex items-center gap-[4px] text-[14px] font-medium text-[var(--color-text-1)]">
        <MsIcon type="icon-logo_dingtalk" size="24" />
        {{ t('project.messageManagement.groupName') }}
      </div>
    </div>
    <div class="flex flex-col px-[12px] pb-[12px]">
      <div class="my-[8px] flex items-center">
        <template v-if="props.robot.dingType === 'ENTERPRISE'">
          <MsIcon
            type="icon-icon_dingding"
            size="24"
            class="rounded-[var(--border-radius-mini)] bg-[rgb(var(--link-3))] px-[4px] text-white"
          />
          <div class="mx-[4px] text-[var(--color-text-4)]">{{ t('project.messageManagement.internalRobot') }}</div>
        </template>
        <template v-else>
          <MsIcon
            type="icon-icon_bot1"
            size="24"
            class="rounded-[var(--border-radius-mini)] bg-[var(--color-text-2)] px-[4px] text-white"
          />
          <div class="mx-[4px] text-[var(--color-text-4)]">{{ t('project.messageManagement.customRobot') }}</div>
        </template>
        <MsTag
          size="small"
          class="ml-[4px] flex items-center gap-[4px] border-none !bg-[var(--color-text-fff)] px-[4px] !text-[var(--color-text-4)]"
        >
          <MsIcon type="icon-icon_bot1" size="22" />
          {{ t('project.messageManagement.robot') }}
        </MsTag>
      </div>
      <div
        class="rounded-[var(--border-radius-small)] bg-[var(--color-text-fff)] p-[12px] text-[var(--color-text-2)]"
        v-html="`【${subject}】${template || '-'}`"
      >
      </div>
    </div>
  </div>
  <div
    v-else-if="props.robot.platform === 'LARK'"
    class="preview-rounded h-full w-[400px] overflow-scroll bg-[var(--color-text-n9)] text-[14px]"
  >
    <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[12px_12px_4px_12px]">
      <div class="flex items-center gap-[4px] text-[14px] font-medium text-[var(--color-text-1)]">
        <MsIcon type="icon-logo_lark" size="24" />
        {{ t('project.messageManagement.groupName') }}
      </div>
    </div>
    <div class="flex flex-col px-[12px] pb-[12px]">
      <div class="my-[8px] flex flex-wrap items-center">
        <MsIcon
          type="icon-icon_bot1"
          size="24"
          class="rounded-[var(--border-radius-mini)] bg-[rgb(var(--warning-6))] px-[4px] text-white"
        />
        <div class="mx-[4px] text-[var(--color-text-4)]">{{ t('project.messageManagement.robot') }}</div>
        <MsTag
          size="small"
          class="flex items-center gap-[4px] border-none !bg-[rgb(var(--warning-3))] px-[4px] !text-[rgb(var(--warning-7))]"
        >
          {{ t('project.messageManagement.robot') }}
        </MsTag>
        <div class="ml-[4px] text-[12px] leading-[16px] text-[var(--color-text-4)]">
          {{ t('project.messageManagement.larkRobotTip') }}
        </div>
      </div>
      <div
        class="rounded-[var(--border-radius-small)] bg-[var(--color-text-fff)] p-[12px] text-[var(--color-text-2)]"
        v-html="`【${subject}】${template || '-'}`"
      >
      </div>
    </div>
  </div>
  <div
    v-else-if="props.robot.platform === 'CUSTOM'"
    class="preview-rounded h-full w-[400px] overflow-scroll bg-[var(--color-text-n9)] p-[12px] text-[14px]"
  >
    <div class="preview-rounded bg-[var(--color-text-fff)]">
      <div class="flex items-center justify-between border-b border-[var(--color-text-n8)] p-[16px_16px_8px_16px]">
        <div class="flex items-center gap-[4px] text-[14px] font-medium text-[var(--color-text-1)]">
          <MsIcon type="icon-logo_wechat-work" size="20"></MsIcon>
          {{ t('project.messageManagement.groupName') }}
        </div>
      </div>
      <div class="flex flex-col px-[16px] pb-[16px]">
        <div class="my-[8px] flex items-center">
          <div class="text-[var(--color-text-4)]">{{ props.functionName }}</div>
          <MsTag size="small" class="ml-[4px] px-[4px] !text-[var(--color-text-4)]">BOT</MsTag>
        </div>
        <div
          class="preview-rounded mt-[4px] bg-[var(--color-text-n9)] p-[12px] text-[var(--color-text-2)]"
          v-html="`【${subject}】${template || '-'}`"
        >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import dayjs from 'dayjs';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { Field, MessageTemplateDetail } from '@/models/projectManagement/message';

  const props = defineProps<{
    robot: MessageTemplateDetail;
    functionName: string; // 功能名
    eventName: string; // 场景名
    isUpdatePreview?: boolean; // 是否更新预览
    fields?: Field[];
  }>();

  const { t } = useI18n();

  // 使用正则表达式替换 ${variableName} 格式的字符串内的变量名为变量描述
  function replaceVariableStr(str: string, fields: Field[], isSubject = false) {
    const regex = /\$\{([^}]+)\}/g;
    return str
      .replace(regex, (match, variableName) => {
        // 在数组中查找匹配的 variableName，并返回其 name 属性
        const variable = fields.find((v) => v.id === variableName);
        if (variableName === 'name' && !isSubject) {
          return variable ? `<span style='color: rgb(var(--primary-6))'><${variable.name}></span>` : match;
        }
        if (isSubject) {
          return variable ? variable.name : match;
        }
        return variable ? `<${variable.name}>` : match;
      })
      .replace(/\n/g, '<br>');
  }

  // 使用正则表达式替换 {{name}} 为高亮的关键字
  function replacePreviewName(str: string) {
    return str
      .replace(/{{(.*?)}}/g, `<span style='color: rgb(var(--primary-6))'>&lt;$1&gt;</span>`)
      .replace(/\n/g, '<br>');
  }

  const subject = computed(() => {
    if (props.isUpdatePreview) {
      return replaceVariableStr(props.robot.subject || '', props.fields || [], true);
    }
    let previewName = replacePreviewName(props.robot.previewSubject || '');
    // 邮件标题默认显示Metersphere
    if (
      props.robot.platform === 'MAIL' &&
      props.robot.previewSubject !== null &&
      props.robot.previewSubject?.indexOf('MeterSphere') === -1
    ) {
      previewName = `MeterSphere ${previewName}`;
    }
    return previewName;
  });

  const template = computed(() => {
    if (props.isUpdatePreview) {
      return replaceVariableStr(props.robot.template || '', props.fields || []);
    }
    return replacePreviewName(props.robot.previewTemplate || '');
  });
</script>

<style lang="less" scoped>
  .preview-rounded {
    @apply overflow-hidden;

    border-radius: var(--border-radius-small);
  }
  .overflow-scroll {
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
</style>
