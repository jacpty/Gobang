import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 主界面面板，选择进入游戏的方式
 * */
public class MainGamePanel extends JPanel {
    public MainGamePanel(JFrame mainframe, JFrame frame){
        Container mainContainer = new Container();
        this.setMainPanel(mainframe, frame, mainContainer);//初始界面
        mainframe.setVisible(true);
        mainframe.validate();
        mainframe.repaint();
    }

    //主界面
    public void setMainPanel(JFrame mainframe, JFrame frame, Container container){
        //创建主界面的按钮
        JLabel backgroundLabel = new JLabel();
        JButton start = new JButton("开始游戏！");
        JButton quit = new JButton("退出游戏");

        //创建图片，用ImageIcon存放图片，然后将背景设置成图片
        ImageIcon background = new ImageIcon(Objects.requireNonNull(MainGamePanel.class.getResource("statics/background.jpg")));
        background.setImage(background.getImage().getScaledInstance(background.getIconWidth(),
                background.getIconHeight(),Image.SCALE_DEFAULT)); //设置图片格式

        //设置各个控件的位置和大小
        container.setLayout(null);
        backgroundLabel.setBounds(0,0,1262,710);
        backgroundLabel.setHorizontalAlignment(0);
        backgroundLabel.setIcon(background);
        start.setBounds(550,450,150,75);
        quit.setBounds(550,525,150,75);
        //将各个控件添加到container中
        mainframe.add(container);
        container.add(start);
        container.add(quit);
        container.add(backgroundLabel);

        //start的事件监听，单击清空并绘制空白棋盘
        start.addActionListener(e -> {
            if (e.getSource() == start){
                mainframe.setVisible(false);
                frame.add(new GamePanel(frame));
                frame.validate();
                frame.repaint();
            }
        });
        //quit的事件监听，单机退出游戏（关闭程序）
        quit.addActionListener(e -> {
            if (e.getSource() == quit){
                System.exit(0); //退出程序
            }
        });
    }
}