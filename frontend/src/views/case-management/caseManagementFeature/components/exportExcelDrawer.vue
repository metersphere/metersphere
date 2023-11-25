<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('featureTest.featureCase.associatedFile')"
    :ok-text="t('featureTest.featureCase.associated')"
    :ok-loading="drawerLoading"
    :width="480"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="header mb-6 flex justify-between">
      <span class="font-medium">{{ t('featureTest.featureCase.SelectExportRange') }}</span>
      <span class="text-[rgb(var(--primary-5))]">{{ t('featureTest.featureCase.clear') }}</span>
    </div>
    <div>
      <a-checkbox class="mb-4" :model-value="checkedAll" :indeterminate="indeterminate" @change="handleChangeAll"
        ><div class="flex items-center">
          <span class="mr-1">{{ t('featureTest.featureCase.baseField') }}</span
          ><span
            ><icon-up
              v-if="foldBaseFields"
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('base')" /><icon-right
              v-else
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('base')"
          /></span>
        </div>
      </a-checkbox>
    </div>
    <a-checkbox-group v-if="foldBaseFields" v-model="baseFields" class="checkboxContainer" @change="handleChange">
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
    </a-checkbox-group>
    <!-- 自定义字段 -->
    <div>
      <a-checkbox class="mb-4" :model-value="checkedAll" :indeterminate="indeterminate" @change="handleChangeAll"
        ><div class="flex items-center">
          <span class="mr-1">{{ t('featureTest.featureCase.customField') }}</span
          ><span
            ><icon-up
              v-if="foldCustomFields"
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('custom')" /><icon-right
              v-else
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('custom')"
          /></span>
        </div>
      </a-checkbox>
    </div>
    <a-checkbox-group v-if="foldCustomFields" v-model="customFields" class="checkboxContainer" @change="handleChange">
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
    </a-checkbox-group>
    <!-- 其他字段 -->
    <div>
      <a-checkbox class="mb-4" :model-value="checkedAll" :indeterminate="indeterminate" @change="handleChangeAll"
        ><div class="flex items-center">
          <span class="mr-1 flex items-center"
            >{{ t('featureTest.featureCase.otherFields') }}<span></span>
            <a-tooltip
              :content="t('featureTest.featureCase.otherFieldsToolTip')"
              position="top"
              :mouse-enter-delay="500"
              mini
            >
              <icon-question-circle
                class="mx-1 text-[16px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]" /></a-tooltip
            ><icon-up
              v-if="foldCustomFields"
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('other')" /><icon-right
              v-else
              class="text-[12px] text-[var(--color-text-brand)]"
              @click="toggle('other')"
          /></span>
        </div>
      </a-checkbox>
    </div>
    <a-checkbox-group v-if="foldOtherFields" v-model="otherFields" class="checkboxContainer" @change="handleChange">
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
      <div class="item checkbox">
        <a-checkbox value="1">Option 1</a-checkbox>
      </div>
    </a-checkbox-group>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
  }>();

  const showDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);

  const checkedAll = ref<boolean>(false);
  const indeterminate = ref<boolean>(false);

  function handleChangeAll() {}
  function handleChange() {}

  function handleDrawerConfirm() {}

  function handleDrawerCancel() {
    showDrawer.value = false;
  }

  const foldBaseFields = ref<boolean>(true); // 默认展开
  const baseFields = ref<string[]>([]);

  const foldCustomFields = ref<boolean>(true);
  const customFields = ref<string[]>([]);

  const foldOtherFields = ref<boolean>(true);
  const otherFields = ref<string[]>([]);

  function toggle(foldType: string) {
    if (foldType === 'base') {
      foldBaseFields.value = !foldBaseFields.value;
    } else if (foldType === 'custom') {
      foldCustomFields.value = !foldCustomFields.value;
    } else {
      foldOtherFields.value = !foldOtherFields.value;
    }
  }

  watch(
    () => props.visible,
    (val) => {
      showDrawer.value = val;
    }
  );

  watch(
    () => showDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );
</script>

<style scoped lang="less">
  .checkboxContainer {
    display: grid;
    margin-bottom: 16px;
    grid-template-columns: repeat(auto-fit, minmax(116px, 1fr));
    grid-gap: 16px;
    .checkbox {
      width: 90px;
      white-space: nowrap;
      @apply overflow-hidden text-ellipsis;
    }
  }
</style>
