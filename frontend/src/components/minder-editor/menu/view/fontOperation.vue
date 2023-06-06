<template>
  <div class="font-group ml-[10px]">
    <a-select
      v-model="fontFamilyDefaultValue"
      :placeholder="t('minder.menu.font.font')"
      class="font-family-select"
      :disabled="disabledFont"
      size="mini"
      @change="execCommandFontFamily"
    >
      <a-option
        v-for="item in fontFamilys"
        :key="item.id"
        :label="item.name"
        :value="item.value"
        :style="{ 'font-family': item.value }"
      />
    </a-select>
    <a-select
      v-model="fontSizeDefaultValue"
      :placeholder="t('minder.menu.font.size')"
      class="font-size-select"
      :disabled="disabledFontSize"
      size="mini"
      @change="execCommandFontSize"
    >
      <a-option
        v-for="item in fontSizes"
        :key="item.id"
        :label="item.label.toString()"
        :value="item.value"
        :style="{
          'font-size': item.value + 'px',
          'height': 2 * item.value + 'px',
          'line-height': 2 * item.value + 'px',
          'padding': 0,
        }"
      />
    </a-select>
    <span class="font-btn mt-[2px]">
      <span
        class="menu-btn tab-icons font-bold"
        :class="{ selected: boldSelected }"
        :disabled="disabledBold"
        @click="execCommandFontStyle('bold')"
      />
      <span
        class="font-italic menu-btn tab-icons"
        :class="{ selected: italicSelected }"
        :disabled="disabledItalic"
        @click="execCommandFontStyle('italic')"
      />
    </span>
  </div>
</template>

<script lang="ts" name="StyleOpreation" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const fontFamilys = [
    {
      id: 1,
      value: '宋体,SimSun',
      name: '宋体',
    },
    {
      id: 2,
      value: '微软雅黑,Microsoft YaHei',
      name: '微软雅黑',
    },
    {
      id: 3,
      value: '楷体,楷体_GB2312,SimKai',
      name: '楷体',
    },
    {
      id: 4,
      value: '黑体, SimHei',
      name: '黑体',
    },
    {
      id: 5,
      value: '隶书, SimLi',
      name: '隶书',
    },
    {
      id: 6,
      value: 'andale mono',
      name: 'Andale Mono',
    },
    {
      id: 7,
      value: 'arial,helvetica,sans-serif',
      name: 'Arial',
    },
    {
      id: 8,
      value: 'arial black,avant garde',
      name: 'arialBlack',
    },
    {
      id: 9,
      value: 'comic sans ms',
      name: 'comic Sans Ms',
    },
    {
      id: 10,
      value: 'impact,chicago',
      name: 'Impact',
    },
    {
      id: 11,
      value: 'times new roman',
      name: 'times New Roman',
    },
    {
      id: 12,
      value: 'sans-serif',
      name: 'Sans-Serif',
    },
  ];
  const fontSizes = [
    {
      id: 1,
      value: 10,
      label: 10,
    },
    {
      id: 2,
      value: 12,
      label: 12,
    },
    {
      id: 3,
      value: 16,
      label: 16,
    },
    {
      id: 4,
      value: 18,
      label: 18,
    },
    {
      id: 5,
      value: 24,
      label: 24,
    },
    {
      id: 6,
      value: 32,
      label: 32,
    },
    {
      id: 7,
      value: 48,
      label: 48,
    },
  ];

  const fontFamilyDefaultValue = ref('');
  const fontSizeDefaultValue = ref('');

  const disabledFont = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }

    const currentFontFamily = window.minder.queryCommandValue('fontfamily');
    // eslint-disable-next-line vue/no-side-effects-in-computed-properties
    fontFamilyDefaultValue.value = currentFontFamily || t('minder.menu.font.font');
    return window.minder.queryCommandState('fontfamily') === -1;
  });
  const disabledFontSize = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    // eslint-disable-next-line vue/no-side-effects-in-computed-properties
    fontSizeDefaultValue.value = window.minder.queryCommandValue('fontsize') || t('minder.menu.font.size');
    return window.minder.queryCommandState('fontsize') === -1;
  });
  const disabledBold = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState('bold') === -1;
  });
  const disabledItalic = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState('italic') === -1;
  });
  const boldSelected = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState('bold') === -1;
  });
  const italicSelected = computed(() => {
    try {
      if (!window.minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    return window.minder.queryCommandState('italic') === -1;
  });

  function execCommandFontFamily(
    value: string | number | Record<string, any> | (string | number | Record<string, any>)[]
  ) {
    if (value === t('minder.menu.font.font')) {
      return;
    }
    window.minder.execCommand('fontfamily', value);
  }

  function execCommandFontSize(
    value: string | number | Record<string, any> | (string | number | Record<string, any>)[]
  ) {
    if (typeof value !== 'number') {
      return;
    }
    window.minder.execCommand('fontsize', value);
  }

  function execCommandFontStyle(style: string) {
    switch (style) {
      case 'bold':
        if (window.minder.queryCommandState('bold') !== -1) {
          window.minder.execCommand('bold');
        }
        break;
      case 'italic':
        if (window.minder.queryCommandState('italic') !== -1) {
          window.minder.execCommand('italic');
        }
        break;
      default:
    }
  }
</script>
