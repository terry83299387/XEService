package qmk;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MeteorFly extends JFrame {

	final int MAX = 5; // (1~1000)流星的个数
	final int SLEEP = 30; // 流星飞行的速度（数值越大，速度越慢）
	final int COLORLV = 2; // (2~20)色阶（可改变光晕大小）
	final String COLOR = null; // ("#000000"~"#ffffff")光晕颜色（如果不填或null，则为默认颜色）
	final int SIZE = 3; // (2~50)流星大小

	private MyPanel panel;

	public MeteorFly() {
		panel = new MyPanel();
		this.getContentPane().add(panel);

		this.setSize(800, 400); // 创建窗体
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public static void main(String[] args) {
		new MeteorFly();
	}

	class MyPanel extends JPanel implements Runnable {

		Meteor p[];

		int AppletWidth, AppletHeight;

		BufferedImage OffScreen;
		Graphics drawOffScreen;
		Thread pThread;

		public MyPanel() {
			setBackground(Color.black); // 窗体初始化
			AppletWidth = 800;
			AppletHeight = 400;
			p = new Meteor[MAX];
			for (int i = 0; i < MAX; i++)
				p[i] = new Meteor();
			OffScreen = new BufferedImage(AppletWidth, AppletHeight, BufferedImage.TYPE_INT_BGR);
			drawOffScreen = OffScreen.getGraphics();
			pThread = new Thread(this);
			pThread.start();
		}

		@Override
		public void paintComponent(Graphics g) {
			// TODO Auto-generated method stub
			super.paintComponents(g);
			g.drawImage(OffScreen, 0, 0, this);
		}

		@Override
		final public void run() {
			while (true) {
				// drawOffScreen.clearRect(0, 0, AppletWidth, AppletHeight); //
				// 清屏

				for (int i = 0; i < MAX; i++) {
					drawOffScreen.setColor(p[i].color); // RGB颜色
					drawOffScreen.fillOval(p[i].x, p[i].y, SIZE, SIZE);
					p[i].x += p[i].mx;
					p[i].y += p[i].my;
					// if (p[i].x > AppletWidth || p[i].y > AppletHeight) {
					// p[i].reset();
					// }

					int x = p[i].x;
					int y = p[i].y;
					int R = p[i].color.getRed(); // 提取颜色
					int G = p[i].color.getGreen();
					int B = p[i].color.getBlue();
					while (true) {
						if (R == 0 && G == 0 && B == 0) {
							break;
						}
						R -= COLORLV; // 尾部颜色淡化
						if (R < 0) {
							R = 0;
						}
						G -= COLORLV;
						if (G < 0) {
							G = 0;
						}
						B -= COLORLV;
						if (B < 0) {
							B = 0;
						}
						Color color = new Color(R, G, B);
						x -= p[i].mx; // 覆盖尾部
						y -= p[i].my;
						drawOffScreen.setColor(color);
						drawOffScreen.fillOval(x, y, SIZE, SIZE);
					}
					if (x > AppletWidth || y > AppletHeight) { // 流星飞出窗口，重置流星
						p[i].reset();
					}
				}
				repaint();

				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	class Meteor { // 流星类
		int x, y; // 流星的位置
		int mx, my; // 下落速度
		Color color; // 流星颜色

		public Meteor() {
			reset();
		}

		public void reset() {
			int rand = (int) (Math.random() * 100); // 随机生成流星出现位置
			if (rand > 35) {
				x = (int) (Math.random() * 600);
				y = 0;
			} else {
				y = (int) (Math.random() * 150);
				x = 0;
			}
			mx = (int) (Math.random() * 2 + 1); // 随机生成下落速度和角度
			my = (int) (Math.random() * 2 + 1);
			if (COLOR == null || COLOR.length() == 0) {
				color = new Color(
				// 随机颜色
						(new Double(Math.random() * 128)).intValue() + 128, (new Double(Math.random() * 128))
								.intValue() + 128, (new Double(Math.random() * 128)).intValue() + 128);
			} else {
				color = Color.decode(COLOR);
			}
		}
	}

}
