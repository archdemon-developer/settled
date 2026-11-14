<script setup lang="ts"></script>

<template>
  <aside class="w-56 shrink-0 border-r border-slate-200 bg-slate-50 p-4">
    <nav class="flex flex-col gap-1 text-sm">
      <RouterLink
        to="/accounts"
        class="rounded px-3 py-2 font-medium text-slate-900 hover:bg-slate-200"
      >
        Accounts
      </RouterLink>
      <RouterLink
        to="/transactions"
        class="rounded px-3 py-2 font-medium text-slate-900 hover:bg-slate-200"
      >
        Transactions
      </RouterLink>
    </nav>
  </aside>
</template>
