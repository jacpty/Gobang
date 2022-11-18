import javax.swing.*;

public class StartGame extends JFrame {
    public static void main(String[] args) {
        JFrame mainframe = new JFrame("AlphaDog - Chess");
        JFrame frame = new JFrame("AlphaDog - 下棋");
        //主界面
        mainframe.setBounds(100,100,1262,710);
        mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainframe.setResizable(false); //禁止窗口缩放
        //游戏界面
        frame.setBounds(100,100,800,600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        //游戏界面都放在面板上
        mainframe.add(new MainGamePanel(mainframe, frame));
        mainframe.setVisible(true);
        mainframe.validate();
        mainframe.repaint();
    }
}
