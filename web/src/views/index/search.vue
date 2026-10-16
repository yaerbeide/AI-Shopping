<template>
  <div class="search">
    <Header />
    <div class="search-content">
      <SearchContentView />
    </div>
  </div>
</template>

<script setup>
  import Header from '/@/views/index/components/header.vue';
  import SearchContentView from '/@/views/index/components/search-content-view.vue';
</script>

<style scoped lang="less">
  .search-content {
    width: 100%;
    min-width: 0;
    margin: 0;
  }
</style>
