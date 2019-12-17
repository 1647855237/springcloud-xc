package com.xuecheng.test.freemarker.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/9/15
 * @why：
 */
public class TestFinally {
    public static void main(String[] args) {
        TestFinally testFinally = new A();
        HashMap map = new HashMap();
        testFinally.a(map
        );

    }

    public Collection a(HashMap map) {
        System.out.println('父');
        return map.values();
    }

}

class A extends TestFinally {

    public Collection a(Map map) {
        System.out.println("字");
        return map.values();
    }
}