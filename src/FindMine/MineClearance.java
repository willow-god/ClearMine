package FindMine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MineClearance {
    static private int midtime = 3600,mineNum = 0;/* 倒计时时间以及可用旗子数 */
    private static final ImageIcon face = new ImageIcon(ClassLoader.getSystemResource("FindMine/face.jpg"));/* 小黄脸图标 */
    static private JLabel label1,label2;/* 提示文字 */
    static private GamePanel gp;/* 雷区 */

    MineClearance(){
        /* 绘制窗口 */
        JFrame f = new JFrame("丐版扫雷");
        f.setBounds(600,200,500,600);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setLayout(null);
        label1 = new JLabel("总时长(不要等到时间结束哦)：" +(midtime / 60 / 60 % 60) + ":"+ (midtime / 60 % 60)+ ":" +(midtime % 60));
        label1.setBounds(10,20,120,20);
        f.add(label1);
        /* 显示旗子数 */
        label2 = new JLabel("剩余旗子数:"+mineNum);
        label2.setBounds(400,20,120,20);
        f.add(label2);
        /* 重置按钮 */
        JButton bt = new JButton(face);//图片大小怎么设置缩放？不会欸
        bt.setBounds(230, 15,30,30);
        bt.addActionListener(e -> {
            f.dispose();
            midtime = 3600;
            new MineClearance();
        });
        f.add(bt);
        /* 绘制雷区 */
        gp = new GamePanel(20,20);
        gp.setBounds(40,100,400,400);
        f.add(gp);
        /* 显示界面 */
        f.setVisible(true);
    }
    /* 倒计时线程 */
    static class CountDown extends Thread{
        public void run(){
            while (midtime > 0){
                try{
                    -- midtime;
                    label1.setText("总时长：" +(midtime / 60 / 60 % 60) + ":"+ (midtime / 60 % 60)+ ":" +(midtime % 60));
                    Thread.sleep(1000);
                }catch (Exception e){
                    System.out.println("错误：" + e);
                }
            }
            if(midtime == 0) {
                gp.showBomb();
                JOptionPane.showMessageDialog(null,"OUT","时辰已到，跪下当斩！",JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    public static void main(String[] args){
        new MineClearance();
        /* 倒计时 */
        CountDown cd = new CountDown();
        cd.start();
    }
    /* 修改旗子数 */
    public static void setMineNum(int i){
        mineNum = i;
        label2.setText("剩余旗子数:"+mineNum);
    }
}

class GamePanel extends JPanel {
    private final int rows;
    private final int cols;
    private final int bombCount;
    private int flagNum;
    private final int BLOCKWIDTH = 20;
    private final int BLOCKHEIGHT = 20;
    private final JLabel[][] label;
    private final boolean[][] state;
    private final Btn[][] btns;
    private final byte[][] click;
    private static final ImageIcon flag = new ImageIcon(ClassLoader.getSystemResource("FindMine/flag.jpg"));
    private static final ImageIcon bomb = new ImageIcon(ClassLoader.getSystemResource("FindMine/bomb.jpg"));
    private static final ImageIcon lucency = new ImageIcon(ClassLoader.getSystemResource("FindMine/luncecy.jpg"));

    /* 构造雷区 */
    public GamePanel(int row, int col) {
        rows = row;/* 行数 */
        cols = col;/* 列数 */
        bombCount = rows * cols / 10; /* 地雷数 */
        flagNum = bombCount;/* 标记数（用于插旗） */
        label = new JLabel[rows][cols];
        state = new boolean[rows][cols];/* 用于存储是否有地雷 */
        btns = new Btn[rows][cols];
        click = new byte[rows][cols];/* 用于存储按钮点击状态（0-未点击，1-已点击，2-未点击但周围有雷，3-插旗） */
        MineClearance.setMineNum(flagNum);
        setLayout(null);
        initLable();
        randomBomb();
        writeNumber();
        randomBtn();
    }

    public void initLable() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JLabel l = new JLabel("", JLabel.CENTER);
                // 设置每个小方格的边界
                l.setBounds(j * BLOCKWIDTH, i * BLOCKHEIGHT, BLOCKWIDTH, BLOCKHEIGHT);
                // 绘制方格边框
                l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                // 设置方格为透明,便于我们填充颜色
                l.setOpaque(true);
                // 背景填充为黄色
                l.setBackground(Color.lightGray);
                // 将方格加入到容器中(即面板JPanel)
                this.add(l);
                // 将方格存到类变量中,方便公用
                label[i][j] = l;
                label[i][j].setVisible(false);
            }
        }
    }

    /* 绘制地雷 */
    private void randomBomb() {
        for (int i = 0; i < bombCount; ++i) {
            int rRow = (int) (Math.random() * rows);
            int rCol = (int) (Math.random() * cols);
            if (state[rRow][rCol]) --i;
            label[rRow][rCol].setIcon(bomb);
            state[rRow][rCol] = true;/* 有地雷的格子state为真 */
        }
    }

    /* 绘制数字 */
    private void writeNumber() {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                if (state[i][j]) {
                    continue;
                }
                int bombCount = 0;
                /* 寻找以自己为中心的九个格子中的地雷数 */
                for (int r = -1; (r + i < rows) && (r < 2); ++r) {
                    if (r + i < 0) continue;
                    for (int c = -1; (c + j < cols) && (c < 2); ++c) {
                        if (c + j < 0) continue;
                        if (state[r + i][c + j]) ++bombCount;
                    }
                }
                if (bombCount > 0) {
                    click[i][j] = 2;
                    label[i][j].setText(String.valueOf(bombCount));
                }
            }
        }
    }
    /* 绘制按钮 */
    /* 绘制按钮 */
    private void randomBtn() {
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                Btn btn = new Btn();
                btn.i = i;
                btn.j = j;
                btn.setBounds(j * BLOCKWIDTH, i * BLOCKHEIGHT, BLOCKWIDTH, BLOCKHEIGHT);
                this.add(btn);
                btns[i][j] = btn;
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        /* 左键点击，屏蔽插旗格子的点击事件 */
                        if(e.getButton() == MouseEvent.BUTTON1) {
                            if(click[btn.i][btn.j] != 3) open(btn);
                        }
                        /* 右键点击 */
                        if(e.getButton() == MouseEvent.BUTTON3) placeFlag(btn);
                    }
                });

            }
        }
    }

    /* 打开这个雷区 */
    private void open(Btn b){
        /* 踩雷 */
        if(state[b.i][b.j]){
            for (int r = 0;r < rows;++r){
                for(int c = 0;c < cols; ++c){
                    btns[r][c].setVisible(false);/* 隐藏label */
                    label[r][c].setVisible(true);/* 显示按钮（这里只有隐藏了按钮才能显示按钮下面的label） */
                }
            }
            JOptionPane.showMessageDialog(null,"嗨嗨害，","你又输了奥！",JOptionPane.PLAIN_MESSAGE);
        }else /* 没有踩雷 */{
            subopen(b);
        }
    }
    /* 递归打开周边雷区 */
    private void subopen(Btn b){
        /* 有雷，不能打开 */
        if(state[b.i][b.j]) return;
        /* 打开过的和插旗的，不用打开 */
        if(click[b.i][b.j] == 1 || click[b.i][b.j] == 3) return;
        /* 周围有雷的，只打开它 */
        if(click[b.i][b.j] == 2) {
            b.setVisible(false);
            label[b.i][b.j].setVisible(true);
            click[b.i][b.j] = 1;
            return;
        }
        /* 打开当前这个按钮 */
        b.setVisible(false);
        label[b.i][b.j].setVisible(true);
        click[b.i][b.j] = 1;
        /* 递归检测周边八个按钮 */
        for (int r = -1; (r + b.i < rows) && (r < 2); ++r) {
            if (r + b.i < 0) continue;
            for (int c = -1; (c + b.j < cols) && (c < 2); ++c) {
                if (c + b.j < 0) continue;
                if (r==0 && c==0) continue;
                Btn newbtn = btns[r + b.i][c + b.j];
                subopen(newbtn);
            }
        }
    }
    /* 插旗 */
    private void placeFlag(Btn b){
        /* 只能插和地雷数相同数目的旗子 */
        if(flagNum>0){
            /* 插过旗的，再点一次取消 */
            if(click[b.i][b.j] == 3){
                if(label[b.i][b.j].getText().equals("[0-9]"))
                    click[b.i][b.j] = 2;
                else
                    click[b.i][b.j] = 0;
                b.setIcon(lucency);
                ++ flagNum;
                MineClearance.setMineNum(flagNum);
            }else /* 未插旗的，插旗 */{
                b.setIcon(flag);
                click[b.i][b.j] = 3;
                -- flagNum;
                MineClearance.setMineNum(flagNum);
            }
            /* 把所有旗子插完了，检测是否成功 */
            if(flagNum == 0){
                boolean flagstate = true;
                for(int i = 0;i < rows; ++i){
                    for(int j = 0;j < cols; ++j){
                        if (click[i][j] != 3 && state[i][j]) flagstate = false;
                    }
                }
                if(flagstate) JOptionPane.showMessageDialog(null,"您成功了","游戏结束",JOptionPane.PLAIN_MESSAGE);
            }
        }else /* 旗子用完了，不能插 */{
            JOptionPane.showMessageDialog(null,"标记已用尽","错误操作",JOptionPane.PLAIN_MESSAGE);
        }
    }
    /* 显示雷区 */
    public void showBomb(){
        for (int r = 0;r < rows;++r){
            for(int c = 0;c < cols; ++c){
                btns[r][c].setVisible(false);/* 隐藏label */
                label[r][c].setVisible(true);/* 显示按钮（这里只有隐藏了按钮才能显示按钮下面的label） */
            }
        }
    }
}

class Btn extends JButton{
    public int i,j;
}
