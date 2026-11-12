import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseSelect from './BaseSelect.vue';

const options = [
  { value: 'CHECKING', label: 'Checking account' },
  { value: 'SAVINGS', label: 'Savings account' },
];

describe('BaseSelect', () => {
  it('renders one option per entry with its label and selects the current value', () => {
    const wrapper = mount(BaseSelect, { props: { modelValue: 'SAVINGS', options } });

    const rendered = wrapper.findAll('option');
    expect(rendered.map((option) => option.text())).toEqual([
      'Checking account',
      'Savings account',
    ]);
    expect(rendered.map((option) => option.attributes('value'))).toEqual(['CHECKING', 'SAVINGS']);
    expect(wrapper.find<HTMLSelectElement>('select').element.value).toBe('SAVINGS');
  });

  it('emits the value of the option the user picks', async () => {
    const wrapper = mount(BaseSelect, { props: { modelValue: 'CHECKING', options } });

    await wrapper.find('select').setValue('SAVINGS');

    expect(wrapper.emitted('update:modelValue')).toEqual([['SAVINGS']]);
  });

  it('puts the attributes it is given on the select itself', () => {
    const wrapper = mount(BaseSelect, {
      props: { modelValue: 'CHECKING', options },
      attrs: { id: 'account-type' },
    });

    expect(wrapper.find('select').attributes('id')).toBe('account-type');
  });
});
