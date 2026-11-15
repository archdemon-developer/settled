import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BasePageTitle from './BasePageTitle.vue';

describe('BasePageTitle', () => {
  it("renders it's slot as a level 2 heading", () => {
    const wrapper = mount(BasePageTitle, { slots: { default: 'Accounts' } });
    expect(wrapper.find('h2').text()).toBe('Accounts');
  });

  it('keeps its own styling and adds the classes the parent passes', () => {
    const wrapper = mount(BasePageTitle, { attrs: { class: 'mt-6' } });

    const classes = wrapper.find('h2').classes();
    expect(classes).toContain('mt-6');
    expect(classes).toContain('font-semibold');
  });
});
