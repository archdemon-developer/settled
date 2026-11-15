import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseInput from './BaseInput.vue';

describe('BaseInput', () => {
  it('shows the value it is given', () => {
    const wrapper = mount(BaseInput, { props: { modelValue: 'SAV001' } });
    expect(wrapper.find<HTMLInputElement>('input').element.value).toBe('SAV001');
  });

  it('emits the new value when the user types', async () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '' } });
    await wrapper.find('input').setValue('CHK001');
    expect(wrapper.emitted('update:modelValue')).toEqual([['CHK001']]);
  });

  it('puts the attributes it is given on the input itself', () => {
    const wrapper = mount(BaseInput, {
      props: { modelValue: '' },
      attrs: { id: 'account-code', required: true, maxlength: '50' },
    });

    const input = wrapper.find('input');
    expect(input.attributes('id')).toBe('account-code');
    expect(input.attributes('required')).toBeDefined();
    expect(input.attributes('maxlength')).toBe('50');
  });

  it('keeps its own styling and adds the classes the parent passes', () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '' }, attrs: { class: 'w-full' } });

    const classes = wrapper.find('input').classes();
    expect(classes).toContain('w-full');
    expect(classes).toContain('border-slate-300');
  });
});
