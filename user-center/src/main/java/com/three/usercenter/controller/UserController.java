package com.three.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.three.usercenter.common.BaseResponse;
import com.three.usercenter.common.ErrorCode;
import com.three.usercenter.common.ResultUtils;
import com.three.usercenter.constant.UserConstant;
import com.three.usercenter.exceptions.BusinessException;
import com.three.usercenter.model.domain.User;
import com.three.usercenter.model.domain.request.UserLoginRequest;
import com.three.usercenter.model.domain.request.UserRegisterRequest;
import com.three.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.three.usercenter.constant.UserConstant.ADMIN_ROLE;

/**
 * @author liusirui
 * @description 用户注册
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null){
//            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String email = userRegisterRequest.getEmail();
        if(userAccount == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号为空");
        }
        if(userPassword == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码为空");
        }
        if(checkPassword == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"校验密码为空");
        }
        if(email == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱为空");
        }
        Long id = userService.userRegister(userAccount, userPassword, checkPassword, email);
        return ResultUtils.success(id);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(userAccount == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号为空");
        }
        if(userPassword == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码为空");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        Integer id = userService.userLogout(request);
        return ResultUtils.success(id);
    }
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safeuser = userService.getSafeUser(user);
        return ResultUtils.success(safeuser);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user == null || user.getUserRole() != ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }
        return true;
    }
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user->userService.getSafeUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"id不得小于1");
        }
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无管理员权限");
        }
        Boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }
}
