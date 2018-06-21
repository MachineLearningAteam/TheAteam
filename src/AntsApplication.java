//コンポーネント(ウィンドウの中のボタンなどの構成要素)の配置を決めるためのクラス
import java.awt.BorderLayout;
//ウィンドウのアイコンを画像から生成するクラス
import javax.swing.ImageIcon;
//ウィンドウのクラス
import javax.swing.JFrame;
//イベントディスパッチスレッド以外のスレッドで描画処理を行うためのスレッドのクラス
import javax.swing.SwingUtilities;

/**
 * Entry point for displaying Ants as an Application.
 * Use this for both Java web start and executable jar.
 */
public class AntsApplication {
	//アプリケーションとして動くときのエントリポイント
	public static void main(String [] args){
		//フレーム構築用の新しいスレッドを生成してそのスレッドでRunnableクラスのrunメソッドを実行する.
		SwingUtilities.invokeLater(new Runnable(){
			//新しく生成されたスレッドで実行される関数
			@Override
			public void run() {
				//Antsという名前の新しいフレームを生成する.
				JFrame frame = new JFrame ("Ants");
				//frameを閉じたらアプリケーションも終了するという設定
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				//Ants.javaで定義されているコンテナAnts(シミュレーション領域)のインスタンスをframeに追加する.
				final Ants ants = new Ants();
				frame.add(ants);
				//詳細設定のやつ
				AdvancedControlPanel advancedPanel = new AdvancedControlPanel(ants);
				//設定のやつ
				final AntsControlPanel antsPanel = new AntsControlPanel(ants, advancedPanel);
				//設定のやつをフレームの右側に追加する.
				frame.add(antsPanel.getPanel(), BorderLayout.EAST);
				//詳細設定のやつをフレームの下側に追加する.
				frame.add(advancedPanel.getPanel(), BorderLayout.SOUTH);
				//フレームのサイズの指定
				frame.setSize(600, 600);
				//フレームのアイコンをicon.pngから生成する.
				frame.setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
				//フレームの可視化
				frame.setVisible(true);
			}
		});
	}
}
