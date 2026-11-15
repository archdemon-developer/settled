import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import AppHeader from './AppHeader.vue';

describe('AppHeader', () => {
  it('renders the app name as the page title', () => {
    const wrapper = mount(AppHeader);

    expect(wrapper.find('header').exists()).toBe(true);
    expect(wrapper.find('h1').text()).toBe('Settled');
  });
});
