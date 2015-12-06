Typesafe Activator template for distributed workers with Akka Cluster.

借用了typesafe的分布式worker template进行了改造

使用scopt增强了run启动的可配置化

利用Akka HTPP进行发送job
POST http://127.0.0.1/job

使用sbt native packager打包程序

此项目分成了root和frontend并可发布成两个docker image

利用circle ci直接发布到docker hub

包含docker-composer范例
