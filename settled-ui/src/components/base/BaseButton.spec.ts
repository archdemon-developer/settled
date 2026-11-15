import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseButton from './BaseButton.vue';

describe('BaseButton', () => {
  it('renders its slot inside a button', () => {
    const wrapper = mount(BaseButton, { slots: { default: 'Save' } });
    expect(wrapper.find('button').text()).toBe('Save');
  });

  it('defaults to type button so it never submits a form by accident', () => {
    const wrapper = mount(BaseButton);
    expect(wrapper.find('button').attributes('type')).toBe('button');
  });

  it('uses the type it is given', () => {
    const wrapper = mount(BaseButton, { props: { type: 'submit' } });

    expect(wrapper.find('button').attributes('type')).toBe('submit');
  });

  it('is disabled only when told to be', () => {
    const enabled = mount(BaseButton);
    const disabled = mount(BaseButton, { props: { disabled: true } });

    expect(enabled.find('button').attributes('disabled')).toBeUndefined();
    expect(disabled.find('button').attributes('disabled')).toBeDefined();
  });

  it('passes click events up to the parent', async () => {
    const onClick = vi.fn();
    const wrapper = mount(BaseButton, { attrs: { onClick } });

    await wrapper.find('button').trigger('click');

    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('keeps its own styling and adds the classes the parent passes', () => {
    const wrapper = mount(BaseButton, { attrs: { class: 'mt-6' } });

    const classes = wrapper.find('button').classes();
    expect(classes).toContain('mt-6');
    expect(classes).toContain('bg-slate-900');
  });
});
