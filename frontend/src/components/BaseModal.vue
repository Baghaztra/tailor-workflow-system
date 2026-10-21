<script setup lang="ts">
defineProps<{ title: string }>()
defineEmits<{ close: [] }>()
</script>

<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal">
      <header class="modal-header">
        <h2>{{ title }}</h2>
        <button type="button" class="btn btn-ghost btn-sm" @click="$emit('close')">Close</button>
      </header>
      <slot />
    </div>
  </div>
</template>
