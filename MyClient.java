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
	private JButton buttonArray[][], pass;//�{�^���p�̔z��
	private Container c;
	private ImageIcon blackIcon, whiteIcon, boardIcon;
    private int myColor; //��0�A��1
    private ImageIcon myIcon, yourIcon, pass1, reset1;
    private int myTurn; //0�̎��̂݃R�}���u����
    private int CountWhite, CountBlack, CountBoard, passcount;
    private JLabel theLabel1;
    private boolean auto, jud; //auto:�I�[�g�p�X���邩�ǂ����̃t���O jud:���s���������ǂ����̃t���O
    
	PrintWriter out;//�o�͗p�̃��C�^�[

	public MyClient() {
		//���O�̓��̓_�C�A���O���J��
		String myName = JOptionPane.showInputDialog(null,"���O����͂��Ă�������","���O�̓���",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//���O���Ȃ��Ƃ��́C"No name"�Ƃ���
		}
        
        //IP�A�h���X
        String IPname = JOptionPane.showInputDialog(null,"IP�A�h���X����͂��Ă�������","IP�A�h���X�̓���",JOptionPane.QUESTION_MESSAGE);//IP�A�h���X�̓��͂��Ȃ��Ƃ��͎��g�̃R���s���[�^
		

		//�E�B���h�E���쐬����
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//�E�B���h�E�����Ƃ��ɁC����������悤�ɐݒ肷��
		setTitle("MyClient");//�E�B���h�E�̃^�C�g����ݒ肷��
		setSize(500,500);//�E�B���h�E�̃T�C�Y��ݒ肷��
		c = getContentPane();//�t���[���̃y�C�����擾����

		//�A�C�R���̐ݒ�
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");
        pass1 = new ImageIcon("pass");
        reset1 = new ImageIcon("reset");
        
        jud = false; //���������false�ɂ��Ă���

        c.setLayout(null);//�������C�A�E�g�̐ݒ���s��Ȃ�
		//�{�^���̐���
		buttonArray = new JButton[8][8];//�{�^���̔z���8*8�쐬����
            int k = 0;
		for(int i=0;i<8;i++){
            for(int j=0; j<8; j++){
                if(((i==3)&&(j==3)) || ((i==4)&&(j==4))){
                    buttonArray[j][i] = new JButton(whiteIcon);//���̏����ʒu
                }else if(((i==4)&&(j==3)) || ((i==3)&&(j==4))){
                    buttonArray[j][i] = new JButton(blackIcon);//���̏����ʒu
                }else{
                    buttonArray[j][i] = new JButton(boardIcon);//�{�^���Ƀ{�[�h�A�C�R����ݒ肷��
                }
                c.add(buttonArray[j][i]);//�y�C���ɓ\��t����
                buttonArray[j][i].setBounds(10+j*45,10+i*45,45,45);//�{�^���̑傫���ƈʒu��ݒ肷��D(x���W�Cy���W,x�̕�,y�̕��j
                buttonArray[j][i].addMouseListener(this);//�{�^�����}�E�X�ł�������Ƃ��ɔ�������悤�ɂ���
                buttonArray[j][i].addMouseMotionListener(this);//�{�^�����}�E�X�œ��������Ƃ����Ƃ��ɔ�������悤�ɂ���
                buttonArray[j][i].setActionCommand(Integer.toString(k));//�{�^���ɔz��̏���t������i�l�b�g���[�N����ăI�u�W�F�N�g�����ʂ��邽�߁j
                k++;
            }
		}
        //pass�{�^���̍쐬
        pass = new JButton("pass", pass1);
        c.add(pass);//�y�C���ɓ\��t����
        pass.setBounds(10,380,90,45);//�{�^���̑傫���ƈʒu��ݒ肷��D(x���W�Cy���W,x�̕�,y�̕��j
        pass.addMouseListener(this);//�{�^�����}�E�X�ł�������Ƃ��ɔ�������悤�ɂ���
        pass.addMouseMotionListener(this);//�{�^�����}�E�X�œ��������Ƃ����Ƃ��ɔ�������悤�ɂ���
        
        //reset�{�^���̍쐬
        pass = new JButton("reset", reset1);
        c.add(pass);//�y�C���ɓ\��t����
        pass.setBounds(100,380,90,45);//�{�^���̑傫���ƈʒu��ݒ肷��D(x���W�Cy���W,x�̕�,y�̕��j
        pass.addMouseListener(this);//�{�^�����}�E�X�ł�������Ƃ��ɔ�������悤�ɂ���
        pass.addMouseMotionListener(this);//�{�^�����}�E�X�œ��������Ƃ����Ƃ��ɔ�������悤�ɂ���
        
		
		//�T�[�o�ɐڑ�����
		Socket socket = null;
		try {
			//"localhost"�́C���������ւ̐ڑ��Dlocalhost��ڑ����IP Address�i"133.42.155.201"�`���j�ɐݒ肷��Ƒ���PC�̃T�[�o�ƒʐM�ł���
			//10000�̓|�[�g�ԍ��DIP Address�Őڑ�����PC�����߂āC�|�[�g�ԍ��ł���PC�㓮�삷��v���O��������肷��
            if(IPname.equals("")){
			socket = new Socket("localhost", 10000);
            }else{
                socket = new Socket(IPname, 10000);
            }
			
		} catch (UnknownHostException e) {
			System.err.println("�z�X�g�� IP �A�h���X������ł��܂���: " + e);
		} catch (IOException e) {
			 System.err.println("�G���[���������܂���: " + e);
		}
		
		MesgRecvThread mrt = new MesgRecvThread(socket, myName);//��M�p�̃X���b�h���쐬����
		mrt.start();//�X���b�h�𓮂����iRun�������j
	}
		
	//���b�Z�[�W��M�̂��߂̃X���b�h
	public class MesgRecvThread extends Thread {
		
		Socket socket;
		String myName;
		
		public MesgRecvThread(Socket s, String n){
			socket = s;
			myName = n;
		}
		
		//�ʐM�󋵂��Ď����C��M�f�[�^�ɂ���ē��삷��
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);//�ڑ��̍ŏ��ɖ��O�𑗂�
                
                String myNumberStr = br.readLine();
                System.out.println("�v�����g" + myNumberStr);
                int myNumberInt = Integer.parseInt(myNumberStr);//���Ԗڂ̐ڑ���
                if(myNumberInt % 2 == 0){//�����ԖڂɌq�����[��������s
                    myColor = 0;
                    myIcon = blackIcon;
                    yourIcon = whiteIcon;
                    myTurn = 0;
                    //�F�t�����x���̍쐬
                    theLabel1 = new JLabel("�����̔Ԃł�");
                    c.add(theLabel1);
                    theLabel1.setBounds(200,380,250,45);
                    theLabel1.setFont(new Font( "�l�r �S�V�b�N" , Font.BOLD, 20));
                    theLabel1.setForeground(Color.BLACK); 
                    
                    JLabel theLabel2 = new JLabel("��");
                    c.add(theLabel2);
                    theLabel2.setBounds(380,10,150,45);
                    theLabel2.setFont(new Font( "�l�r �S�V�b�N" , Font.BOLD, 30));
                    theLabel2.setForeground(Color.BLACK); 
                }else{//��ԖڂɌq�����[��������U
                    myColor = 1;
                    myIcon = whiteIcon;
                    yourIcon = blackIcon;
                    myTurn = 1;
                    //�F�t�����x���̍쐬
                    theLabel1 = new JLabel("����̔Ԃł�");
                    c.add(theLabel1);
                    theLabel1.setBounds(200,380,250,45);
                    theLabel1.setFont(new Font( "�l�r �S�V�b�N" , Font.BOLD, 20));
                    theLabel1.setForeground(Color.BLACK); 
                    
                    JLabel theLabel2 = new JLabel("��");
                    c.add(theLabel2);
                    theLabel2.setBounds(380,10,150,45);
                    theLabel2.setFont(new Font( "�l�r �S�V�b�N" , Font.BOLD, 30));
                    theLabel2.setForeground(Color.WHITE); 
                    
                }
                
                
				while(true) {
					String inputLine = br.readLine();//�f�[�^����s�������ǂݍ���ł݂�
					if (inputLine != null) {//�ǂݍ��񂾂Ƃ��Ƀf�[�^���ǂݍ��܂ꂽ���ǂ������`�F�b�N����
						System.out.println(inputLine);//�f�o�b�O�i����m�F�p�j�ɃR���\�[���ɏo�͂���
						String[] inputTokens = inputLine.split(" ");	//���̓f�[�^����͂��邽�߂ɁA�X�y�[�X�Ő؂蕪����
						String cmd = inputTokens[0];//�R�}���h�̎��o���D�P�ڂ̗v�f�����o��
                        if(cmd.equals("PLACE")){//cmd�̕�����"PLACE"�����������ׂ�D��������true�ƂȂ�
							//PLACE�̎��̏���(�R�}�̐F��ς��鏈��)
							String theBName = inputTokens[1];//�{�^���̖��O�i�ԍ��j�̎擾
							int theBnum = Integer.parseInt(theBName);//�{�^���̖��O�𐔒l�ɕϊ�����
                            int y = theBnum / 8; //y���W
                            int x = theBnum % 8; //x���W
							int theColor = Integer.parseInt(inputTokens[2]);//�F�𐔒l�ɕϊ�����
                            if(theColor == myColor){
                                //���M���N���C�A���g�ł̏���
                                buttonArray[x][y].setIcon(myIcon);
                            } else {
                                //���M��N���C�A���g�ł̏���
                                buttonArray[x][y].setIcon(yourIcon);
                            }
                            myTurn = 1 - myTurn;
                            passcount = 0;
                            turntext();
                            IconCount();//���̎��_�ŏ��s����������̂��ߊe�R�}�̐��𐔂���
                            if(myTurn == 0){
                                halfjudge();//���s�������m�F����֐�(2�񏟗����o���Ȃ��悤�ɕЕ��������������s��)
                                if(!jud ){
                                    autopass();//���̃^�[���̃R�}���I�[�g�p�X���邩�ǂ����̔���
                                }
                            }
						}
                        //�Q��p�X����Ə��s����
                        if(cmd.equals("PASS")){//cmd�̕�����"PASS"�����������ׂ�D��������true�ƂȂ�
                            if(passcount < 1){
                                myTurn = 1 - myTurn;
                                passcount++;
                                turntext(); //�^�[��������ւ�����̂ŕ\����؂�ւ���
                                if(myTurn == 0){
                                    autopass();//���̃^�[���̃R�}���I�[�g�p�X���邩�ǂ����̔���
                                }
                            }else{
                                myTurn = 1;
                                judgement();
                            }
						}
                        //64�}�X���܂�A�S�ł̏ꍇ�ɏ��s����
                        if(cmd.equals("JUDGE")){//cmd�̕�����"JUDGE"�����������ׂ�D��������true�ƂȂ�
                            myTurn = 1;
                            judgement(); //���s������o�͂���֐��ɔ��
                        }
                        //�Ֆʂ����Z�b�g����
                        if(cmd.equals("RESET")){ //reset�{�^���������ƔՖʂ������������
                            jud = false; //���s�����false�ɂ���
                            for(int i=0;i<8;i++){
                                for(int j=0; j<8; j++){
                                    if(((i==3)&&(j==3)) || ((i==4)&&(j==4))){
                                        buttonArray[i][j].setIcon(whiteIcon);//���̏����ʒu
                                    }else if(((i==4)&&(j==3)) || ((i==3)&&(j==4))){
                                        buttonArray[i][j].setIcon(blackIcon);//���̏����ʒu
                                    }else{
                                        buttonArray[i][j].setIcon(boardIcon);//�{�^���Ƀ{�[�h�A�C�R����ݒ肷��
                                    }
                                }
                            }
                            if(myNumberInt % 2 == 0){//�����ԖڂɌq�����[������s
                                myTurn = 0;
                                turntext();
                                theLabel1.setForeground(Color.BLACK); 
                            }else{//��ԖڂɌq�����[��������U
                                myTurn = 1;
                                turntext();
                                theLabel1.setForeground(Color.BLACK); 
                            }
                        }
                        //���܂ꂽ�R�}�����Ԃ�
                        if(cmd.equals("FLIP")){//cmd�̕�����"FLIP"�����������ׂ�D��������true�ƂȂ�
							//TURN�̎��̏���(�R�}�𗠕Ԃ�����)
                            int theColor = Integer.parseInt(inputTokens[3]);//�F�𐔒l�ɕϊ�����
							int x = Integer.parseInt(inputTokens[1]);
                            int y = Integer.parseInt(inputTokens[2]);
                            if(theColor == myColor){
                            //���M���N���C�A���g�ł̏���
                                buttonArray[x][y].setIcon(myIcon);
                            } else {
                                //���M��N���C�A���g�ł̏���
                                buttonArray[x][y].setIcon(yourIcon);
                            }
                        }
                        //���s����ʂɏo�͂���
                        if(cmd.equals("JUDGETEXT")){
							//TURN�̎��̏���(�R�}�𗠕Ԃ�����)
                            int winColor = Integer.parseInt(inputTokens[1]);//�F�𐔒l�ɕϊ�����
                            if(winColor == 2){
                                theLabel1.setText("���������I");
                            }else if(winColor == myColor){
                                //�����\��
                                theLabel1.setText("�����I");
                                theLabel1.setForeground(Color.RED); 
                            } else {
                                //�s�k�\��
                                theLabel1.setText("�����I");
                                theLabel1.setForeground(Color.BLUE); 
                            }
                        }
					}else{
						break;
					}
				
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("�G���[���������܂���: " + e);
			}
		}
	}

	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
	}
  	
	public void mouseClicked(MouseEvent e) {//�{�^�����N���b�N�����Ƃ��̏���
        if(myTurn == 0){//myTurn��0�̎��̂݃R�}���u����
            System.out.println("�N���b�N");
            JButton theButton = (JButton)e.getComponent();//�N���b�N�����I�u�W�F�N�g�𓾂�D�^���Ⴄ�̂ŃL���X�g����
            String theArrayIndex = theButton.getActionCommand();//�{�^���̔z��̔ԍ������o��

            Icon theIcon = theButton.getIcon();//theIcon�ɂ́C���݂̃{�^���ɐݒ肳�ꂽ�A�C�R��������
            System.out.println("�A�C�R��" + theIcon);//�f�o�b�O�i�m�F�p�j�ɁC�N���b�N�����A�C�R���̖��O���o�͂���
            
            //�p�X������
            if(theIcon == pass1){
                //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
                String msg = "PASS";
                //�T�[�o�ɏ��𑗂�
                out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
                out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
            }
            
            //�R�}��V���ɒu��
            if(theIcon == boardIcon){//theIcon��boardIcon�̎��ɂ����R�}��u����悤�ɂ���
                int temp = Integer.parseInt(theArrayIndex);
                int y = temp / 8;
                int x = temp % 8;
                if(judgeButton(y, x)){
                    if(myColor == 0){
                        theButton.setIcon(blackIcon);
                    }else{
                        theButton.setIcon(whiteIcon);
                    }
                    //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
                    String msg = "PLACE"+" "+theArrayIndex+" "+myColor;

                    //�T�[�o�ɏ��𑗂�
                    out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
                    out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
                }else{
                    System.out.println("�����ɂ͒u���܂���");
                }
            }
            
            repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
        }
        
        //�Ֆʂ̃��Z�b�g
        System.out.println("�N���b�N");
        JButton theButton = (JButton)e.getComponent();//�N���b�N�����I�u�W�F�N�g�𓾂�D�^���Ⴄ�̂ŃL���X�g����
        String theArrayIndex = theButton.getActionCommand();//�{�^���̔z��̔ԍ������o��

        Icon theIcon = theButton.getIcon();//theIcon�ɂ́C���݂̃{�^���ɐݒ肳�ꂽ�A�C�R��������
        System.out.println("�A�C�R��" + theIcon);//�f�o�b�O�i�m�F�p�j�ɁC�N���b�N�����A�C�R���̖��O���o�͂���
        if(theIcon == reset1){
            //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
            String msg = "RESET";
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
            repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
        }
        
	}
	
    //���̑��̃}�E�X�̏����͎g��Ȃ�
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
    
    //�N���b�N�����A�C�R���̎l���W�}�X��yourIcon�����邩���肷��
    public boolean judgeButton(int y, int x){
        boolean flag = false; //�����ɃR�}���u����ꍇflag��true�ɕς��
        ArrayList<Integer> list = new ArrayList<Integer>(); //�Ԃ�R�}���L�^����z�������Ă���
        
        for(int i=-1; i<=1; i++){
            for(int j=-1; j<=1; j++){
                if((x+j>=0) && (x+j<=7) && (y+i>=0) && (y+i<=7 )){ //8*8�̔Ղɔ[�܂�͈͂Ŋm�F����
                    //���̃R�}�����Ԃ�̂��m�F����
                    if(flipButtons(x, y, j, i, list) >= 1){
                        flag = true; //���Ԃ�R�}���������ꍇ�����փR�}���u����悤�ɂȂ�
                        TurnButton(list); //�R�}�����ۂɗ��Ԃ�����
                        list.clear();
                    }
                }
            }
        }
        if(!flag){
            System.out.println("flag��false�̂܂܂ł�");
        }
        return flag;
    }
    
    //�R�}���������Ԃ�̂��m�F����
    public int flipButtons(int x, int y, int j, int i, ArrayList<Integer> list){
        int flipNum = 0; //���Ԃ�R�}�̐���Ԃ�
        Icon theIcoon = buttonArray[x+j][y+i].getIcon();
        int a = j;
        int b = i;
        
        if(theIcoon == yourIcon){//yourIcon���������ꍇ�ǂ��܂ő������m�F����
            while(true){//yourIcon����������֏��ԂɃR�}�����Ă���
                if((x+a<0) || (x+a>7) || (y+b<0) || (y+b>7 )){ //�ՊO�܂ōs�����ꍇ�̓��[�v�𔲂��o��
                    flipNum = 0; //���܂łɐ����Ă����R�}�̐���0�ɂ���
                    break;
                }
                Icon theIcooon = buttonArray[x+a][y+b].getIcon();
                if(theIcooon == yourIcon){
                    list.add(x+a);
                    list.add(y+b);
                    flipNum++;
                }else if(theIcooon == myIcon){
                    break; //����܂ł�flipNum�Ő��������������Ԃ�
                }else{
                    flipNum = 0;
                    break;
                }
                //���̃}�X������
                a += j;
                b += i;
            }
        }
        if(flipNum == 0){ //���Ԃ�R�}���Ȃ��ꍇ�͌��Ƃ��Ċo���Ă������R�}�̈ʒu�����X�g�������
            list.clear();
        }
        return flipNum;
    }
    
    //���X�g�Ŋo���Ă����R�}�����ۂɂЂ�����Ԃ�����
    public void TurnButton(ArrayList<Integer> list){
        for(int i = 0; i < list.size(); i++){ //1�������𑗂�
            int x = list.get(i);
            int y = list.get(i+1);
            String msg = "FLIP"+" "+x+" "+y+" "+myColor;
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����

            repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
            i++;
        }
    }
    
    //���s������o�͂��镔��
    public void judgement(){
        IconCount();
        int judgetext = 100; //��������𐔎��ő���
        System.out.println("���̐��F" + CountWhite);
        System.out.println("���̐��F" + CountBlack);
        if(CountWhite > CountBlack){
            System.out.println("���̏���");
            judgetext = 1;
        }else if(CountWhite < CountBlack){
            System.out.println("���̏���");
            judgetext = 0;
        }else{
            System.out.println("��������");
            judgetext = 2;
        }
        String msg = "JUDGETEXT"+" "+judgetext;
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����

            repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
    }
    
    //�Ղɂ���e�A�C�R���̖������J�E���g���鏈��
    public void IconCount(){
        CountWhite = 0;
        CountBlack = 0;
        CountBoard = 0;
        for(int i=0;i<8;i++){
            for(int j=0; j<8; j++){
                JButton theButton = buttonArray[j][i];
                Icon theIcon = theButton.getIcon();//theIcon�ɂ́C���݂̃{�^���ɐݒ肳�ꂽ�A�C�R��������
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
    
    //���݂ǂ���̃^�[�����e�L�X�g�\������֐�
    public void turntext(){
        if(myTurn == 0){
            theLabel1.setText("�����̔Ԃł�");
        }else if(myTurn == 1){
            if(auto){
                theLabel1.setText("�����p�X���܂���"); //�����p�X�����ꍇ�͒m�点��
            }else{
                theLabel1.setText("����̔Ԃł�");
            }
        }
    }
    
    //�r���ŏ������肪�����̊m�F���s���֐�
    public void halfjudge(){
        //64�}�X�R�}�����܂��Ă����ꍇ
        if(CountBoard == 0){
            jud = true;
            System.out.println("64�}�X���܂�܂���");
            //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
            String msg = "JUDGE";
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
        }
        
        //�����S�Łi�Ղ����ׂč��j�̏ꍇ�j
        if((CountWhite == 0) && (CountBlack > 0)){
            jud = true;
            System.out.println("���̃R�}���Ȃ��Ȃ�܂���");
            //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
            String msg = "JUDGE";
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
        }
        
        //�����S�Łi�Ղ����ׂĔ��j�̏ꍇ�j
        if((CountBlack == 0) && (CountWhite > 0)){
            jud = true;
            System.out.println("���̃R�}���Ȃ��Ȃ�܂���");
            //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
            String msg = "JUDGE";
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
        }
        repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
    }
    
    //�R�}���u���ꏊ���Ȃ����ǂ����i�����p�X�j�̔�����s���B
    public void autopass(){
        auto = false; //auto�t���b�O�������Ă���
        int notpass = 0; //�u����ӏ����J�E���g����
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
        if(notpass == 0){ //�u����ꏊ���ꂩ�����Ȃ������ꍇ�̏���
            auto = true;
            System.out.println("�����̃^�[���ɃR�}��u���ꏊ������܂���");
            //���M�����쐬����i��M���ɂ́C���̑��������ԂɃf�[�^�����o���D�X�y�[�X���f�[�^�̋�؂�ƂȂ�j
            String msg = "PASS";
            //�T�[�o�ɏ��𑗂�
            out.println(msg);//���M�f�[�^���o�b�t�@�ɏ����o��
            out.flush();//���M�f�[�^���t���b�V���i�l�b�g���[�N��ɂ͂��o���j����
            repaint();//��ʂ̃I�u�W�F�N�g��`�悵����
        }else{
            System.out.println("�����̃^�[���ɃR�}��u���ꏊ������܂�");
        }
    }
    
    //�I�������A�C�R���̂̎l���W�}�X��yourIcon�����邩���肷��
    public boolean judgeButton1(int y, int x){
        boolean flag = false; //�����ɃR�}���u����ꍇflag��true�ɕς��
        ArrayList<Integer> list = new ArrayList<Integer>(); //�Ԃ�R�}���L�^����z�������Ă���
        int flip = 0; //�����R�}�����Ԃ邩������
        
        for(int i=-1; i<=1; i++){
            for(int j=-1; j<=1; j++){
                if((x+j>=0) && (x+j<=7) && (y+i>=0) && (y+i<=7 )){ //8*8�̔Ղɔ[�܂�͈͂Ŋm�F����
                    //���̃R�}�����Ԃ�̂��m�F����
                    if(flipButtons(x, y, j, i, list) >= 1){
                        flag = true; //���Ԃ�R�}���������ꍇ�����փR�}���u����悤�ɂȂ�
                        flip += list.size() / 2;
                        list.clear();
                    }
                }
            }
        }
        if(flip > 0){
            System.out.println("x: " + x + " ,y: " + y + "�ɃR�}��u����" + flip + "�����Ԃ�܂�");
        }
        return flag;
    }
}
