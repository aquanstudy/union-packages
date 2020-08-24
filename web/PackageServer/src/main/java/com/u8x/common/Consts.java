package com.u8x.common;

/**
 * Created by Administrator on 2017/5/14.
 */
public class Consts {

    public static final class RespCode {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
        public static final int ReLogin = 2;
        public static final int SUCWITHWARN = 3;
    }

    public static final class Tips{
        public static final String FailMsg = "操作失败";
        public static final String SucMsg = "操作成功";
        public static final String UserNameError = "用户名错误";
        public static final String PasswordError = "密码错误";
        public static final String UPError = "用户名或密码错误";
        public static final String VCodeError = "验证码错误";
        public static final String ALOCK = "重试次数过多，账号被锁定，请30分钟后再试";
        public static final String SessionInvalid = "登录失效";
        public static final String UserNameExists = "用户名已存在";
        public static final String UserNotExists = "角色不存在";
        public static final String AdminNotExists = "管理员不存在";
        public static final String GameNameExists = "游戏已存在";
        public static final String GameNotExists = "游戏不存在";
        public static final String ChannelNameExists = "渠道名称已存在";
        public static final String ChannelExists = "渠道已存在";
        public static final String ChannelNotExists = "渠道不存在";
        public static final String ChildrenExists = "该菜单下面还存在子菜单，请先删除所有子菜单";
        public static final String NO_PERMISSION = "没有操作权限";
        public static final String RESEND_ERROR = "订单补发出现异常";
        public static final String CpExists = "该CP名字已存在";
        public static final String CpNotExists = "该CP不存在";
        public static final String ProductExists = "该商品已存在";
        public static final String PackingNotDelete = "正在打包，无法删除";
        public static final String PackLogNotExists = "打包记录不存在";
        public static final String PackNotSuccess = "本次打包没有成功";
        public static final String ChannelNotSpecified = "请指定一个渠道";
        public static final String TestCaseNotExists = "该渠道无测试用例";
        public static final String MetaNotConfig = "该渠道没有配置参数meta";
        public static final String SDKNotConfig = "本地渠道SDK目录不存在";
        public static final String APKNotSpecified = "母包未指定";
        public static final String PNAMENOTVALID = "包名不合法";
        public static final String KeystoreFileExtInvalid = "证书文件必须以.keystore或.jks为文件名后缀";
    }


    public static final class Pay {
        public static final class Status {
            public static final int NONE = 0;
            /**
             * 订单创建
             */
            public static final int CREATE = 1;
            /**
             * 订单提交(第三方支付平台确认)
             */
            public static final int COMMIT = 2;
            /**
             * 订单完成
             */
            public static final int FINISH = 3;
            /**
             * 订单失败
             */
            public static final int FAILURE = 4;
        }

        /**
         * 支付平台
         */
        public static final class Type {
            /**
             * 支付宝
             */
            public static final int Alipay = 1;
            /**
             * 银联
             */
            public static final int Union = 2;
            /**
             * 微信
             */
            public static final int Weixin = 3;
            /**
             * 代币
             */
            public static final int Jeton = 4;
        }

        /**
         * 订单类型
         */
        public static final class OrderType{

            public static final int GameCharge = 1;     //游戏充值，购买游戏道具
            public static final int CoinCharge = 2;     //平台币充值，充值平台币
        }

        /**
         * 支付结果
         */
        public static final class Result {
            /**
             * 未支付
             */
            public static final int NotPay = 0;
            /**
             * 支付成功
             */
            public static final int PaySuccess = 1;
            /**
             * 支付失败
             */
            public static final int PayFailure = 2;
        }


    }

    /**
     * 通知游戏服状态
     */
    public static final class NotifyStatus {
        /**
         * 未通知
         */
        public static final int NotNotify = 0;
        /**
         * 已通知
         */
        public static final int NotifySuccess = 1;

        public static final int NotifyFailed = 2;
    }
}
