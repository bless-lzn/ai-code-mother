package com.limou.aicodemother.service;

import com.limou.aicodemother.model.dto.app.AppQueryRequest;
import com.limou.aicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.limou.aicodemother.model.entity.App;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 李振南
 */
public interface AppService extends IService<App> {

    /**
     * 将 App 转换成 AppVO
     *
     * @param app 实体对象
     * @return AppVO
     */
    AppVO getAppVO(App app);
    /**
     * 获取查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取 AppVO 列表
     *
     * @param appList 实体列表
     * @return AppVO 列表
     */
    List<AppVO> getAppVOList(List<App> appList);
}
