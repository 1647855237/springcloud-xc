package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: xiongwei
 * @Date: 2019/12/9
 * @why：
 */
@Service
public class UserService {

    @Autowired
    private XcUserRepository xcUserRepository;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    @Autowired
    private XcCompanyUserRepository xcCompanyUserRepository;


    /**
     * 根据用户账号查询用户信息
     * @param username
     * @return
     */
    public XcUser findXcUserByUsername(String username){
        return xcUserRepository.findXcUserByUsername(username);
    }

    /**
     * /根据账号查询用户的信息，返回用户扩展信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username){
        // 用户信息
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null) {
            return null;
        }
        // 用户id
        String id = xcUser.getId();
        // 查询用户所属公司
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(id);
        String compangyId = null;
        if (xcCompanyUser != null) {
            compangyId = xcCompanyUser.getCompanyId();
        }
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(id);
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(compangyId);
        xcUserExt.setPermissions(xcMenus);
        return xcUserExt;
    }



}
