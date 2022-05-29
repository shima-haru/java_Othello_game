import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.ArrayList;

public class MyClient extends JFrame implements MouseListener,MouseMotionListener {
	private JButton buttonArray[][], pass;//ボタン用の配列
	private Container c;
	private ImageIcon blackIcon, whiteIcon, boardIcon;
    private int myColor; //黒0、白1
    private ImageIcon myIcon, yourIcon, pass1, reset1;
    private int myTurn; //0の時のみコマが置ける
    private int CountWhite, CountBlack, CountBoard, passcount;
    private JLabel theLabel1;
    private boolean auto, jud; //auto:オートパスするかどうかのフラグ jud:勝敗がついたかどうかのフラグ
    
	PrintWriter out;//出力用のライター

	public MyClient() {
		//名前の入力ダイアログを開く
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//名前がないときは，"No name"とする
		}
        
        //IPアドレス
        String IPname = JOptionPane.showInputDialog(null,"IPアドレスを入力してください","IPアドレスの入力",JOptionPane.QUESTION_MESSAGE);//IPアドレスの入力がないときは自身のコンピュータ
		

		//ウィンドウを作成する
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
		setTitle("MyClient");//ウィンドウのタイトルを設定する
		setSize(500,500);//ウィンドウのサイズを設定する
		c = getContentPane();//フレームのペインを取得する

		//アイコンの設定
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");
        pass1 = new ImageIcon("pass");
        reset1 = new ImageIcon("reset");
        
        jud = false; //勝利判定をfalseにしておく

        c.setLayout(null);//自動レイアウトの設定を行わない
		//ボタンの生成
		buttonArray = new JButton[8][8];//ボタンの配列を8*8個作成する
            int k = 0;
		for(int i=0;i<8;i++){
            for(int j=0; j<8; j++){
                if(((i==3)&&(j==3)) || ((i==4)&&(j==4))){
                    buttonArray[j][i] = new JButton(whiteIcon);//白の初期位置
                }else if(((i==4)&&(j==3)) || ((i==3)&&(j==4))){
                    buttonArray[j][i] = new JButton(blackIcon);//黒の初期位置
                }else{
                    buttonArray[j][i] = new JButton(boardIcon);//ボタンにボードアイコンを設定する
                }
                c.add(buttonArray[j][i]);//ペインに貼り付ける
                buttonArray[j][i].setBounds(10+j*45,10+i*45,45,45);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
                buttonArray[j][i].addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
                buttonArray[j][i].addMouseMotionListener(this);//ボタンをマウスで動かそうとしたときに反応するようにする
                buttonArray[j][i].setActionCommand(Integer.toString(k));//ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
                k++;
            }
		}
        //passボタンの作成
        pass = new JButton("pass", pass1);
        c.add(pass);//ペインに貼り付ける
        pass.setBounds(10,380,90,45);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
        pass.addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
        pass.addMouseMotionListener(this);//ボタンをマウスで動かそうとしたときに反応するようにする
        
        //resetボタンの作成
        pass = new JButton("reset", reset1);
        c.add(pass);//ペインに貼り付ける
        pass.setBounds(100,380,90,45);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
        pass.addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
        pass.addMouseMotionListener(this);//ボタンをマウスで動かそうとしたときに反応するようにする
        
		
		//サーバに接続する
		Socket socket = null;
		try {
			//"localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			//10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
            if(IPname.equals("")){
			socket = new Socket("localhost", 10000);
            }else{
                socket = new Socket(IPname, 10000);
            }
			
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			 System.err.println("エラーが発生しました: " + e);
		}
		
		MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
		mrt.start();//スレッドを動かす（Runが動く）
	}
		
	//メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {
		
		Socket socket;
		String myName;
		
		public MesgRecvThread(Socket s, String n){
			socket = s;
			myName = n;
		}
		
		//通信状況を監視し，受信データによって動作する
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);//接続の最初に名前を送る
                
                String myNumberStr = br.readLine();
                System.out.println("プリント" + myNumberStr);
                int myNumberInt = Integer.parseInt(myNumberStr);//何番目の接続か
                if(myNumberInt % 2 == 0){//偶数番目に繋いだ端末がが先行
                    myColor = 0;
                    myIcon = blackIcon;
                    yourIcon = whiteIcon;
                    myTurn = 0;
                    //色付きラベルの作成
                    theLabel1 = new JLabel("自分の番です");
                    c.add(theLabel1);
                    theLabel1.setBounds(200,380,250,45);
                    theLabel1.setFont(new Font( "ＭＳ ゴシック" , Font.BOLD, 20));
                    theLabel1.setForeground(Color.BLACK); 
                    
                    JLabel theLabel2 = new JLabel("黒");
                    c.add(theLabel2);
                    theLabel2.setBounds(380,10,150,45);
                    theLabel2.setFont(new Font( "ＭＳ ゴシック" , Font.BOLD, 30));
                    theLabel2.setForeground(Color.BLACK); 
                }else{//奇数番目に繋いだ端末がが後攻
                    myColor = 1;
                    myIcon = whiteIcon;
                    yourIcon = blackIcon;
                    myTurn = 1;
                    //色付きラベルの作成
                    theLabel1 = new JLabel("相手の番です");
                    c.add(theLabel1);
                    theLabel1.setBounds(200,380,250,45);
                    theLabel1.setFont(new Font( "ＭＳ ゴシック" , Font.BOLD, 20));
                    theLabel1.setForeground(Color.BLACK); 
                    
                    JLabel theLabel2 = new JLabel("白");
                    c.add(theLabel2);
                    theLabel2.setBounds(380,10,150,45);
                    theLabel2.setFont(new Font( "ＭＳ ゴシック" , Font.BOLD, 30));
                    theLabel2.setForeground(Color.WHITE); 
                    
                }
                
                
				while(true) {
					String inputLine = br.readLine();//データを一行分だけ読み込んでみる
					if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
						System.out.println(inputLine);//デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" ");	//入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す
                        if(cmd.equals("PLACE")){//cmdの文字と"PLACE"が同じか調べる．同じ時にtrueとなる
							//PLACEの時の処理(コマの色を変える処理)
							String theBName = inputTokens[1];//ボタンの名前（番号）の取得
							int theBnum = Integer.parseInt(theBName);//ボタンの名前を数値に変換する
                            int y = theBnum / 8; //y座標
                            int x = theBnum % 8; //x座標
							int theColor = Integer.parseInt(inputTokens[2]);//色を数値に変換する
                            if(theColor == myColor){
                                //送信元クライアントでの処理
                                buttonArray[x][y].setIcon(myIcon);
                            } else {
                                //送信先クライアントでの処理
                                buttonArray[x][y].setIcon(yourIcon);
                            }
                            myTurn = 1 - myTurn;
                            passcount = 0;
                            turntext();
                            IconCount();//この時点で勝敗がつくか判定のため各コマの数を数える
                            if(myTurn == 0){
                                halfjudge();//勝敗がつくか確認する関数(2回勝利を出さないように片方だけが処理を行う)
                                if(!jud ){
                                    autopass();//次のターンのコマがオートパスするかどうかの判定
                                }
                            }
						}
                        //２回パスすると勝敗がつく
                        if(cmd.equals("PASS")){//cmdの文字と"PASS"が同じか調べる．同じ時にtrueとなる
                            if(passcount < 1){
                                myTurn = 1 - myTurn;
                                passcount++;
                                turntext(); //ターンが入れ替わったので表示を切り替える
                                if(myTurn == 0){
                                    autopass();//次のターンのコマがオートパスするかどうかの判定
                                }
                            }else{
                                myTurn = 1;
                                judgement();
                            }
						}
                        //64マス埋まる、全滅の場合に勝敗がつく
                        if(cmd.equals("JUDGE")){//cmdの文字と"JUDGE"が同じか調べる．同じ時にtrueとなる
                            myTurn = 1;
                            judgement(); //勝敗判定を出力する関数に飛ぶ
                        }
                        //盤面をリセットする
                        if(cmd.equals("RESET")){ //resetボタンを押すと盤面が初期化される
                            jud = false; //勝敗判定をfalseにする
                            for(int i=0;i<8;i++){
                                for(int j=0; j<8; j++){
                                    if(((i==3)&&(j==3)) || ((i==4)&&(j==4))){
                                        buttonArray[i][j].setIcon(whiteIcon);//白の初期位置
                                    }else if(((i==4)&&(j==3)) || ((i==3)&&(j==4))){
                                        buttonArray[i][j].setIcon(blackIcon);//黒の初期位置
                                    }else{
                                        buttonArray[i][j].setIcon(boardIcon);//ボタンにボードアイコンを設定する
                                    }
                                }
                            }
                            if(myNumberInt % 2 == 0){//偶数番目に繋いだ端末が先行
                                myTurn = 0;
                                turntext();
                                theLabel1.setForeground(Color.BLACK); 
                            }else{//奇数番目に繋いだ端末がが後攻
                                myTurn = 1;
                                turntext();
                                theLabel1.setForeground(Color.BLACK); 
                            }
                        }
                        //挟まれたコマが裏返る
                        if(cmd.equals("FLIP")){//cmdの文字と"FLIP"が同じか調べる．同じ時にtrueとなる
							//TURNの時の処理(コマを裏返す処理)
                            int theColor = Integer.parseInt(inputTokens[3]);//色を数値に変換する
							int x = Integer.parseInt(inputTokens[1]);
                            int y = Integer.parseInt(inputTokens[2]);
                            if(theColor == myColor){
                            //送信元クライアントでの処理
                                buttonArray[x][y].setIcon(myIcon);
                            } else {
                                //送信先クライアントでの処理
                                buttonArray[x][y].setIcon(yourIcon);
                            }
                        }
                        //勝敗を画面に出力する
                        if(cmd.equals("JUDGETEXT")){
							//TURNの時の処理(コマを裏返す処理)
                            int winColor = Integer.parseInt(inputTokens[1]);//色を数値に変換する
                            if(winColor == 2){
                                theLabel1.setText("引き分け！");
                            }else if(winColor == myColor){
                                //勝利表示
                                theLabel1.setText("勝ち！");
                                theLabel1.setForeground(Color.RED); 
                            } else {
                                //敗北表示
                                theLabel1.setText("負け！");
                                theLabel1.setForeground(Color.BLUE); 
                            }
                        }
					}else{
						break;
					}
				
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
	}
  	
	public void mouseClicked(MouseEvent e) {//ボタンをクリックしたときの処理
        if(myTurn == 0){//myTurnが0の時のみコマが置ける
            System.out.println("クリック");
            JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．型が違うのでキャストする
            String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す

            Icon theIcon = theButton.getIcon();//theIconには，現在のボタンに設定されたアイコンが入る
            System.out.println("アイコン" + theIcon);//デバッグ（確認用）に，クリックしたアイコンの名前を出力する
            
            //パスをする
            if(theIcon == pass1){
                //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
                String msg = "PASS";
                //サーバに情報を送る
                out.println(msg);//送信データをバッファに書き出す
                out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
            }
            
            //コマを新たに置く
            if(theIcon == boardIcon){//theIconがboardIconの時にだけコマを置けるようにする
                int temp = Integer.parseInt(theArrayIndex);
                int y = temp / 8;
                int x = temp % 8;
                if(judgeButton(y, x)){
                    if(myColor == 0){
                        theButton.setIcon(blackIcon);
                    }else{
                        theButton.setIcon(whiteIcon);
                    }
                    //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
                    String msg = "PLACE"+" "+theArrayIndex+" "+myColor;

                    //サーバに情報を送る
                    out.println(msg);//送信データをバッファに書き出す
                    out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
                }else{
                    System.out.println("そこには置けません");
                }
            }
            
            repaint();//画面のオブジェクトを描画し直す
        }
        
        //盤面のリセット
        System.out.println("クリック");
        JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．型が違うのでキャストする
        String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す

        Icon theIcon = theButton.getIcon();//theIconには，現在のボタンに設定されたアイコンが入る
        System.out.println("アイコン" + theIcon);//デバッグ（確認用）に，クリックしたアイコンの名前を出力する
        if(theIcon == reset1){
            //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
            String msg = "RESET";
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
            repaint();//画面のオブジェクトを描画し直す
        }
        
	}
	
    //その他のマウスの処理は使わない
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
    
    //クリックしたアイコンの四方８マスにyourIconがあるか判定する
    public boolean judgeButton(int y, int x){
        boolean flag = false; //そこにコマが置ける場合flagがtrueに変わる
        ArrayList<Integer> list = new ArrayList<Integer>(); //返るコマを記録する配列を作っておく
        
        for(int i=-1; i<=1; i++){
            for(int j=-1; j<=1; j++){
                if((x+j>=0) && (x+j<=7) && (y+i>=0) && (y+i<=7 )){ //8*8の盤に納まる範囲で確認する
                    //そのコマが裏返るのか確認する
                    if(flipButtons(x, y, j, i, list) >= 1){
                        flag = true; //裏返るコマがあった場合そこへコマが置けるようになる
                        TurnButton(list); //コマを実際に裏返す処理
                        list.clear();
                    }
                }
            }
        }
        if(!flag){
            System.out.println("flagはfalseのままです");
        }
        return flag;
    }
    
    //コマがいくつ裏返るのか確認する
    public int flipButtons(int x, int y, int j, int i, ArrayList<Integer> list){
        int flipNum = 0; //裏返るコマの数を返す
        Icon theIcoon = buttonArray[x+j][y+i].getIcon();
        int a = j;
        int b = i;
        
        if(theIcoon == yourIcon){//yourIconがあった場合どこまで続くか確認する
            while(true){//yourIconがある方向へ順番にコマを見ていく
                if((x+a<0) || (x+a>7) || (y+b<0) || (y+b>7 )){ //盤外まで行った場合はループを抜け出す
                    flipNum = 0; //今までに数えていたコマの数を0にする
                    break;
                }
                Icon theIcooon = buttonArray[x+a][y+b].getIcon();
                if(theIcooon == yourIcon){
                    list.add(x+a);
                    list.add(y+b);
                    flipNum++;
                }else if(theIcooon == myIcon){
                    break; //それまでにflipNumで数えた枚数が裏返る
                }else{
                    flipNum = 0;
                    break;
                }
                //次のマスを見る
                a += j;
                b += i;
            }
        }
        if(flipNum == 0){ //裏返るコマがない場合は候補として覚えておいたコマの位置をリストから消す
            list.clear();
        }
        return flipNum;
    }
    
    //リストで覚えていたコマを実際にひっくり返す処理
    public void TurnButton(ArrayList<Integer> list){
        for(int i = 0; i < list.size(); i++){ //1枚ずつ情報を送る
            int x = list.get(i);
            int y = list.get(i+1);
            String msg = "FLIP"+" "+x+" "+y+" "+myColor;
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する

            repaint();//画面のオブジェクトを描画し直す
            i++;
        }
    }
    
    //勝敗判定を出力する部分
    public void judgement(){
        IconCount();
        int judgetext = 100; //勝利判定を数字で送る
        System.out.println("白の数：" + CountWhite);
        System.out.println("黒の数：" + CountBlack);
        if(CountWhite > CountBlack){
            System.out.println("白の勝ち");
            judgetext = 1;
        }else if(CountWhite < CountBlack){
            System.out.println("黒の勝ち");
            judgetext = 0;
        }else{
            System.out.println("引き分け");
            judgetext = 2;
        }
        String msg = "JUDGETEXT"+" "+judgetext;
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する

            repaint();//画面のオブジェクトを描画し直す
    }
    
    //盤にある各アイコンの枚数をカウントする処理
    public void IconCount(){
        CountWhite = 0;
        CountBlack = 0;
        CountBoard = 0;
        for(int i=0;i<8;i++){
            for(int j=0; j<8; j++){
                JButton theButton = buttonArray[j][i];
                Icon theIcon = theButton.getIcon();//theIconには，現在のボタンに設定されたアイコンが入る
                if(theIcon == whiteIcon){
                    CountWhite++;
                }else if(theIcon == blackIcon){
                    CountBlack++;
                }else{
                    CountBoard++;
                }
            }
        }
    }
    
    //現在どちらのターンかテキスト表示する関数
    public void turntext(){
        if(myTurn == 0){
            theLabel1.setText("自分の番です");
        }else if(myTurn == 1){
            if(auto){
                theLabel1.setText("自動パスしました"); //自動パスした場合は知らせる
            }else{
                theLabel1.setText("相手の番です");
            }
        }
    }
    
    //途中で勝利判定がつくかの確認を行う関数
    public void halfjudge(){
        //64マスコマが埋まっていた場合
        if(CountBoard == 0){
            jud = true;
            System.out.println("64マス埋まりました");
            //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
            String msg = "JUDGE";
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
        }
        
        //白が全滅（盤がすべて黒）の場合）
        if((CountWhite == 0) && (CountBlack > 0)){
            jud = true;
            System.out.println("白のコマがなくなりました");
            //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
            String msg = "JUDGE";
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
        }
        
        //黒が全滅（盤がすべて白）の場合）
        if((CountBlack == 0) && (CountWhite > 0)){
            jud = true;
            System.out.println("黒のコマがなくなりました");
            //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
            String msg = "JUDGE";
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
        }
        repaint();//画面のオブジェクトを描画し直す
    }
    
    //コマが置く場所がないかどうか（自動パス）の判定を行う。
    public void autopass(){
        auto = false; //autoフラッグを下げておく
        int notpass = 0; //置ける箇所をカウントする
        for(int i=0;i<8;i++){
            for(int j=0; j<8; j++){
                JButton theButton = buttonArray[j][i];
                Icon theIcon = theButton.getIcon();
                if(theIcon == boardIcon){
                    if(judgeButton1(i, j)){
                        notpass++;
                    }
                }
            }
        }
        if(notpass == 0){ //置ける場所が一か所もなかった場合の処理
            auto = true;
            System.out.println("自分のターンにコマを置く場所がありません");
            //送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
            String msg = "PASS";
            //サーバに情報を送る
            out.println(msg);//送信データをバッファに書き出す
            out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する
            repaint();//画面のオブジェクトを描画し直す
        }else{
            System.out.println("自分のターンにコマを置く場所があります");
        }
    }
    
    //選択したアイコンのの四方８マスにyourIconがあるか判定する
    public boolean judgeButton1(int y, int x){
        boolean flag = false; //そこにコマが置ける場合flagがtrueに変わる
        ArrayList<Integer> list = new ArrayList<Integer>(); //返るコマを記録する配列を作っておく
        int flip = 0; //何枚コマが裏返るか数える
        
        for(int i=-1; i<=1; i++){
            for(int j=-1; j<=1; j++){
                if((x+j>=0) && (x+j<=7) && (y+i>=0) && (y+i<=7 )){ //8*8の盤に納まる範囲で確認する
                    //そのコマが裏返るのか確認する
                    if(flipButtons(x, y, j, i, list) >= 1){
                        flag = true; //裏返るコマがあった場合そこへコマが置けるようになる
                        flip += list.size() / 2;
                        list.clear();
                    }
                }
            }
        }
        if(flip > 0){
            System.out.println("x: " + x + " ,y: " + y + "にコマを置くと" + flip + "枚裏返ります");
        }
        return flag;
    }
}
