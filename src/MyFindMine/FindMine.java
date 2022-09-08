package MyFindMine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FindMine {
    private static int countdown = 4800;//后面使用选择器进行难度的改变
    private static int mineNum;
    private static final ImageIcon face = new ImageIcon(ClassLoader.getSystemResource("FindMine/face.jpg"));//小黄脸按钮，重开一局，设在外面是因为后面还要使用
    private static JLabel label1,label2;
    private static JButton SelectDifficulty = new JButton("点击选择");
    private static MineArea minearea;
    FindMine(int MineAreaSize, int MineNum) {
        mineNum = MineNum;
        JFrame  jf = new JFrame("伊拉克战损版扫雷");
        jf.setVisible(true);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setLocationRelativeTo(null);
        jf.setBounds(600,200,500,600);
        jf.setLayout(null);

        Container container = jf.getContentPane();
        container.setBackground(Color.LIGHT_GRAY);

        //选择难度区域代码，放JLabel和按钮触发JDialog组件
        JLabel SelectText = new JLabel("难度：");
        SelectText.setBounds(360,20,120,20);
        //SelectDifficulty = new JButton("点击选择");
        SelectDifficulty.setBounds(360,40,90,20);
        container.add(SelectText);
        container.add(SelectDifficulty);
        SelectDifficulty.addActionListener(e -> {
            SelectDialog select = new SelectDialog(jf);
            select.setVisible(true);
            jf.dispose();
        });

        //倒计时区域代码
        label1 = new JLabel("倒计时：" +(countdown / 60 / 60 % 60) +
                "时"+ (countdown / 60 % 60)+ "分" +(countdown % 60)+"秒");
        label1.setBounds(10,20,120,20);
        container.add(label1);

        //剩余旗子数区域代码
        label2 = new JLabel("剩余地雷数:"+MineNum);
        label2.setBounds(10,40,120,20);
        container.add(label2);

        //小黄脸按钮的事件
        JButton bt = new JButton(face);
        bt.setBounds(230, 20,40,40);
        bt.addActionListener(e -> {
            jf.dispose();
            countdown = 4800;
            new FindMine(MineAreaSize,MineNum);
        });
        container.add(bt);

        //画雷区喽！欸嘿嘿嘿
        minearea = new MineArea(MineAreaSize,MineNum);
        minearea.setBounds((500-20*MineAreaSize)/2,50+(500-20*MineAreaSize)/2,20*MineAreaSize,20*MineAreaSize);
        container.add(minearea);
    }

    //倒计时多线程
    static class CountDown extends Thread{
        public void run(){
            while (countdown > 0){
                try{
                    -- countdown;
                    label1.setText("总时长：" +(countdown / 60 / 60 % 60) + "时"+ (countdown / 60 % 60)+ "分" +(countdown % 60)+"秒");
                    sleep(1000);
                }catch (Exception e){
                    System.out.println("错误：" + e);
                }
            }
            if(countdown == 0) {
                //展示完整的雷区
                minearea.showBomb();
                JOptionPane.showMessageDialog(null,"OUT","时辰已到，跪下当斩！",JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //选择难度的JDialog弹窗
    static class SelectDialog extends JDialog {
        private static JButton ButtonEasy , ButtonMedium , ButtonDifficult;
        public SelectDialog(JFrame frame) {
            super(frame,"难度选择",true);
            setLayout(null);
            setLocationRelativeTo(null);
            setSize(100,200);
            Container c = getContentPane();
            ButtonEasy      = new JButton("简单");
            ButtonMedium    = new JButton("中等");
            ButtonDifficult = new JButton("困难");
            ButtonEasy.setBounds     (10,10,80,40);
            ButtonMedium.setBounds   (10,60,80,40);
            ButtonDifficult.setBounds(10,110,80,40);
            c.add(ButtonEasy);
            c.add(ButtonMedium);
            c.add(ButtonDifficult);
            ButtonEasy.addActionListener(e -> {
                countdown = 4800;
                SelectDifficulty.setText("简单");
                new FindMine(10,10);
                this.dispose();
            });

            ButtonMedium.addActionListener(e -> {
                countdown = 4800;
                SelectDifficulty.setText("中等");
                new FindMine(15,40);
                this.dispose();
            });

            ButtonDifficult.addActionListener(e -> {
                countdown = 4800;
                SelectDifficulty.setText("困难");
                new FindMine(20,60);
                this.dispose();
            });
        }
    }

    //定义一个雷区的类，然后在类的基础上进行修改
    class MineArea extends JPanel {
        private final int row;
        private final int col;
        private final int bombCount;
        private int flagNum;
        private final JLabel[][] label;
        private final boolean[][] state;
        private final Btn[][] btns;
        private final byte[][] click;

        //地雷的三种状态
        private final ImageIcon flag = new ImageIcon(ClassLoader.getSystemResource("FindMine/flag.jpg"));
        private final ImageIcon bomb = new ImageIcon(ClassLoader.getSystemResource("FindMine/bomb.jpg"));
        private final ImageIcon lucency = new ImageIcon(ClassLoader.getSystemResource("FindMine/luncecy.jpg"));
        public MineArea(int MineAreaSize,int MineNum) {
            row = MineAreaSize;
            col = MineAreaSize;
            bombCount = MineNum; /* 地雷数 */
            flagNum = bombCount;/* 标记数（用于插旗） */
            label = new JLabel[row][col];
            state = new boolean[row][col];/* 用于存储是否有地雷 */
            btns = new Btn[row][col];
            click = new byte[row][col];/* 用于存储按钮点击状态（0-未点击，1-已点击，2-未点击但周围有雷，3-插旗） */
            FindMine.setMineNum(flagNum);
            setLayout(null);
            InitTable();
            RandomMine();
            writeNumber();
            randomBtn();
        }

        //画表格开始，在初始化表格中进行此步骤
        public void InitTable() {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    JLabel l = new JLabel("", JLabel.CENTER);
                    // 设置每个小方格的边界
                    l.setBounds(j * 20, i * 20, 20, 20);
                    // 绘制方格边框
                    l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    // 设置方格为透明,便于我们填充颜色
                    l.setOpaque(true);
                    // 背景填充为黄色
                    l.setBackground(Color.lightGray);
                    // 将方格加入到面板JPanel中
                    this.add(l);
                    // 将方格存到类变量中,方便公用
                    label[i][j] = l;
                    label[i][j].setVisible(false);
                }
            }
        }

        private void RandomMine() {
            for (int i = 0; i < bombCount; ++i) {
                int rRow = (int) (Math.random() * row);
                int rCol = (int) (Math.random() * col);
                if (state[rRow][rCol]) --i;
                label[rRow][rCol].setIcon(bomb);
                state[rRow][rCol] = true;/* 有地雷的格子state为真 */
            }
        }

        /* 绘制数字 */
        private void writeNumber() {
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < col; ++j) {
                    if (state[i][j]) {
                        continue;
                    }
                    int bombCount = 0;
                    /* 寻找以自己为中心的九个格子中的地雷数 */
                    for (int r = -1; (r + i < row) && (r < 2); ++r) {
                        if (r + i < 0) continue;
                        for (int c = -1; (c + j < col) && (c < 2); ++c) {
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
        private void randomBtn() {
            for (int i = 0; i < row; ++i) {
                for (int j = 0; j < col; ++j) {
                    Btn btn = new Btn();
                    btn.i = i;
                    btn.j = j;
                    btn.setBounds(j * 20, i * 20, 20, 20);
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
                for (int r = 0;r < row;++r){
                    for(int c = 0;c < col; ++c){
                        btns[r][c].setVisible(false);/* 隐藏label */
                        label[r][c].setVisible(true);/* 显示按钮（这里只有隐藏了按钮才能显示按钮下面的label） */
                    }
                }
                JOptionPane.showMessageDialog(null,"嗨嗨害，","你又输了奥！",JOptionPane.PLAIN_MESSAGE);
            }else /* 没有踩雷 */{
                subopen(b);
            }
        }

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
            for (int r = -1; (r + b.i < row) && (r < 2); ++r) {
                if (r + b.i < 0) continue;
                for (int c = -1; (c + b.j < col) && (c < 2); ++c) {
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
                    FindMine.setMineNum(flagNum);
                }else /* 未插旗的，插旗 */{
                    b.setIcon(flag);
                    click[b.i][b.j] = 3;
                    -- flagNum;
                    FindMine.setMineNum(flagNum);
                }
                /* 把所有旗子插完了，检测是否成功 */
                if(flagNum == 0){
                    boolean flagstate = true;
                    for(int i = 0;i < row; ++i){
                        for(int j = 0;j < col; ++j){
                            if (click[i][j] != 3 && state[i][j]) flagstate = false;
                        }
                    }
                    if(flagstate) JOptionPane.showMessageDialog(null,"您成功了","游戏结束",JOptionPane.PLAIN_MESSAGE);
                }
            }else /* 旗子用完了，不能插 */{
                JOptionPane.showMessageDialog(null,"标记已用尽","错误操作",JOptionPane.PLAIN_MESSAGE);
            }
        }

        public void showBomb(){
            for (int r = 0;r < row;++r){
                for(int c = 0;c < col; ++c){
                    btns[r][c].setVisible(false);/* 隐藏label */
                    label[r][c].setVisible(true);/* 显示按钮（这里只有隐藏了按钮才能显示按钮下面的label） */
                }
            }
        }
    }

    public static void setMineNum(int i){
        mineNum = i;
        label2.setText("剩余旗子数:"+mineNum);
    }

    //为了方便进行统计，给btn中添加两个内部变量，有利于其进行二位数组方面的统计
    class Btn extends JButton{
        public int i,j;
    }

    public static void main(String[] args) {
        new FindMine(10,10);
        CountDown cd = new CountDown();
        cd.start();
    }
}
