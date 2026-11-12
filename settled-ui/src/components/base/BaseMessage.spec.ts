import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import BaseMessage from './BaseMessage.vue';

describe('BaseMessage', () => {
  it('renders its slot in a paragraph', () => {
    const wrapper = mount(BaseMessage, { props: { tone: 'muted' }, slots: { default: 'Loading' } });

    expect(wrapper.find('p').text()).toBe('Loading');
  });

  it('colours an error message red', () => {
    const wrapper = mount(BaseMessage, { props: { tone: 'error' } });

    const classes = wrapper.find('p').classes();
    expect(classes).toContain('text-red-700');
    expect(classes).not.toContain('text-slate-500');
  });

  it('colours a muted message grey', () => {
    const wrapper = mount(BaseMessage, { props: { tone: 'muted' } });

    const classes = wrapper.find('p').classes();
    expect(classes).toContain('text-slate-500');
    expect(classes).not.toContain('text-red-700');
  });

  it('keeps its own styling and adds the classes the parent passes', () => {
    const wrapper = mount(BaseMessage, { props: { tone: 'muted' }, attrs: { class: 'mt-4' } });

    const classes = wrapper.find('p').classes();
    expect(classes).toContain('mt-4');
    expect(classes).toContain('text-sm');
  });
});
