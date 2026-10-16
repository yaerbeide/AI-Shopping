<template>
  <div class="user">
    <IndexHeader />

    <div class="user-content">
      <div class="user-content-left">
        <MineInfosView />
      </div>
      <div class="user-content-right">
        <router-view />
      </div>
    </div>
  </div>
</template>
<script>
  import IndexHeader from '/@/views/index/components/header.vue';
  import MineInfosView from '/@/views/index/user/mine-infos-view.vue';

  export default {
    components: {
      MineInfosView,
      IndexHeader,
    },
    data() {
      return {
        collapsed: false,
      };
    },
  };
</script>
<style scoped lang="less">
  .user {
    display: block;
    min-height: 100vh;
    padding-top: 96px;
    background:
      radial-gradient(circle at 10% 8%, rgba(255, 209, 0, 0.16), transparent 24%),
      linear-gradient(180deg, #f7f8fa 0%, #ffffff 42%);
  }

  .user-content {
    display: flex;
    flex-direction: row;
    width: min(1320px, calc(100% - 48px));
    margin: 0 auto 80px;
    position: relative;
    gap: 28px;
    padding-top: 0;

    .user-content-left {
      position: sticky;
      top: 104px;
      height: fit-content;
    }

    .user-content-right {
      flex: 1;
      min-width: 0;
      padding: 28px 32px 38px;
      background-color: #fff;
      border: 1px solid #edf0f4;
      border-radius: 22px;
      box-shadow: 0 22px 60px rgba(16, 24, 40, 0.07);
      min-height: 620px;
    }
  }

  @media (max-width: 900px) {
    .user-content {
      flex-direction: column;
      width: min(100% - 32px, 720px);

      .user-content-left {
        position: relative;
        top: 0;
        width: 100%;
      }

      .user-content-right {
        padding: 22px;
        min-height: auto;
      }
    }
  }

  @media (max-width: 560px) {
    .user {
      padding-top: 86px;
    }

    .user-content {
      width: calc(100% - 24px);

      .user-content-right {
        padding: 18px;
      }
    }
  }
</style>
