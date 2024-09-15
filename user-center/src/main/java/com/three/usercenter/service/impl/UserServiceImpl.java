package com.three.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.three.usercenter.common.ErrorCode;
import com.three.usercenter.exceptions.BusinessException;
import com.three.usercenter.service.UserService;
import com.three.usercenter.model.domain.User;
import com.three.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.three.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author liusirui
* @description 针对表【user(用户)】的数据库操作Service实现
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "three";

    // TODO 邮箱认证
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String email) {
        //  1.1 校验不能为空
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
        //  1.2 校验账号长度
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        // 1.3 校验密码长度
        if(userPassword.length()<6 || checkPassword.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        // 1.4 校验密码与检验密码相同
        if(!checkPassword.equals(userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"校验密码与检验密码不同");
        }
        // 1.5 校验特殊字符
        String validPattern = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号含有特殊字符");
        }
        // 1.6 校验账号不重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("userAccount", userAccount);
        //long count = this.count(queryWrapper); //service方法
        long count = userMapper.selectCount(queryWrapper); //mapper方法
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已注册");
        }
        // 1.7 邮箱不能重复
        queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("email", email);
        //count = this.count(queryWrapper); //service方法
        count = userMapper.selectCount(queryWrapper); //mapper方法
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"邮箱已注册");
        }
        //TODO 1.8 是否为武大邮箱

        // 2 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setEmail(email);
        boolean saveResult = this.save(user);
        if(!saveResult){
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //  1.1 校验不能为空
        if(userAccount == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号为空");
        }
        if(userPassword == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码为空");
        }
        //  1.2 校验账号长度
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        // 1.3 校验密码长度
        if(userPassword.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        // 1.4 校验特殊字符
        String validPattern = "[ `~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号中不得含有特殊字符");
        }
        // 2 密码加密
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在");
        }
        if (!encryptedPassword.equals(user.getUserPassword())){
            log.info("User login failed, incorrect password.");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 4 用户脱敏
        User safeUser = getSafeUser(user);
        // 5 记录登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        return safeUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 用户脱敏函数
     *
     * @param originalUser
     * @return
     */
    @Override
    public User getSafeUser(User originalUser){
        if(originalUser == null){
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originalUser.getId());
        safeUser.setUsername(originalUser.getUsername());
        safeUser.setUserAccount(originalUser.getUserAccount());
        safeUser.setAvatarUrl(originalUser.getAvatarUrl());
        safeUser.setGender(originalUser.getGender());
        safeUser.setPhone(originalUser.getPhone());
        safeUser.setEmail(originalUser.getEmail());
        safeUser.setUserStatus(originalUser.getUserStatus());
        safeUser.setCreateTime(originalUser.getCreateTime());
        safeUser.setUserRole(originalUser.getUserRole());
        return safeUser;
    }
}




