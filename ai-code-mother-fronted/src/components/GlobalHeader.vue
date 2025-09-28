<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/favicon.ico" alt="logo"/>
            <div class="title">AI代码生成</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
            v-model:selectedKeys="current"
            mode="horizontal"
            :items="originItems"
            @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示栏 -->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar"/>
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined/>
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>

        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import {h, ref} from 'vue';
import {MailOutlined, AppstoreOutlined, SettingOutlined, HomeOutlined,LogoutOutlined} from '@ant-design/icons-vue';
import {MenuProps, message} from 'ant-design-vue';
import router from "@/router";
import {useLoginUserStore} from "@/stores/counter.ts";
import {userLogout} from "@/api/userController.ts";
import UserManagePage from "@/pages/admin/UserManagePage.vue";

const current = ref<string[]>([]);
// 未经过滤的菜单项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页'
  },
  {
    key: '/about',
    icon: () => h(MailOutlined),
    label: '关于',
    title: '关于'
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
]

const loginUserStore = useLoginUserStore();

const doMenuClick = ({key}: { key: string }) => {
  // console.log(key)
  router.push({
        path: key,
      }
  )
}
// 检测路由变化
router.afterEach((to, from) => {
  current.value = [to.path]
})


// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录'
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}


</script>
<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>


