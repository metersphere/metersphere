import { PropType } from 'vue';
import { onClickOutside } from '@vueuse/core';

import { useI18n } from '@/hooks/useI18n';

import './style.less';
import { MsUserSelector } from '../ms-user-selector';
import { UserRequestTypeEnum } from '../ms-user-selector/utils';
import { ModelValueType } from './types';
import Message from '@arco-design/web-vue/es/message';

type EditStatus = 'null' | 'active' | 'hover';
export default defineComponent({
  name: 'MsEditComp',
  props: {
    // 具体绑定的值
    modelValue: {
      type: [String, Number, Boolean, Object, Array] as PropType<ModelValueType>,
      default: '',
    },
    // 表格默认显示的值
    defaultValue: {
      type: [String, Number, Boolean, Object, Array] as PropType<ModelValueType>,
      default: '',
    },
    // 组件类型
    mode: {
      type: String,
      default: 'Input',
    },
    // 选择框的选项
    options: {
      type: Array as PropType<{ value: string; label: string }[]>,
      default: () => [],
    },
    // 用户选择器的类型
    userSelectorType: {
      type: String as PropType<UserRequestTypeEnum>,
      default: '',
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    change: (value: ModelValueType, cb: (result: boolean, defaultValue: string) => void) => true,
  },
  setup(props, { emit }) {
    const { mode, options, modelValue, defaultValue } = toRefs(props);
    const oldModelValue = ref(modelValue.value);
    const editStatus = ref<EditStatus>('null');
    const selectRef = ref<HTMLElement | null>(null);
    const chengeStatus = ref(false);

    const { t } = useI18n();

    const handleChange = (value: ModelValueType) => {
      emit('change', value, (result: boolean, v: string) => {
        if (result) {
          defaultValue.value = v;
          Message.success(t('common.updateSuccess'));
          chengeStatus.value = true;
        } else {
          Message.error(t('common.updateFail'));
          modelValue.value = oldModelValue.value;
        }
      });
    };

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Enter') {
        handleChange(modelValue.value);
      }
    };

    const handleClick = () => {
      editStatus.value = 'active';
      if (mode.value === 'select') {
        selectRef.value?.focus();
      }
    };

    const handleReset = () => {
      editStatus.value = 'null';
      modelValue.value = oldModelValue.value;
      chengeStatus.value = false;
    };

    const handleBlur = () => {
      handleReset();
    };

    const renderDiv = () => {
      const _defaultValue = Array.isArray(defaultValue.value) ? defaultValue.value.join(',') : defaultValue.value;

      return (
        <div onClick={handleClick} class={'cursor-pointer'}>
          {_defaultValue}
        </div>
      );
    };

    const renderPrivew = () => {
      const _defaultValue = Array.isArray(defaultValue.value) ? defaultValue.value.join(',') : defaultValue.value;

      return (
        <div
          onClick={handleClick}
          onMouseleave={() => {
            if (editStatus.value === 'hover') editStatus.value = 'null';
          }}
          class="gap[8px] flex h-[32px] w-[112px] cursor-pointer flex-row items-center justify-between  bg-[var(--color-text-n8)] p-[8px]"
        >
          <div class="grow">{_defaultValue}</div>
          <div>
            <icon-down size={16} />
          </div>
        </div>
      );
    };

    onClickOutside(selectRef, (event) => {
      if (editStatus.value === 'active' && !chengeStatus.value) {
        handleReset();
      }
    });

    const renderChild = () => {
      if (mode.value === 'input') {
        return <a-input modelValue={modelValue} onKeyDown={handleKeyDown} onBlur={handleBlur} />;
      }
      if (mode.value === 'select') {
        return (
          <div ref={selectRef}>
            <a-select modelValue={modelValue} options={options.value} onSelect={handleChange} />
          </div>
        );
      }
      if (mode.value === 'tagInput') {
        return <a-input-tag modelValue={modelValue} onKeyDown={handleKeyDown} onBlur={handleBlur} />;
      }
      if (mode.value === 'userSelect') {
        return (
          <MsUserSelector
            modelValue={modelValue.value as string[]}
            onSelect={handleChange}
            type={props.userSelectorType}
          />
        );
      }
    };

    return () => (
      <div
        class="ms-edit-comp"
        onMouseenter={() => {
          if (editStatus.value === 'null') editStatus.value = 'hover';
        }}
      >
        {editStatus.value === 'null' && renderDiv()}
        {editStatus.value === 'hover' && renderPrivew()}
        {editStatus.value === 'active' && renderChild()}
      </div>
    );
  },
});
