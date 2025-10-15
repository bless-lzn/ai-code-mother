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
      <a-button type="primary" :disabled="!hasSelected" :loading="state.loading" @click="deleteSelected" >
        Reload
      </a-button>
      <span style="margin-left: 8px">
        <template v-if="hasSelected">
          {{ `Selected ${state.selectedRowKeys.length} items` }}
        </template>
      </span>
    </div>
    <a-table
        :row-selection="{ selectedRowKeys: state.selectedRowKeys, onChange: onSelectChange }"
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :row-key="(record) => record.id"
        @change="doTableChange">
      <template #bodyCell="{ column, record }">
        <!--        avatar头像显示-->
        <template v-if="column.dataIndex==='userAvatar'">
          <a-image :src="record.userAvatar" :width="120"></a-image>
        </template>

        <!--        角色显示标签 管理员用绿色，普通用户用蓝色-->
        <template v-if="column.dataIndex==='userRole'">
          <a-tag v-if="record.userRole==='admin'" color="green">管理员</a-tag>
          <a-tag v-else color="blue">普通用户</a-tag>
        </template>
        <!--        日期格式化-->
        <template v-if="column.dataIndex==='createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <!--        操作模块-->
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
import {SmileOutlined, DownOutlined} from '@ant-design/icons-vue';
import {computed, onMounted, reactive, ref} from "vue";
import {deleteUser, listUserVoByPage} from "@/api/userController.ts";
import {message} from "ant-design-vue";
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


//数据
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

//将数据源定义为响应式数据
const fetchData = async () => {
  const res = await listUserVoByPage(searchParams)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    // state.selectedRowKeys = dataList.value.map(item => item.id as number)??[]
    // console.log( state.selectedRowKeys)
    total.value = Number(res.data.data.totalPage) ?? 0
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
    showTotal: (total: any) => `共 ${total} 条`,
  }
})
// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//处理删除逻辑
const handleDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({id})
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败')
  }
}

//处理修改逻辑
const handleUpdate = async (record: API.UserVO) => {
  console.log(record)
  message.success('修改成功')
}

//加载
onMounted(() => {
  fetchData()
})

const state = reactive<{
  selectedRowKeys: number[];
  loading: boolean;
}>({
  selectedRowKeys: [], // Check here to configure the default column
  loading: false,
});
const hasSelected = computed(() => state.selectedRowKeys.length > 0);

const deleteSelected = () => {
  state.loading = true;
  // ajax request after empty completing
  console.log('selectedRowKeys changed: ',state.selectedRowKeys.values() );
    state.loading = false;
    state.selectedRowKeys = [];
    fetchData()
  ;
};

// type Key = string | number;
const onSelectChange = (selectedRowKeys: number[]) => {
  console.log('selectedRowKeys changed: ', selectedRowKeys);

  state.selectedRowKeys = selectedRowKeys;
};

</script>
<style scoped>
#userManagePage {
}
</style>

