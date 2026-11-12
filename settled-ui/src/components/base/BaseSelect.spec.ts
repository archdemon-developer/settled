import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseSelect from './BaseSelect.vue';

const options =
  '<option value="CHECKING">CHECKING</option><option value="SAVINGS">SAVINGS</option>';

describe('BaseSelect', () => {
  it('renders the options it is given and selects the current value', () => {
    const wrapper = mount(BaseSelect, {
      props: { modelValue: 'SAVINGS' },
      slots: { default: options },
    });

    expect(wrapper.findAll('option')).toHaveLength(2);
    expect(wrapper.find<HTMLSelectElement>('select').element.value).toBe('SAVINGS');
  });

  it('emits the new value when the user picks an option', async () => {
    const wrapper = mount(BaseSelect, {
      props: { modelValue: 'CHECKING' },
      slots: { default: options },
    });

    await wrapper.find('select').setValue('SAVINGS');

    expect(wrapper.emitted('update:modelValue')).toEqual([['SAVINGS']]);
  });

  it('puts the attributes it is given on the select itself', () => {
    const wrapper = mount(BaseSelect, {
      props: { modelValue: 'CHECKING' },
      attrs: { id: 'account-type' },
      slots: { default: options },
    });

    expect(wrapper.find('select').attributes('id')).toBe('account-type');
  });
});
