# PremiumVerify

[![Stars](https://img.shields.io/github/stars/MrXiaoM/PremiumVerify?style=plastic)](https://github.com/MrXiaoM/PremiumVerify//stargazers) [![Downloads](https://img.shields.io/github/downloads/MrXiaoM/PremiumVerify/total)](https://github.com/MrXiaoM/PremiumVerify/releases)

Minecraft 离线模式服务器正版验证插件。

想要给予拥有 `Minecraft: Java Edition` 正版账号的玩家一些特殊奖励吗？使用本插件即可实现！

可配置项如下
+ 每个正版账号可验证次数
+ 每个玩家可尝试验证次数
+ 验证请求超时时间
+ 玩家拥有什么权限时视为已验证正版，不允许再验证
+ 验证后奖励，可设置多个`控制台命令`、`玩家命令`、`消息提示`、`全服公告`等等，支持使用 PAPI 变量

仅支持微软账号验证。

## 命令

根命令: `/permiumverify`, `/verify`, `/pv`  
其中 `<>` 包裹的为必选参数，`[]` 包裹的为可选参数。
```
/pv request - 请求正版验证，最多可尝试3次(配置文件中可配置次数)
/pv fail <玩家> <次数> - 设置玩家的失败次数
/pv reload - 重载配置文件
```

## 权限

```
premiumverify.request - 允许使用 /pv request 命令
premiumverify.request.bypass-fail-limit - 无视失败次数限制
premiumverify.fail - 允许使用 /pv fail 命令
premiumverify.reload - 允许使用 /pv reload 命令
```
