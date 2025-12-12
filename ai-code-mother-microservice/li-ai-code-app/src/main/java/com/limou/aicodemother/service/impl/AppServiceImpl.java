package com.limou.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.limou.aicodemother.ai.AiCodeGenTypeRoutingService;
import com.limou.aicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.limou.aicodemother.ai.AiCodeGeneratorServiceFactory;
import com.limou.aicodemother.ai.core.AiCodeGeneratorFacade;
import com.limou.aicodemother.ai.core.builder.VueProjectBuilder;
import com.limou.aicodemother.ai.core.handle.StreamHandlerExecutor;
import com.limou.aicodemother.ai.model.enums.CodeGenTypeEnum;
import com.limou.aicodemother.constant.AppConstant;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.exception.ThrowUtils;
import com.limou.aicodemother.innerservice.InnerScreenshotService;
import com.limou.aicodemother.innerservice.InnerUserService;
import com.limou.aicodemother.mapper.AppMapper;
import com.limou.aicodemother.model.dto.app.AppAddRequest;
import com.limou.aicodemother.model.dto.app.AppQueryRequest;
import com.limou.aicodemother.model.entity.App;
import com.limou.aicodemother.model.entity.User;
import com.limou.aicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.limou.aicodemother.model.vo.AppVO;
import com.limou.aicodemother.model.vo.UserVO;
import com.limou.aicodemother.service.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author 李振南
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    @Lazy
    private InnerUserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;


    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;


    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    @Lazy
    private InnerScreenshotService screenshotService;


//    @Resource
//    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    /**
     * 聊天生成代码
     *
     * @param appId     应用ID
     * @param message   消息-提示词
     * @param loginUser 登录用户
     * @return 生成的代码
     */

    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1.参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0 || message == null, ErrorCode.PARAMS_ERROR);
        //2.查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //3.权限校验，只有自己才能和自己对话
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        //4.获取应用的代码生成类型
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        //4.1添加用户信息到chat_message中
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        //5.调用AI生成代码,并且将ai生成的信息保存到chat_history中
        StringBuilder aiResponseBuilder = new StringBuilder();
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, enumByValue, appId);
        //对话的时候进行异步构建
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, enumByValue);


        //                () -> {
//                    String aiResponse = aiResponseBuilder.toString();
//                    if (StrUtil.isBlank(aiResponse)) {
//                        return;
//                    }
//                    //7.添加AI生成的信息到chat_message中
//                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                }
//        ).doOnError(error -> {
//            //如果AI回复消息失败也要记录一下
//            String errorMessage = "AI回复失败：" + error.getMessage();
//            chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//        });
    }

    /**
     * 将 App 转换成 AppVO
     *
     * @param app 实体对象
     * @return AppVO
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    /**
     * 获取查询条件对象
     *
     * @param appQueryRequest 查询条件请求
     * @return 查询条件对象
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 获取 AppVO 列表
     *
     * @param appList 实体列表
     * @return AppVO 列表
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * @param appId     应用id
     * @param loginUser 登录用户
     * @return 可访问路径
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        //1.权限校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        //2.查询应用信息
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //3.验证用户是否有权限
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR, "无操作权限");
        //4.检查是否已经有deployKey,没有就生成
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            //生成6位随机数
            deployKey = RandomUtil.randomString(6);
        }
        String codeGenType = app.getCodeGenType();
        //5.获取代码的生成路径
        String genePath = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, codeGenType, appId);
        File sourceFile = new File(genePath);
        //6.检查原目录是否存在
        if (!FileUtil.exist(genePath) || !sourceFile.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "代码生成目录不存在");
        }
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            //vue的项目需要构建。。。。再次进行构造更加保险一点。
            boolean buildSuccess = vueProjectBuilder.buildProject(genePath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "构建失败");
            //检查dist目录是否存在
            File distDir = new File(genePath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.OPERATION_ERROR, "部署路径不存在");
            genePath = distDir.getPath();
            log.info("构建成功");
        }

        //7.复制文件到部署目录
        String deployPath = String.format("%s/%s/", AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey);
        FileUtil.copyContent(FileUtil.file(genePath), new File(deployPath), true);

        //8.更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        this.updateById(updateApp);
        //9.返回部署可以访问的路径
        String deployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        //10.异步处理生成和保存截图
        geneAndSaveScreenshot(appId, deployUrl);
        return deployUrl;

    }

    public void geneAndSaveScreenshot(Long appId, String webUrl) {
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        Thread.startVirtualThread(() -> {
            String coverUrl = screenshotService.generateAndUploadScreenshot(webUrl);
            App build = App.builder().cover(coverUrl).id(appId).build();
            boolean updateSuccess = this.updateById(build);
            if (!updateSuccess) {
                log.error("更新应用封面失败");
            }
        });

    }

    /**
     * 删除（逻辑删除）
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean removeById(Serializable id) {
//        关联删除
        if (id == null)
            return false;
        //先删除成功对话消息
        long appId = Long.parseLong(id.toString());
        chatHistoryService.deleteByAppId(appId);
        return super.removeById(id);
    }


    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param loginUser
     * @return
     */

    @Override
    public Long createApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 使用 AI 智能选择代码生成类型
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.aiCodeGenTypeRoutingService();
        CodeGenTypeEnum selectedCodeGenType = aiCodeGenTypeRoutingService.routerCodeGenType(initPrompt);

        app.setCodeGenType(selectedCodeGenType.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), selectedCodeGenType.getValue());
        return app.getId();
    }

}
