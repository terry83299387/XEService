<!--
  本文档采用Markdown编写。
  作者：QiaoMingkui
  日期：201511
-->

## 一、整体架构

<br/>

> ```注意：程序的顶层包为xextension，文章后面将省略该包名（例如以Main表示类xextension.Main）。```

1. 概述

	本程序（XeXtension）实现为一个小型Web服务器，用来为Xfinity提供扩展功能。程序通过HTTP协议与Xfinity交互，基本步骤为：

    * Xfinity通过`GET`方式向XeXtension发送请求
    * XeXtension接收请求后根据请求中的`op`参数来决定调用哪个服务来处理请求
    * 请求处理完成，返回结果

<br/>

2. 主要的类、包和其他文件：

    * `Main`类为整个程序的入口
    * `autoupdate.AutoUpdater`类实现自动更新
    * `service.XEService`类是一个服务管理器，用来派发请求到具体的服务类
    * `operation`包中包含具体服务相关的类（例如文件选择、文件传输等）。其中的抽象类`Processor`是所有具体服务类的基类
    * `http`包包含`HTTP`相关的类，目前有`Request`和`Response`2个类
    * `global`包包含全局的配置相关类和其他辅助类
    * `resources`文件夹中包含配置文件和其他资源文件
    * `libs`文件夹存放本程序用到的第三方库
    * `extlibs`中的jar包是1.6版的JRE中的ext文件夹中的扩展包。由于本程序的运行参数会覆盖`java.ext.dirs`虚拟机参数，而该参数原本指向的是该文件夹。所以将其中的jar包也拷贝过来，可避免在用到时出现错误。
    * `scripts`文件夹中存放程序安装和卸载脚本
    * `build.xml`文件是ant脚本文件，用来使用ant编译、打包本程序

<br/>

3. 目前提供的功能与服务：

    <table>
        <tr>
            <td>op（操作码）</td>
            <td>所在类或包</td>
            <td>说明</td>
        </tr>
        <tr>
            <td>5</td>
            <td>operation.VersionInfo</td>
            <td>返回XeXtension的版本信息</td>
        </tr>
        <tr>
            <td>1</td>
            <td>operation.EchoBack</td>
            <td>用于调试。返回请求信息（包括HTTP头部和参数）</td>
        </tr>
        <tr>
            <td>2</td>
            <td>operation.file_browser</td>
            <td>提供选择本地文件的功能，支持多选和扩展名过滤</td>
        </tr>
        <tr>
            <td>3</td>
            <td>operation.file_transfer</td>
            <td>文件传输。在用户本地和远程集群存储之间提供文件上传和下载功能</td>
        </tr>
        <tr>
            <td>4</td>
            <td>operation.run_app</td>
            <td>在用户本地运行指定程序</td>
        </tr>
    </table>

<br/>

## 二、如何扩展

<br/>

当需要新增一个功能时，遵循以下步骤：

1. 在`operation`包中新建一个包
2. 定义一个类继承基类`operation.Processor`
3. 为新的服务定义一个操作码`op`，同时在`service.ServiceDispatcher`类中扩展`getProcessor`方法，使其能够根据该操作码返回新定义的服务类的对象
4. 修改ant脚本文件`build.xml`，为新定义的服务增加一个打包操作（jar包的文件名使用全小写，例如filebrowser.jar而不是fileBrowser.jar，这可以避免很多文件名大小写相关的问题）
5. 修改config.properties配置文件，更改版本号字段`customer.version`
6. 使用ant打包，发布新版本，同时将代码发生了修改的jar包发布为补丁（见“打包与发布”一节）

<br/>

## 三、打包与发布

<br/>

当有新版本时，需要对程序进行打包和发布。本程序采用ant进行打包。更新时，除了将整个程序进行打包和发布外，还要将发生了修改的部分文件和jar包发布为更新补丁。

本程序打包策略为：首先将核心功能打包为一个jar包；同时将每个服务（`operation`中的子包）都打包为独立的jar包；配置文件、脚本等文件不打包。同时jar包的文件名全部小写。

这种策略可以减少每次更新补丁的大小，因为只需要将修改过的包发布为更新补丁即可。

完整安装包的发布方法为：将所有文件打包为压缩文件，文件名为`XeXtension+版本号+压缩包扩展名`，例如`XeXtension1.0.zip`。推荐使用`zip`方式打包。最后将该安装包放入Xfinity中指定目录下，并更新`PluginManager.js`文件中与版本相关的部分即可。

更新补丁的发布方法为：在Xfinity中的更新目录下（`xexupdate`），在`patches`目录中新建一个与新版本的版本号同名的文件夹，将发生了改动的文件按照与安装包中相同的目录关系放到该文件夹下。例如更新了`libs`目录中的`xxx.jar`，则同样要在新的补丁文件夹中创建`libs`目录，然后将`xxx.jar`放入其中。

然后修改`xexupdate`目录中的`update.log`文件，在其顶部增加新版本，并列出所有补丁文件。自动更新时将会根据该列表下载补丁文件。

更详细信息可参考`xexupdate`中的`README.txt`文件。

<br/>

## 四、关于config.properties配置文件

<br/>

`config.properties`是全局配置文件。关于里面包含的具体项这里就不细说了。下面主要说一说配置项的2大分类。

所有配置项分为2大类，一类是不以`customer`开头的项，另一类是以`customer`开头的项。

前者（不以`customer`开头的）表示这一项不由用户配置，是一个固定的配置项（除非版本更新，更改了这一项的值）。而其他以`customer`开头的则表示这是一个会根据用户的情况而修改的项。

例如，`customer.version`是一个`customer项`，其值会随着每次版本更新而改变。

而`autoupdate.default_servers`则不是一个`customer项`，它的值在用户安装后或最后一次更新了`config.properties`文件后就保持改变了。

在自动更新更新了新版本的补丁时，如果`config.properties`有改动，将会采用以下策略来合并2个新旧配置文件：

* 所有`非customer项`都会更新为新版本配置文件中的值
* 检查用户现有的配置文件，用其中`customer项`的值覆盖新版本配置文件中的值

最后，更新后的配置文件中所有`非customer项`和新加入的`customer项`与新版本保持一致，而那些原来就存在的`customer项`则不会被更新。

<br/>

### 什么时候用`customer`？

当一个配置项需要依赖于用户的具体环境和行为，也就是说，不同用户之间该配置项的值可能不同的话，就应该用`customer`。例如`customer.autoupdate`是一个`customer项`，因为用户可以决定是否开启自动更新；`customer.autoupdate.server`也是一个`customer项`，因为用户访问Xfinity的地址可能不同（专线用户和非专线用户），该配置项保存了用户实际访问Xfinity的地址。

再比如，`autoupdate.default_servers`项保存了默认的Xfinity地址，如果`customer.autoupdate.server`为空或不可用，则会使用该默认地址。默认地址不需要由用户修改，所以该配置项不是一个`customer项`

为什么将`customer.version`项设置为一个`customer`项，主要是因为这样一来，用户就不需要在每次版本更新时都更新`config.properties`配置文件了（因为更新记录`update.log`中已经有新版本的信息）。

<br/>

## 五、

