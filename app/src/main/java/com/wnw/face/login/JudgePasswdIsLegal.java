package com.wnw.face.login;

/**
 * Created by wxx on 2017/3/3.
 * 这个类是用来判断用户输入的密码是不是满足以下条件：
 * 1. 密码中必须包含数字和字母
 * 2. 密码必须满足正则表达式的格式
 */
public class JudgePasswdIsLegal {

    /**判断用户输入的密码是不是满足合法性*/
    public boolean passwdIsLegal(String passwd){
        if(isMatchLength(passwd) && isLetterDigit(passwd)){
            return true;
        }
        return false;
    }

    /**判断字符串长度是不是6-20位*/
    public static boolean isMatchLength(String str){
        if(str.isEmpty()){
            return false;
        }else{
            if(str.length()<6 || str.length() >20){
                return false;
            }
        }
        return true;
    }

    /**判断
     * 1. 字符串中是不是包含了数字和字母
     * 2. 是否满足正则表达式中的要求
     * 3. 只有同时满足上面两个条件，才会返回true,否则返回false
     * */
    public static boolean isLetterDigit(String str){

        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
        boolean isLetter = false;//定义一个boolean值，用来表示是否包含字母

        for(int i=0 ; i<str.length();i++){
            if(Character.isDigit(str.charAt(i))){   //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            }
            if(Character.isLetter(str.charAt(i))){  //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }

        }

        /**目前这个表达式只能判断0-9，a-z,A-Z中的字符，如果是其他半角字符，暂时还不能判断*/
        String regex = "^[a-zA-Z0-9]+$";
        boolean isRight = isDigit && isLetter && str.matches(regex);
        return isRight;
    }
}
