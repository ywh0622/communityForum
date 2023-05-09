项目简介：该论坛是一个互动交流平台，实现了注册登陆、发帖评论、回复点赞、消息提醒功能和网站数据统计功能。
项目地址：https://gitee.com/yang-0622/springboot
技术栈：Spring Boot + Mybatis + MySQL + Redis + Kafka
工作内容：
    使用Redis存储登陆ticket和验证码
    使用Redis实现点赞, zset实现关注, HyperLogLog统计UV, Bitmap统计DAU
    使用kafka处理发送评论、点赞和关注等系统通知
    对热帖排行模块，使用分布式缓存redis和本地缓存caffeine作为多级缓存
