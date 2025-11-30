package com.limou.aicodemother.service;

import com.limou.aicodemother.model.dto.app.AppQueryRequest;
import com.limou.aicodemother.model.entity.User;
import com.limou.aicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.limou.aicodemother.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 李振南
 */
public interface AppService extends IService<App> {

    /**
     * AI生成代码
     *
     * @param appId      应用 id
     * @param message    消息
     * @param loginUser  登录用户
     * @return 生成的代码
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

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

    /**
     * 部署应用
     *
     * @param appId     应用 id
     * @param loginUser 登录用户
     * @return 部署结果
     */
    String deployApp(Long appId, User loginUser);
}
