import java.io.File;

public interface Config {
    File filePath = new File("src/load.txt");
    public static final int WIDTH = 30; //棋盘离左右两边的距离(包含拓展范围)
    public static final int EXPAND = 20; //棋盘的外部范围(拓展范围)
    public static final int GRID = 40; //棋盘格子的宽度
    public static final int CHESS = 20; //棋子的大小
    public static final int RADIUS = 10; //棋子的半径大小
}