Typesafe Activator template for distributed workers with Akka Cluster.
词范例借用了typesafe的分布式worker范例进行了改造

使用scopt增强了run启动的可配置化

利用Akka HTPP进行发送job
POST http://127.0.0.1/job

使用sbt native packager打包程序

此项目分成了root和frontend并可发布成两个docker image

利用circle ci直接发布到docker hub

包含docker-composer范例

以下为常用操作
> sbt
> projects
可看到root与frontend
> project root
切换到root
> project frontend
切换到frontend

打包或发布
> project root
> docker:publishLocal
建立本机docker image
> docker:publish
发布到docker hub
> stage
到target/universal/stage/bin 可在本地运行无需通过sbt

docker composer范例
> docker-compose up -d
> docker-compose ps
> docker-compose log
> docker-compose stop
> docker-compose rm -v -f
