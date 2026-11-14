import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import FormField from './FormField.vue';

const controlSlot = '<template #default="{ id }"><input :id="id" /></template>';

function mountField(props: { error?: string } = {}) {
  return mount(FormField, {
    props: { id: 'account-code', label: 'code', ...props },
    slots: { default: controlSlot },
  });
}

describe('FormField', () => {
  it('shows the label', () => {
    const wrapper = mountField();

    expect(wrapper.find('label').text()).toBe('code');
  });

  it('ties the label to the control through the id it hands the slot', () => {
    const wrapper = mountField();

    expect(wrapper.find('label').attributes('for')).toBe('account-code');
    expect(wrapper.find('input').attributes('id')).toBe('account-code');
  });

  it('shows the error under the control when there is one', () => {
    const wrapper = mountField({ error: 'Code is required' });

    expect(wrapper.text()).toContain('Code is required');
    expect(wrapper.find('p').classes()).toContain('text-red-700');
  });

  it('shows no error line when there is no error', () => {
    const wrapper = mountField();

    expect(wrapper.find('p').exists()).toBe(false);
  });
});
