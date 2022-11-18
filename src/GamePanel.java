import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * 游戏界面，实现下棋功能
 * */
public class GamePanel extends JPanel implements Config{
    Graphics g;
    int round = 1; //第几手，单数为玩家，双数为电脑
    int oldx, oldy; //临时存放鼠标点击的坐标
    public Boolean over = false; //判断棋局是否结束
    public static Boolean load = false; //是否需要加载
    public int[][] board = new int[15][15]; //棋盘，1为玩家棋子，2是电脑棋子，0是空
    public Deque<StackElem> stack = new ArrayDeque<>(); //用栈将坐标保存起来
    public ArrayList<ListElem> strings = new ArrayList<>(); //从文件中读取到的上一局游戏进度

    //创建文本框
    JTextField countGame = new JTextField("第" + round + "手");
    JTextField player = new JTextField("黑先");
    //创建空白棋盘的布局
    JButton restart = new JButton("重新开始");
    JButton save = new JButton("保存棋局");
    JButton regret = new JButton("悔棋");
    JButton loadData = new JButton("载入上一局");

    //栈的元素，使同一层栈可以储存两个int
    public static class StackElem {
        int x = 0;
        int y = 0;
    }
    //可变列表的元素
    public static class ListElem {
        String[] str;
    }

    public GamePanel(JFrame frame){
        addMouseListener(new getMousePosition());
        Container container1 = new Container(); //创建容器存放控件
        this.setGamePanel(frame, container1); //初始化控件

        frame.setVisible(true);
    }

    //初始化控件
    public void setGamePanel(JFrame frame, Container container){
        //设置各个控件的位置和大小
        container.setLayout(null);
        countGame.setEditable(false); //设置不可编辑
        player.setEditable(false);
        this.setBackground(Color.lightGray);

        frame.setBounds(100,100,800,640);//游戏界面大小
        countGame.setBounds(650,20,100,50);
        player.setBounds(650,70,100,50);
        restart.setBounds(650,120,100,50);
        save.setBounds(650,170,100,50);
        regret.setBounds(650,220,100,50);
        loadData.setBounds(650,270,100,50);

        //将各个控件添加到container中
        frame.add(container);
        container.add(countGame);
        container.add(player);
        container.add(restart);
        container.add(save);
        container.add(regret);
        container.add(loadData);

        //重新开始的事件监听，清空棋盘并全部重新初始化
        restart.addActionListener(e -> {
            if (e.getSource() == restart){
                this.restart(frame);
            }
        });
        //保存棋局的事件监听，将残局用文件保存起来后退出游戏
        save.addActionListener(e -> {
            if (e.getSource() == save){
                try {
                    save();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            System.exit(0);
        });
        //悔棋的事件监听，单击悔棋，回到上一次落子
        regret.addActionListener(e -> {
            if (e.getSource() == regret){
                this.regert(g);
            }
        });
        //加载上一局棋
        loadData.addActionListener(e -> {
            if (e.getSource() == loadData){
                if (round == 1){
                    load = true;
                    mouseClick();
                }else {
                    JOptionPane.showMessageDialog(null, "棋局已经开始，请点击重新开始重置棋局！");
                }

            }
        });
    }

    //初始化界面
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        //画出棋盘
        for (int i=0; i<15; i++){
            g.drawLine(50,i*Config.GRID+20,610,i*Config.GRID+20); //横线
            g.drawLine(i*Config.GRID+50,20,i*Config.GRID+50,580); //竖线
        }
        //天元和星位
        g.fillOval(325,295,10,10);
        g.fillOval(165,135,10,10);
        g.fillOval(485,135,10,10);
        g.fillOval(165,455,10,10);
        g.fillOval(485,455,10,10);
    }

    //鼠标监听事件，记录鼠标点击坐标
    class getMousePosition extends MouseAdapter{
        public void mouseClicked(MouseEvent e) {
            if (!over) {
                oldx = e.getX();
                oldy = e.getY();
                mouseClick();
            }
        }
    }

    //记录下有效的鼠标点击
    public void mouseClick() {
        setVisible(true);
        g = this.getGraphics();
        if (load){
            paintLoad(g);
            load = false;
        }
        if (!this.over) {
            if (round%2 == 1){
                if ((this.oldx > 30 && this.oldx < 630) && (this.oldy < 600)) {
                    //将鼠标按下的坐标转化为棋盘上的坐标
                    StackElem s1 = new StackElem();
                    int m = (oldx - 30) / 40;
                    int n = oldy / 40;
                    s1.x = m;
                    s1.y = n;
                    if (this.board[m][n] == 0) {
                        this.board[s1.x][s1.y] = 1;
                        stack.push(s1); //棋子坐标压入栈
                        this.updatePaint(g);
                        this.getBestPoint();
                        this.updatePaint(g);
                    }
                }
            }
        }
    }

    //重新开始游戏
    public void restart(JFrame frame){
        frame.dispose(); //关闭窗口并新建
        frame.add(new GamePanel(frame));
        frame.validate();
        frame.repaint();

        //重新初始化
        round = 1;
        over = false;
        board = new int[15][15];
        stack = new ArrayDeque<>();
        Weight.weightArray = new int[15][15];
    }

    //保存游戏
    public void save() throws IOException {
        Deque<StackElem> tempStack = new ArrayDeque<>();
        //用另外一个栈临时存放坐标，调转栈的方向，方便写入文件后读取
        while (!stack.isEmpty()){
            StackElem s = new StackElem();
            s.x = stack.peek().x;
            s.y = stack.peek().y;
            stack.pop();
            tempStack.push(s);
        }

        //将临时栈中的数据写入到文件中
        FileOutputStream fop = new FileOutputStream(filePath);
        OutputStreamWriter writer = new OutputStreamWriter(fop, StandardCharsets.UTF_8);
        try {
            while (!tempStack.isEmpty()){
                assert tempStack.peek() != null;
                writer.append(String.valueOf(tempStack.peek().x)).append(" ");
                assert tempStack.peek() != null;
                writer.append(String.valueOf(tempStack.peek().y)).append("\n");
                tempStack.pop();
                writer.flush();
            }
            writer.close();
            fop.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    //悔棋
    public void regert(Graphics g){
        for (int i=0; i<2; i++) {
            //判断栈不为空
            int x, y;
            if (!stack.isEmpty()) {
                assert stack.peek() != null;
                assert stack.peek() != null;
                x = stack.peek().x;
                y = stack.peek().y;
                board[x][y] = 0;
                stack.pop();
            }else {
                JOptionPane.showMessageDialog(null, "无棋可悔！");
                break;
            }

            g.clearRect(x*GRID+40,y*GRID+10, Config.CHESS, Config.CHESS); //擦除棋子所在区域
            g.setColor(Color.lightGray);
            g.fillRect(x*GRID+40,y*GRID+10, Config.CHESS, Config.CHESS); //把被擦掉的背景重新画上去

            //判断棋子的不同位置画不同的线
            g.setColor(Color.BLACK);
            if (x==0 && y== 0){ //左上
                g.drawLine(50,20,60,20);
                g.drawLine(50,20,50,30);
            }else if (x==0 && y== 14){ //左下
                g.drawLine(50,580,60,580);
                g.drawLine(50,570,50,580);
            }else if (x==14 && y== 0){ //右上
                g.drawLine(600,20,610,20);
                g.drawLine(610,20,610,30);
            }else if (x==14 && y== 14){ //右下
                g.drawLine(600,580,610,580);
                g.drawLine(610,570,610,580);
            }else if (x == 0) { //棋子在左竖边线
                g.drawLine(50,y*GRID+20,60,y*GRID+20);
                g.drawLine(50,y*GRID+10,50,y*GRID+30);
            } else if (x == 14) { //棋子在右竖边线
                g.drawLine(600,y*GRID+20,610,y*GRID+20);
                g.drawLine(610,y*GRID+10,610,y*GRID+30);
            }else if (y == 0) { //棋子在上边线
                g.drawLine(x*GRID+40,20,x*GRID+60,20);
                g.drawLine(x*GRID+40,20,x*GRID+40,30);
            }else if (y == 14) { //棋子在下边线
                g.drawLine(x*GRID+40,560,x*GRID+60,560);
                g.drawLine(x*GRID+40,550,x*GRID+40,570);
            }else { //棋子在棋盘内
                //在星位上
                if ((x==7 && y==7) || (x==3 && y==3) || (x==11 && y==7) || (x==7 && y==11) || (x==11 && y==11)) {
                    g.fillOval(x*GRID+45,y*GRID+15,10,10);
                }
                g.drawLine(x*GRID+40,y*GRID+20,x*GRID+60,y*GRID+20);
                g.drawLine(x*GRID+50,y*GRID+10,x*GRID+50,y*GRID+30);
            }
        }
    }

    //加载上一局棋
    public void paintLoad(Graphics g){
        try {
            FileInputStream fip = new FileInputStream(Config.filePath);
            InputStreamReader reader = new InputStreamReader(fip);
            BufferedReader buffReader = new BufferedReader(reader); //使用缓冲流
            String str;
            while ((str = buffReader.readLine()) != null){
                ListElem l = new ListElem();
                l.str = str.split("\\s+"); //字符串切片
                strings.add(l);
            }
            buffReader.close();
            reader.close();
            fip.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for (ListElem string : strings) {
            StackElem s0 = new StackElem();
            s0.x = Integer.parseInt(string.str[0]);
            s0.y = Integer.parseInt(string.str[1]);
            stack.push(s0);
            this.updatePaint(g);
        }
    }

    //使用坐标画出棋子
    public void updatePaint(Graphics g){
        if (!over){
            if (round%2 == 1){
                round++;
                countGame.setText("第" + round + "手");
                player.setText("白先");
                g.setColor(Color.BLACK);
                assert stack.peek() != null;
                g.fillOval(stack.peek().x*GRID+40,stack.peek().y*GRID+10,Config.CHESS,Config.CHESS);
                win(1);
            }else {
                round++;
                countGame.setText("第" + round + "手");
                player.setText("黑先");
                g.setColor(Color.WHITE);
                assert stack.peek() != null;
                g.fillOval(stack.peek().x*GRID+40,stack.peek().y*GRID+10,Config.CHESS,Config.CHESS);
                win(2);
            }
        }
    }

    //找出最佳的下棋点
    public void getBestPoint(){
        //遍历棋盘找出空的位置
        for (int i=0; i<board.length; i++){
            for (int j=0; j<board[i].length; j++){
                //判断当前位置是否为空
                if (board[i][j] == 0){
                    //往左延伸
                    StringBuilder ConnectType= new StringBuilder("0");
                    int jmin = Math.max(0, j-4);
                    for(int positionj=j-1; positionj>=jmin; positionj--) {
                        //依次加上前面的棋子
                        ConnectType.append(board[i][positionj]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置中
                    Integer valueleft = Weight.map.get(ConnectType.toString());
                    if(valueleft != null){
                        Weight.weightArray[i][j] += valueleft;
                    }

                    //往右延伸
                    ConnectType = new StringBuilder("0");
                    int jmax = Math.min(14, j+4);
                    for(int positionj=j+1; positionj<=jmax; positionj++) {
                        //依次加上前面的棋子
                        ConnectType.append(board[i][positionj]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置中
                    Integer valueright = Weight.map.get(ConnectType.toString());
                    if(valueright != null) {
                        Weight.weightArray[i][j] += valueright;
                    }

                    //联合判断，判断行
                    Weight.weightArray[i][j] += unionWeight(valueleft, valueright);

                    //往上延伸
                    ConnectType = new StringBuilder("0");
                    int imin = Math.max(0, i-4);
                    for(int positioni=i-1; positioni>=imin; positioni--) {
                        //依次加上前面的棋子
                        ConnectType.append(board[positioni][j]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置中
                    Integer valueup = Weight.map.get(ConnectType.toString());
                    if(valueup != null) {
                        Weight.weightArray[i][j] += valueup;
                    }

                    //往下延伸
                    ConnectType = new StringBuilder("0");
                    int imax=Math.min(14, i+4);
                    for(int positioni=i+1; positioni<=imax; positioni++) {
                        //依次加上前面的棋子
                        ConnectType.append(board[positioni][j]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置中
                    Integer valuedown = Weight.map.get(ConnectType.toString());
                    if(valuedown != null) {
                        Weight.weightArray[i][j] += valuedown;
                    }

                    //联合判断，判断列
                    Weight.weightArray[i][j] += unionWeight(valueup, valuedown);

                    //往左上方延伸,i,j,都减去相同的数
                    ConnectType = new StringBuilder("0");
                    for(int position=-1; position>=-4; position--) {
                        if(i+position>=0 && i+position<=14 && j+position>=0 && j+position<=14)
                            ConnectType.append(board[i + position][j + position]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置
                    Integer valueLeftUp = Weight.map.get(ConnectType.toString());
                    if(valueLeftUp != null) {
                        Weight.weightArray[i][j] += valueLeftUp;
                    }

                    //往右下方延伸,i,j,都加上相同的数
                    ConnectType = new StringBuilder("0");
                    for(int position=1; position<=4; position++) {
                        if(i+position>=0 && i+position<=14 && j+position>=0 && j+position<=14)
                            ConnectType.append(board[i + position][j + position]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置
                    Integer valueRightDown = Weight.map.get(ConnectType.toString());
                    if(valueRightDown != null) {
                        Weight.weightArray[i][j] += valueRightDown;
                    }

                    //联合判断，判断行
                    Weight.weightArray[i][j] += unionWeight(valueLeftUp, valueRightDown);

                    //往左下方延伸,i加,j减
                    ConnectType = new StringBuilder("0");
                    for(int position=1; position<=4; position++) {
                        if(i+position>=0 && i+position<=14 && j-position>=0 && j-position<=14)
                            ConnectType.append(board[i + position][j - position]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置
                    Integer valueLeftDown = Weight.map.get(ConnectType.toString());
                    if(valueLeftDown != null) {
                        Weight.weightArray[i][j] += valueLeftDown;
                    }

                    //往右上方延伸,i减,j加
                    ConnectType = new StringBuilder("0");
                    for(int position=1; position<=4; position++) {
                        if(i-position>=0 && i-position<=14 && j+position>=0 && j+position<=14)
                            ConnectType.append(board[i - position][j + position]);
                    }
                    //从数组中取出相应的权值，加到权值数组的当前位置
                    Integer valueRightUp = Weight.map.get(ConnectType.toString());
                    if(valueRightUp != null) {
                        Weight.weightArray[i][j]+=valueRightUp;
                    }

                    //联合判断，判断行
                    Weight.weightArray[i][j] += unionWeight(valueLeftDown, valueRightUp);
                }
            }
        }

        //取出最大的权值
        int AIi = 0, AIj = 0;
        int weightmax=0;
        StackElem s2 = new StackElem();
        for(int i=0;i<15;i++) {
            for(int j=0;j<15;j++) {
                if(weightmax < Weight.weightArray[i][j]) {
                    weightmax = Weight.weightArray[i][j];
                    AIi = i;
                    AIj = j;
                }
            }
        }
        s2.x = AIi;
        s2.y = AIj;
        this.board[s2.x][s2.y] = 2;
        stack.push(s2);
        Weight.weightArray = new int[15][15];
    }

    //AI联合算法
    public Integer unionWeight(Integer a, Integer b) {
        //先判断a,b两个数值是不是null
        if(a==null || b==null){
            return 0;
        }
        //一一
        else if(a>=10 && a<=25 && b>=10 && b<=25) {
            return 60;
        }
        //一二、二一
        else if((a>=10 && a<=25 && b>=60 && b<=80) || (a>=60 && a<=80 && b>=10 && b<=25)) {
            return 800;
        }
        //一三、三一、二二
        else if((a>=10 && a<=25 && b>=140 && b<=1000) || (a>=140 && a<=1000 && b>=10 && b<=25) || (a>=60 && a<=80 && b>=60 && b<=80)) {
            return 3000;
        }
        //二三、三二
        else if((a>=60 && a<=80 && b>=140 && b<=1000) || (a>=140 && a<=1000 && b>=60 && b<=80)) {
            return 3000;
        }
        else {
            return 0;
        }
    }

    //判断输赢
    public void win(int Chess){
        String side;
        if (Chess == 1){
            side = "黑";
        }else {
            side = "白";
        }

        //和棋
        if (round == 255){
            over = true;
            JOptionPane.showMessageDialog(null, "棋子下完了，和棋!");
            restart.doClick();
        }

        //行判断
        assert stack.peek() != null;
        int curx = stack.peek().x, cury = stack.peek().y;
        int count = 0;
        int min = cury - 4, max = cury + 4;
        if(min < 0) min = 0;
        if(max > 14) max = 14;
        for (int i=min; i<=max; i++){
            if (board[curx][i] == Chess) {
                count++;
            }else {
                count = 0;
            }
            if (count == 5) {
                over = true;
                JOptionPane.showMessageDialog(null, side + "方胜！");
                restart.doClick();
            }
        }

        //列判断
        count = 0;
        min = curx - 4;
        max = curx + 4;
        if(min < 0) min = 0;
        if(max > 14) max = 14;
        for (int i=min; i<=max; i++){
            if (board[i][cury] == Chess) {
                count++;
            }else {
                count = 0;
            }
            if (count == 5) {
                over = true;
                JOptionPane.showMessageDialog(null, side + "方胜！");
                restart.doClick();
            }
        }

        //斜方向判断
        count = 0;
        for(int i=-4; i<=4; i++) {
            if(curx+i >= 0 && cury+i >= 0 && curx+i <= 14 && cury+i <= 14) {
                if (board[curx+i][cury+i] == Chess) {
                    count++;
                }else {
                    count = 0;
                }
            }
            if (count == 5){
                over = true;
                JOptionPane.showMessageDialog(null, side + "方胜！");
                restart.doClick();
            }
        }

        count = 0;
        for(int i=-4; i<=4; i++) {
            if(curx+i >= 0 && cury-i >= 0 && curx+i <= 14 && cury-i <= 14) {
                if (board[curx+i][cury-i] == Chess) {
                    count++;
                }else {
                    count = 0;
                }
            }
            if (count == 5){
                over = true;
                JOptionPane.showMessageDialog(null, side + "方胜！");
                restart.doClick();
            }
        }
    }
}