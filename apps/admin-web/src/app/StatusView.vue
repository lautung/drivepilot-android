<script setup lang="ts">
defineProps<{ code: string; title: string; description: string }>()
</script>

<template>
  <main class="status-page">
    <div class="status-code">{{ code }}</div>
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
    <ElButton type="primary" @click="$router.push({ name: 'dashboard' })">返回系统概览</ElButton>
  </main>
</template>
