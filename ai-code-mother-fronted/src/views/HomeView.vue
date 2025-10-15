<template>
  <div id="userManagePage">

    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号"/>
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名"/>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider/>
    <!-- 表格 -->
    <div style="margin-bottom: 16px">
      <a-button type="primary" :disabled="!hasSelected" :loading="state.loading" @click="start">
        Reload
      </a-button>
      <a-button
          type="primary"
          danger
          :disabled="!hasSelected"
          style="margin-left: 8px"
          @click="handleBatchDelete"
      >
        批量删除
      </a-button>
      <span style="margin-left: 8px">
        <template v-if="hasSelected">
          {{ `已选择 ${state.selectedRowKeys.length} 项` }}
        </template>
      </span>
    </div>
    <a-table
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :row-key="(record) => record.id"
        @change="doTableChange">
      <template #bodyCell="{ column, record }">
        <!-- avatar头像显示 -->
        <template v-if="column.dataIndex==='userAvatar'">
          <a-image :src="record.userAvatar" :width="120"></a-image>
        </template>

        <!-- 角色显示标签 管理员用绿色，普通用户用蓝色 -->
        <template v-if="column.dataIndex==='userRole'">
          <a-tag v-if="record.userRole==='admin'" color="green">管理员</a-tag>
          <a-tag v-else color="blue">普通用户</a-tag>
        </template>
        <!-- 日期格式化 -->
        <template v-if="column.dataIndex==='createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!-- 操作模块 -->
        <template v-if="column.key==='action'">
          <a-space wrap>
            <a-button type="primary" @click="handleUpdate(record)">编辑</a-button>
            <a-button type="primary" danger @click="handleDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { deleteUser, listUserVoByPage } from "@/api/userController.ts";
import { message, Modal } from "ant-design-vue";
import dayjs from "dayjs";

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    align: 'center'
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const dataList = ref<API.UserVO[]>([])
const total = ref<number>(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 将数据源定义为响应式数据
const fetchData = async () => {
  const res = await listUserVoByPage(searchParams)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = Number(res.data.data.totalPage) ?? 0 // 注意：这里应该是total而不是totalPage
  } else {
    message.error('获取用户列表失败')
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 处理删除逻辑
const handleDelete = async (id: number) => {
  if (!id) {
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '您确定要删除这个用户吗？',
    onOk: async () => {
      const res = await deleteUser({ id })
      if (res.data.code === 0) {
        message.success('删除成功')
        fetchData()
        // 如果删除的是选中的项，从选中列表中移除
        const index = state.selectedRowKeys.indexOf(id)
        if (index > -1) {
          state.selectedRowKeys.splice(index, 1)
        }
      } else {
        message.error('删除失败')
      }
    }
  })
}

// 批量删除
const handleBatchDelete = async () => {
  if (state.selectedRowKeys.length === 0) {
    message.warning('请先选择要删除的用户')
    return
  }

  Modal.confirm({
    title: '确认批量删除',
    content: `您确定要删除选中的 ${state.selectedRowKeys.length} 个用户吗？`,
    onOk: async () => {
      // 这里需要实现批量删除的API调用
      // 假设有批量删除的API
      try {
        // const res = await batchDeleteUser({ ids: state.selectedRowKeys })
        // if (res.data.code === 0) {
        message.success(`成功删除 ${state.selectedRowKeys.length} 个用户`)
        fetchData()
        state.selectedRowKeys = []
        // }
      } catch (error) {
        message.error('批量删除失败')
      }
    }
  })
}

// 处理修改逻辑
const handleUpdate = async (record: API.UserVO) => {
  console.log('编辑用户:', record)
  message.success('修改成功')
}

// 加载
onMounted(() => {
  fetchData()
})

const state = reactive<{
  selectedRowKeys: number[];
  loading: boolean;
}>({
  selectedRowKeys: [],
  loading: false,
})

const hasSelected = computed(() => state.selectedRowKeys.length > 0)

const start = () => {
  state.loading = true
  // 模拟重新加载
  setTimeout(() => {
    state.loading = false
    state.selectedRowKeys = []
    fetchData()
  }, 1000)
}

const onSelectChange = (selectedRowKeys: number[]) => {
  console.log('selectedRowKeys changed: ', selectedRowKeys)
  state.selectedRowKeys = selectedRowKeys
}

// 使用computed创建rowSelection配置，避免重复渲染问题
const rowSelection = computed(() => {
  return {
    selectedRowKeys: state.selectedRowKeys,
    onChange: onSelectChange,
    // 可选：添加选择框的列配置
    columnWidth: 48,
    fixed: true,
  }
})
</script>

<style scoped>
#userManagePage {
  padding: 16px;
}
</style>