package edu.util;

/**
 * 示例：你们可替换成 Spring Security / Sa-Token / JWT。
 *
 * 当前项目里 Notice 模块也有一份同名类，这里为 Manage 模块单独放一份，避免模块依赖耦合。
 */
public class SecurityUtil {

    public static Long getUserId() {
        // TODO: 从当前登录态获取
        return 1002L;
    }

    public static Long getDeptId() {
        // TODO: 从当前登录态获取
        return 1002L;
    }
}
