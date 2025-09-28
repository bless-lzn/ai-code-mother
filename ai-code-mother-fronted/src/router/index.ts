import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '../views/HomeView.vue'
import HomePage from "@/pages/HomePage.vue";
import UserManagePage from "@/pages/admin/UserManagePage.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: '主页',
            component: HomePage,
        },
        {
            path: '/user/login',
            name: '用户登录',
            component: () => import('@/pages/user/userLoginPage.vue'),
        },
        {
            path: '/user/register',
            name: '用户注册',
            component: () => import('@/pages/user/UserRegisterPage.vue'),
        },
        {
            path: '/admin/userManage',
            name: '用户管理',
            component: UserManagePage,
        },
    ],
})

export default router
