import { mount } from '@vue/test-utils';
import { describe, expect, test } from 'vitest';
import Footer from '@/components/footer/index.vue';

describe('Footer', () => {
  test('renders the correct text', () => {
    const wrapper = mount(Footer, {
      props: {
        text: 'Custom Text',
      },
    });

    expect(wrapper.text()).toBe('Custom Text');
  });

  test('renders the default text if no prop is provided', () => {
    const wrapper = mount(Footer);

    expect(wrapper.text()).toBe('MeterSphere');
  });

  test('applies the correct styles', () => {
    const wrapper = mount(Footer);

    expect(wrapper.find('.footer').exists()).toBe(true);
    expect(wrapper.classes()).toContain('footer');
  });
});
