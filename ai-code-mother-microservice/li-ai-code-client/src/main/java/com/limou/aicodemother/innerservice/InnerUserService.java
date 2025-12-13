package com.limou.aicodemother.innerservice;

import com.limou.aicodemother.constant.UserConstant;
import com.limou.aicodemother.exception.BusinessException;
import com.limou.aicodemother.exception.ErrorCode;
import com.limou.aicodemother.model.entity.User;
import com.limou.aicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface InnerUserService {

    List<User> listByIds(Collection<? extends Serializable> ids);

    User getById(Serializable id);

    UserVO getUserVO(User user);

    // 静态方法，避免跨服务调用
    static User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}

