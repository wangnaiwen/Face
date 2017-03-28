package com.wnw.face.login;

import android.text.TextUtils;

/**
 * Created by wxx on 2017/3/27.
 * 这个类主要提供判断用户输入的字符串是不是一个手机号码
 */
public class JudgePhoneNums {

    /**
     * 判断手机号码是否合理
     * 1. 判断字符串的位数
     * 2. 验证手机号码格式
     * */
    public boolean judgePhoneNums(String phoneNums){
        if(isMatchLength(phoneNums,11) && isMobileNums(phoneNums)){
            return true;
        }
        return false;
    }

    /**
     * 判断字符串的位数是不是
     * */
    public static boolean isMatchLength(String str,int length){
        if(str.isEmpty()){
            return false;
        }else{
            return str.length() == length ? true: false;
        }
    }

    /**
     * 验证手机格式
     * */
    public static boolean isMobileNums(String mobileNums){
        /**
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */

        // "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        String telRegex = "[1][358]\\d{9}";

        if(TextUtils.isEmpty(mobileNums)){
            return false;
        }else{
            return mobileNums.matches(telRegex);
        }
    }
}
