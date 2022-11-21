# Gobang
Save my sophomore homework

## 需求分析

本程序需要有一个主界面和一个下棋界面，在下棋页面点击即可下棋。

主界面需要有背景图片装饰，两个个按钮：开始游戏、退出。

下棋界面需要有棋盘区和控件区，背景需要纯色背景以提高白色棋子的对比度。
进入下棋界面时需要在左侧棋盘区绘制完整棋盘，右侧控件区有两个标签：显示当前是第几手棋、轮到谁下棋，
四个按钮：重新开始、保存棋盘、悔棋、载入上一局棋，载入上一局棋需要在棋盘为空白时才生效。

## 思路分析

棋子的每一步坐标可以用链表来记录，但是为了实现悔棋功能需要使用双向链表，不如直接使用栈。

只写了人机对战，加入人人对战难度也不高，只是不太想写了，AI判断最佳落子算法采用权值法。

## 下棋流程图
![流程图](https://github.com/jacpty/Gobang/blob/main/picture/flowChart.png)

## 环境

> Windows11系统
>
> IntelliJ IDEA 集成开发环境
>
> JDK17
