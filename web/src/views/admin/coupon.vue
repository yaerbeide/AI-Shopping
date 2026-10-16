<script>
  import { reactive, ref, onMounted, h, nextTick, computed } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    GiftOutlined,
    MoneyCollectOutlined,
    PercentageOutlined,
    CalendarOutlined,
    SearchOutlined,
    ReloadOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
  } from '@ant-design/icons-vue';
  import { getCouponList, addCoupon, updateCoupon, deleteCoupon } from '@/api/coupon';

  export default {
    name: 'AdminCoupon',
    components: {
      GiftOutlined,
      MoneyCollectOutlined,
      PercentageOutlined,
      CalendarOutlined,
      SearchOutlined,
      ReloadOutlined,
      PlusOutlined,
      EditOutlined,
      DeleteOutlined,
    },
    setup() {
      // 表格数据
      const tableData = ref([]);
      const pagination = reactive({
        current: 1,
        pageSize: 10,
        total: 0,
        showTotal: (total) => `共 ${total} 条`,
        showQuickJumper: true,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
      });
      const loading = ref(false);
      const searchKeyword = ref('');
      const filterType = ref(null);
      const filterStatus = ref(null);

      // 计算过滤后的数据
      const filteredTableData = computed(() => {
        let result = [...tableData.value];

        // 根据类型筛选
        if (filterType.value !== null) {
          result = result.filter((item) => item.type === filterType.value);
        }

        // 根据状态筛选
        if (filterStatus.value !== null) {
          result = result.filter((item) => item.status === filterStatus.value);
        }

        return result;
      });

      // 计算优惠券总数
      const totalCoupons = computed(() => {
        return tableData.value.length;
      });

      // 获取优惠券类型数量
      const getCouponTypeCount = (type) => {
        return tableData.value.filter((item) => item.type === type).length;
      };

      // 获取活动中的优惠券数量
      const getActiveCouponCount = () => {
        const now = new Date().toISOString();
        return tableData.value.filter((item) => {
          return item.status === 1 && item.startTime && item.endTime && item.startTime <= now && item.endTime >= now;
        }).length;
      };

      // 获取行样式
      const getRowClassName = (record) => {
        const now = new Date().toISOString();
        if (record.status === 0) {
          return 'coupon-row-disabled';
        }
        if (record.endTime && record.endTime < now) {
          return 'coupon-row-expired';
        }
        if (record.startTime && record.startTime > now) {
          return 'coupon-row-upcoming';
        }
        return 'coupon-row-active';
      };

      // 定义列
      const columns = [
        {
          title: '名称',
          dataIndex: 'name',
          key: 'name',
          ellipsis: true,
          width: 150,
        },
        {
          title: '类型',
          dataIndex: 'type',
          key: 'type',
          width: 90,
          filters: [
            { text: '满减券', value: 1 },
            { text: '折扣券', value: 2 },
          ],
          onFilter: (value, record) => record.type === value,
        },
        {
          title: '折扣',
          dataIndex: 'discount',
          key: 'discount',
          width: 80,
        },
        {
          title: '使用门槛',
          dataIndex: 'threshold',
          key: 'threshold',
          width: 120,
        },
        {
          title: '优惠金额',
          dataIndex: 'amount',
          key: 'amount',
          width: 100,
        },
        {
          title: '开始时间',
          dataIndex: 'startTime',
          key: 'startTime',
          width: 160,
          sorter: (a, b) => new Date(a.startTime) - new Date(b.startTime),
        },
        {
          title: '结束时间',
          dataIndex: 'endTime',
          key: 'endTime',
          width: 160,
          sorter: (a, b) => new Date(a.endTime) - new Date(b.endTime),
        },
        {
          title: '状态',
          dataIndex: 'status',
          key: 'status',
          width: 80,
          filters: [
            { text: '启用', value: 1 },
            { text: '禁用', value: 0 },
          ],
          onFilter: (value, record) => record.status === value,
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          key: 'createTime',
          width: 160,
          sorter: (a, b) => new Date(a.createTime) - new Date(b.createTime),
        },
        {
          title: '操作',
          dataIndex: 'action',
          key: 'action',
          fixed: 'right',
          width: 140,
        },
      ];

      // 表单相关
      const modalVisible = ref(false);
      const modalTitle = ref('添加优惠券');
      const modalConfirmLoading = ref(false);
      const formRef = ref(null);
      const formData = reactive({
        id: null,
        name: '',
        type: 1, // 默认满减券
        discount: null,
        threshold: null,
        amount: null,
        startTime: null,
        endTime: null,
        status: 1,
      });

      // 表单校验规则
      const rules = {
        name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
        discount: [
          { required: true, message: '请输入折扣率', trigger: 'change', type: 'number' },
          { type: 'number', min: 0.1, max: 1, message: '折扣率必须在0.1-1之间', trigger: 'change' },
        ],
        amount: [
          { required: true, message: '请输入优惠金额', trigger: 'change', type: 'number' },
          { type: 'number', min: 1, message: '优惠金额必须大于0', trigger: 'change' },
        ],
        startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
        endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
      };

      const typePlaceholder = ref('');
      const disabledDiscount = ref(false);
      const disabledAmount = ref(false);

      // 加载数据
      const loadData = async (pageNum = 1, pageSize = 10) => {
        loading.value = true;
        try {
          const res = await getCouponList(pageNum, pageSize, searchKeyword.value);
          if (res && res.code === 200) {
            tableData.value = res.data.records || [];
            pagination.total = res.data.total || 0;
            pagination.current = pageNum;
            pagination.pageSize = pageSize;
          } else {
            message.error(res?.message || '获取数据失败');
            // 如果API失败，使用默认数据（临时解决方案）
            setDefaultData();
          }
        } catch (error) {
          console.error('获取优惠券列表失败', error);
          message.error('获取数据失败');
          // 如果API出错，使用默认数据（临时解决方案）
          setDefaultData();
        } finally {
          loading.value = false;
        }
      };

      // 设置默认数据
      const setDefaultData = () => {
        tableData.value = [
          {
            id: 1,
            name: '新用户优惠券',
            type: 1,
            discount: null,
            threshold: 100,
            amount: 10,
            startTime: '2025-06-11T23:18:54',
            endTime: '2029-12-15T23:59:59',
            status: 1,
            createTime: '2025-06-11T14:53:34',
          },
          {
            id: 2,
            name: '全场折扣券',
            type: 2,
            discount: 0.8,
            threshold: 0,
            amount: null,
            startTime: '2025-06-11T23:18:44',
            endTime: '2028-12-21T23:59:59',
            status: 1,
            createTime: '2025-06-11T14:53:34',
          },
          {
            id: 3,
            name: '满100减10元券',
            type: 1,
            discount: null,
            threshold: 100,
            amount: 10,
            startTime: '2025-06-11T23:18:33',
            endTime: '2026-12-11T23:59:59',
            status: 1,
            createTime: '2023-01-01T00:00:00',
          },
          {
            id: 4,
            name: '满200减30元券',
            type: 1,
            discount: null,
            threshold: 200,
            amount: 30,
            startTime: '2025-06-11T23:18:02',
            endTime: '2027-06-10T23:18:05',
            status: 1,
            createTime: '2023-01-01T00:00:00',
          },
          {
            id: 5,
            name: '9折优惠券',
            type: 2,
            discount: 0.9,
            threshold: 0,
            amount: null,
            startTime: '2025-06-11T23:19:25',
            endTime: '2027-06-24T15:59:48',
            status: 1,
            createTime: '2023-01-01T00:00:00',
          },
        ];
        pagination.total = tableData.value.length;
      };

      // 表格分页变化
      const handleTableChange = (pag) => {
        loadData(pag.current, pag.pageSize);
      };

      // 搜索
      const handleSearch = () => {
        loadData(1, pagination.pageSize);
      };

      // 重置搜索
      const handleReset = () => {
        searchKeyword.value = '';
        filterType.value = null;
        filterStatus.value = null;
        loadData(1, pagination.pageSize);
      };

      // 打开添加弹窗
      const handleAdd = () => {
        resetForm();
        modalTitle.value = '添加优惠券';
        modalVisible.value = true;
      };

      // 打开编辑弹窗
      const handleEdit = (record) => {
        // 先重置表单，确保清除之前的数据
        resetForm();

        modalTitle.value = '编辑优惠券';

        // 深拷贝数据以避免直接引用
        const recordCopy = JSON.parse(JSON.stringify(record));

        // 处理日期格式，确保格式一致
        if (recordCopy.startTime && recordCopy.startTime.includes('T')) {
          recordCopy.startTime = recordCopy.startTime.replace('T', ' ');
        }
        if (recordCopy.endTime && recordCopy.endTime.includes('T')) {
          recordCopy.endTime = recordCopy.endTime.replace('T', ' ');
        }

        // 将记录数据赋值给表单数据
        Object.assign(formData, {
          id: recordCopy.id,
          name: recordCopy.name,
          type: recordCopy.type,
          discount: recordCopy.discount,
          threshold: recordCopy.threshold,
          amount: recordCopy.amount,
          startTime: recordCopy.startTime,
          endTime: recordCopy.endTime,
          status: recordCopy.status,
        });

        // 根据类型设置相关字段状态
        setFieldsByType(formData.type);

        // 确保表单状态更新后再显示弹窗
        nextTick(() => {
          modalVisible.value = true;
        });
      };

      // 表单类型切换
      const handleTypeChange = (e) => {
        setFieldsByType(e.target.value);
      };

      // 根据类型设置字段状态
      const setFieldsByType = (type) => {
        if (type === 1) {
          // 满减券
          typePlaceholder.value = '满X元减Y元';
          disabledDiscount.value = true;
          disabledAmount.value = false;
          formData.discount = null;
        } else if (type === 2) {
          // 折扣券
          typePlaceholder.value = '打X折';
          disabledDiscount.value = false;
          disabledAmount.value = true;
          formData.amount = null;
        }
      };

      // 表单提交
      const handleSubmit = () => {
        formRef.value.validate().then(async () => {
          modalConfirmLoading.value = true;
          try {
            // 创建一个新对象，避免直接修改表单数据
            const submitData = JSON.parse(JSON.stringify(formData));

            // 确保日期格式正确，使用ISO格式（带T的格式）
            if (submitData.startTime && !submitData.startTime.includes('T')) {
              submitData.startTime = submitData.startTime.replace(' ', 'T');
            }
            if (submitData.endTime && !submitData.endTime.includes('T')) {
              submitData.endTime = submitData.endTime.replace(' ', 'T');
            }

            let res;
            if (submitData.id) {
              res = await updateCoupon(submitData);
            } else {
              res = await addCoupon(submitData);
            }

            if (res && res.code === 200) {
              message.success(res.message || '操作成功');
              modalVisible.value = false;
              loadData(pagination.current, pagination.pageSize);
            } else {
              message.error(res?.message || '操作失败');
            }
          } catch (error) {
            console.error('保存优惠券失败', error);
            message.error('操作失败');
          } finally {
            modalConfirmLoading.value = false;
          }
        });
      };

      // 删除优惠券
      const handleDelete = (record) => {
        Modal.confirm({
          title: '确认删除',
          content: `确定要删除优惠券 "${record.name}" 吗？`,
          okText: '确认',
          cancelText: '取消',
          okType: 'danger',
          onOk: async () => {
            try {
              const res = await deleteCoupon(record.id);
              if (res && res.code === 200) {
                message.success(res.message || '删除成功');
                loadData(pagination.current, pagination.pageSize);
              } else {
                message.error(res?.message || '删除失败');
              }
            } catch (error) {
              console.error('删除优惠券失败', error);
              message.error('删除失败');
            }
          },
        });
      };

      // 重置表单
      const resetForm = () => {
        // 先将所有字段置为初始值
        Object.assign(formData, {
          id: null,
          name: '',
          type: 1,
          discount: null,
          threshold: null,
          amount: null,
          startTime: null,
          endTime: null,
          status: 1,
        });

        // 根据类型设置相关字段
        setFieldsByType(1);

        // 如果表单引用存在，重置校验状态
        nextTick(() => {
          formRef.value && formRef.value.resetFields();
        });
      };

      // 取消弹窗
      const handleCancel = () => {
        modalVisible.value = false;
      };

      // 格式化类型
      const formatType = (type) => {
        return type === 1 ? '满减券' : type === 2 ? '折扣券' : '未知';
      };

      // 格式化状态
      const formatStatus = (status) => {
        return status === 1 ? '启用' : '禁用';
      };

      // 挂载时加载数据
      onMounted(() => {
        // 先设置默认数据，确保界面有内容显示
        setDefaultData();
        // 然后尝试从API加载数据
        loadData();
        setFieldsByType(1);
      });

      return {
        tableData,
        filteredTableData,
        columns,
        pagination,
        loading,
        searchKeyword,
        filterType,
        filterStatus,
        handleSearch,
        handleReset,
        handleTableChange,
        handleAdd,
        handleEdit,
        handleDelete,
        modalVisible,
        modalTitle,
        modalConfirmLoading,
        handleSubmit,
        handleCancel,
        formRef,
        formData,
        rules,
        formatType,
        formatStatus,
        typePlaceholder,
        disabledDiscount,
        disabledAmount,
        handleTypeChange,
        setFieldsByType,
        getCouponTypeCount,
        getActiveCouponCount,
        getRowClassName,
        totalCoupons,
      };
    },
  };
</script>

<template>
  <div class="coupon-container">
    <!-- 顶部统计卡片 -->
    <div class="statistics-cards">
      <a-row :gutter="{ xs: 8, sm: 16 }">
        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card card-primary">
            <div class="stat-icon">
              <span class="anticon"><gift-outlined /></span>
            </div>
            <div class="stat-content">
              <div class="stat-title">优惠券总数</div>
              <div class="stat-value">{{ totalCoupons || 0 }}</div>
              <div class="stat-desc">所有优惠券数量</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card card-success">
            <div class="stat-icon">
              <span class="anticon"><money-collect-outlined /></span>
            </div>
            <div class="stat-content">
              <div class="stat-title">满减券</div>
              <div class="stat-value">{{ getCouponTypeCount(1) }}</div>
              <div class="stat-desc">满X元减Y元类型</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card card-warning">
            <div class="stat-icon">
              <span class="anticon"><percentage-outlined /></span>
            </div>
            <div class="stat-content">
              <div class="stat-title">折扣券</div>
              <div class="stat-value">{{ getCouponTypeCount(2) }}</div>
              <div class="stat-desc">打折类型优惠券</div>
            </div>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="12" :lg="6">
          <div class="stat-card card-info">
            <div class="stat-icon">
              <span class="anticon"><calendar-outlined /></span>
            </div>
            <div class="stat-content">
              <div class="stat-title">活动中优惠券</div>
              <div class="stat-value">{{ getActiveCouponCount() }}</div>
              <div class="stat-desc">当前正在进行中</div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-container">
      <a-card class="filter-card" :bordered="false">
        <a-row :gutter="{ xs: 8, sm: 16 }" class="filter-row">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" class="filter-col">
            <a-input v-model:value="searchKeyword" placeholder="搜索优惠券名称" allowClear class="search-input">
              <template #prefix>
                <search-outlined />
              </template>
            </a-input>
          </a-col>
          <a-col :xs="12" :sm="12" :md="6" :lg="4" class="filter-col">
            <a-select v-model:value="filterType" placeholder="选择优惠券类型" style="width: 100%" class="filter-select">
              <a-select-option :value="null">全部类型</a-select-option>
              <a-select-option :value="1">满减券</a-select-option>
              <a-select-option :value="2">折扣券</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="12" :sm="12" :md="6" :lg="4" class="filter-col">
            <a-select v-model:value="filterStatus" placeholder="选择状态" style="width: 100%" class="filter-select">
              <a-select-option :value="null">全部状态</a-select-option>
              <a-select-option :value="1">启用</a-select-option>
              <a-select-option :value="0">禁用</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="12" :sm="12" :md="8" :lg="6" class="filter-col">
            <div class="filter-btns">
              <a-button type="primary" @click="handleSearch" class="btn-search"> <search-outlined />搜索 </a-button>
              <a-button @click="handleReset" class="btn-reset"> <reload-outlined />重置 </a-button>
            </div>
          </a-col>
          <a-col :xs="12" :sm="12" :md="8" :lg="4" class="filter-col" style="text-align: right">
            <a-button type="primary" @click="handleAdd" class="btn-add"> <plus-outlined />添加优惠券 </a-button>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 表格区域 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :dataSource="filteredTableData"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
        :rowClassName="getRowClassName"
        :scroll="{ x: 1200 }"
      >
        <!-- 自定义类型列 -->
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'type'">
            <a-tag :color="record.type === 1 ? 'blue' : 'green'">
              {{ formatType(record.type) }}
            </a-tag>
          </template>

          <!-- 自定义折扣列 -->
          <template v-if="column.dataIndex === 'discount'">
            <span v-if="record.type === 2" class="highlight-text">{{ text * 10 }}折</span>
            <span v-else>-</span>
          </template>

          <!-- 自定义使用门槛列 -->
          <template v-if="column.dataIndex === 'threshold'">
            <span v-if="text"
              >满 <span class="highlight-text">{{ text }}元</span> 可用</span
            >
            <a-tag v-else color="purple">无门槛</a-tag>
          </template>

          <!-- 自定义优惠金额列 -->
          <template v-if="column.dataIndex === 'amount'">
            <span v-if="record.type === 1" class="highlight-text">{{ text }}元</span>
            <span v-else>-</span>
          </template>

          <!-- 自定义状态列 -->
          <template v-if="column.dataIndex === 'status'">
            <a-badge :status="record.status === 1 ? 'success' : 'error'" />
            <span>{{ formatStatus(record.status) }}</span>
          </template>

          <!-- 自定义操作列 -->
          <template v-if="column.dataIndex === 'action'">
            <div class="action-btns">
              <a-button type="primary" size="small" @click="handleEdit(record)" class="btn-edit"> <edit-outlined />编辑 </a-button>
              <a-button type="danger" size="small" @click="handleDelete(record)" class="btn-delete"> <delete-outlined />删除 </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 弹窗表单 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      :confirm-loading="modalConfirmLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
      destroyOnClose
      class="coupon-modal"
      width="700px"
    >
      <a-form ref="formRef" :model="formData" :rules="rules" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-divider>基本信息</a-divider>

        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="优惠券名称" name="name">
              <a-input v-model:value="formData.name" placeholder="请输入优惠券名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="优惠券类型" name="type">
              <a-radio-group v-model:value="formData.type" @change="handleTypeChange" button-style="solid">
                <a-radio-button :value="1"> <money-collect-outlined /> 满减券 </a-radio-button>
                <a-radio-button :value="2"> <percentage-outlined /> 折扣券 </a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>优惠设置</a-divider>

        <a-row :gutter="16">
          <a-col :span="formData.type === 1 ? 12 : 24">
            <a-form-item label="使用门槛" name="threshold">
              <a-input-number
                v-model:value="formData.threshold"
                :min="0"
                :step="1"
                placeholder="请输入使用门槛金额"
                style="width: 100%"
                addon-before="满"
                addon-after="元可用"
              />
              <div class="form-help">0表示无门槛</div>
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="formData.type === 1">
            <a-form-item label="优惠金额" name="amount">
              <a-input-number
                v-model:value="formData.amount"
                :min="0"
                :step="1"
                :disabled="disabledAmount"
                placeholder="请输入优惠金额"
                style="width: 100%"
                addon-after="元"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16" v-if="formData.type === 2">
          <a-col :span="24">
            <a-form-item label="折扣力度" name="discount">
              <a-slider
                v-model:value="formData.discount"
                :min="0"
                :max="1"
                :step="0.1"
                :disabled="disabledDiscount"
                :tooltip-visible="true"
                :marks="{
                  0: '0折',
                  0.5: '5折',
                  1: '10折',
                }"
              />
              <a-input-number
                v-model:value="formData.discount"
                :min="0"
                :max="1"
                :step="0.1"
                :disabled="disabledDiscount"
                style="width: 100%"
                addon-after="折"
              />
              <div class="form-help">例如：0.8 表示8折</div>
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>有效期设置</a-divider>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间" name="startTime">
              <a-date-picker
                v-model:value="formData.startTime"
                show-time
                :valueFormat="'YYYY-MM-DD[T]HH:mm:ss'"
                :format="'YYYY-MM-DD HH:mm:ss'"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间" name="endTime">
              <a-date-picker
                v-model:value="formData.endTime"
                show-time
                :valueFormat="'YYYY-MM-DD[T]HH:mm:ss'"
                :format="'YYYY-MM-DD HH:mm:ss'"
                placeholder="请选择结束时间"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider>状态设置</a-divider>

        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item label="优惠券状态" name="status">
              <a-switch
                :checked="formData.status === 1"
                checked-children="启用"
                un-checked-children="禁用"
                @change="(checked) => (formData.status = checked ? 1 : 0)"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
  .coupon-container {
    padding: 24px;
    background: #f0f2f5;
    min-height: calc(100vh - 64px);
  }

  /* 统计卡片样式 */
  .statistics-cards {
    margin-bottom: 24px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    height: 100%;
    transition: all 0.3s;
    overflow: hidden;
    position: relative;
  }

  .stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
  }

  .card-primary {
    background: linear-gradient(135deg, #1677ff, #2389ff);
    color: white;
  }

  .card-success {
    background: linear-gradient(135deg, #52c41a, #73d13d);
    color: white;
  }

  .card-warning {
    background: linear-gradient(135deg, #faad14, #ffc53d);
    color: white;
  }

  .card-info {
    background: linear-gradient(135deg, #13c2c2, #36cfc9);
    color: white;
  }

  .stat-icon {
    font-size: 32px;
    margin-right: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    padding: 12px;
    height: 60px;
    width: 60px;
  }

  .stat-content {
    flex: 1;
  }

  .stat-title {
    font-size: 16px;
    margin-bottom: 4px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 4px;
    line-height: 1.2;
  }

  .stat-desc {
    font-size: 12px;
    opacity: 0.8;
  }

  /* 过滤和搜索区域 */
  .filter-container {
    margin-bottom: 24px;
  }

  .filter-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .filter-row {
    margin-bottom: -16px;
  }

  .filter-col {
    margin-bottom: 16px;
  }

  .search-input,
  .filter-select {
    border-radius: 6px;
    width: 100%;
  }

  .filter-btns {
    display: flex;
    gap: 8px;
  }

  .btn-search,
  .btn-reset,
  .btn-add {
    display: flex;
    align-items: center;
    border-radius: 6px;
  }

  .btn-search i,
  .btn-reset i,
  .btn-add i {
    margin-right: 4px;
  }

  @media (max-width: 576px) {
    .filter-btns {
      justify-content: flex-start;
    }

    .filter-col[style*='text-align: right'] {
      text-align: left !important;
    }

    .statistics-cards .ant-col {
      margin-bottom: 16px;
    }
  }

  /* 表格区域样式 */
  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    margin-bottom: 24px;
    overflow: hidden;
  }

  .table-card :deep(.ant-card-body) {
    padding: 0;
  }

  .table-card :deep(.ant-table-wrapper) {
    width: 100%;
    overflow: auto;
  }

  .table-card :deep(.ant-table) {
    overflow: auto;
  }

  .table-card :deep(.ant-pagination) {
    margin: 16px;
  }

  /* 表格行样式 */
  .coupon-row-active {
    background-color: #fff;
  }

  .coupon-row-disabled {
    background-color: #f5f5f5;
    color: rgba(0, 0, 0, 0.45);
  }

  .coupon-row-expired {
    background-color: #fafafa;
    color: rgba(0, 0, 0, 0.45);
  }

  .coupon-row-upcoming {
    background-color: #e6f7ff;
  }

  /* 高亮文本 */
  .highlight-text {
    color: #1677ff;
    font-weight: bold;
  }

  /* 操作按钮样式 */
  .action-btns {
    display: flex;
    gap: 4px;
    justify-content: center;
  }

  .btn-edit,
  .btn-delete {
    padding: 0 8px;
    height: 24px;
    line-height: 22px;
    font-size: 12px;
  }

  .btn-edit {
    background-color: #1677ff;
    border-color: #1677ff;
  }

  .btn-delete {
    background-color: #ff4d4f;
    border-color: #ff4d4f;
  }

  /* 弹窗样式 */
  .coupon-modal {
    :deep(.ant-modal-content) {
      border-radius: 12px;
      overflow: hidden;
    }

    :deep(.ant-modal-header) {
      padding: 16px 24px;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.ant-modal-body) {
      padding: 24px;
      max-height: 70vh;
      overflow-y: auto;
    }

    :deep(.ant-modal-footer) {
      padding: 16px 24px;
      border-top: 1px solid #f0f0f0;
    }
  }

  /* 表单样式 */
  :deep(.ant-form-item-label) {
    font-weight: 500;
  }

  :deep(.ant-divider) {
    margin: 12px 0 24px;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
    font-size: 16px;
  }

  .form-help {
    color: rgba(0, 0, 0, 0.45);
    font-size: 12px;
    margin-top: 4px;
  }

  /* 添加动画效果 */
  .statistics-cards,
  .filter-card,
  .table-card {
    animation: fadeIn 0.5s ease-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
