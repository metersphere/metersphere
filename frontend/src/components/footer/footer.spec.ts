import { shallowMount } from '@vue/test-utils';
import { describe, expect, test } from 'vitest';
import Footer from './index.vue';

describe('Footer', () => {
  test('mount @vue/test-utils', () => {
    const wrapper = shallowMount(Footer, {
      slots: {
        default: '',
      },
    });
    expect(wrapper.text()).toBe('');
  });
});
