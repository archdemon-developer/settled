import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import AppFooter from './AppFooter.vue';

describe('AppFooter', () => {
  it('renders the product tagline', () => {
    const wrapper = mount(AppFooter);

    expect(wrapper.find('footer').text()).toContain('Double-entry ledger');
  });
});
