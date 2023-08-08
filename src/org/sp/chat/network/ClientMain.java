package org.sp.chat.network;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sp.chat.client.domain.Room;
import org.sp.chat.client.domain.Roommate;
import org.sp.chat.client.model.RoommateDAO;
import org.sp.chat.client.view.ChatMain;

import util.DBManager;

public class ClientMain extends JFrame{
	JPanel p_north;
	JPanel p_center;
	JPanel p_south;
	JComboBox box;
	JLabel la_name;
	JButton bt_connect;
	JScrollPane scroll;
	JTextArea t_input;
	JButton bt_input;
	JScrollPane scroll_input;
	
	Socket socket;
	ClientMessageThread cmt;
	Room room;
	DBManager dbManager;
	RoommateDAO roommateDAO;
	Roommate roommate;
	List<Roommate> roommateList=new ArrayList<Roommate>();
	
	public ClientMain(Room room) {
		this.room=room;
		p_north = new JPanel();
		p_center = new JPanel();
		p_south = new JPanel();
		box = new JComboBox();
		bt_connect = new JButton("접속");
		la_name = new JLabel("채팅방");
	
		scroll = new JScrollPane(p_center);
		t_input = new JTextArea();
		bt_input = new JButton("입력");
		scroll_input = new JScrollPane(t_input);
		
		dbManager = new DBManager();
		roommateDAO = new RoommateDAO(dbManager);
		
		box.addItem("192.168.1.37");
		box.addItem("192.168.0.14");
		box.addItem("192.168.1.220");
		box.addItem("192.168.1.224");
		box.addItem("192.168.1.229");
		box.addItem("192.168.0.2");

		//스타일
		p_north.setPreferredSize(new Dimension(380,50));
		p_south.setPreferredSize(new Dimension(380,50));
		scroll.setPreferredSize(new Dimension(300,500));
		scroll_input.setPreferredSize(new Dimension(230,45));
		t_input.setLineWrap(true);
		p_center.setLayout(new BoxLayout(p_center, BoxLayout.Y_AXIS));
		
		//조립
		p_north.add(la_name);
		p_north.add(box);
		p_north.add(bt_connect);
		p_south.add(scroll_input);
		p_south.add(bt_input);
		
		add(p_north, BorderLayout.NORTH);
		add(scroll);
		
		add(p_south, BorderLayout.SOUTH);
		
		setBounds(0, 0, 380, 600);
		setVisible(true);
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		
		bt_connect.addActionListener((e)->{
			connect();
		});
		
		
		t_input.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int key=e.getKeyCode();
				if(key == KeyEvent.VK_ENTER) {//엔터치면..
					//서버에 메시지 전송
					send();
				}
			}
		});
		
		bt_input.addActionListener((e)->{
			send();
			
		});
		getRoommate();
		
	}
	
	public void connect() {
		String ip=(String)box.getSelectedItem();
		//int port=Integer.parseInt(t_port.getText());
		
		try {
			socket = new Socket(ip, 7777);
			
			cmt = new ClientMessageThread(this);
			cmt.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getRoommate() {
		roommateList=roommateDAO.selectChat(room.getRoom_idx());
		System.out.println(roommateList.size()+"참여 중");
	}
	
	public void send() {
		StringBuffer sb = new StringBuffer();
		
		//개발자가 전송 프로토콜을 정의한다..
		sb.append("{");
		sb.append("\"idx\":"+ChatMain.member.getMember_idx()+",");
		sb.append("\"id\" :\""+ChatMain.member.getId()+"\", ");
		sb.append("\"name\" :\""+ChatMain.member.getName()+"\", ");
		sb.append("\"nick\" :\""+ChatMain.member.getNick()+"\", ");
		sb.append("\"email\" :\""+ChatMain.member.getEmail()+"\", ");
		sb.append("\"img\" :\""+ChatMain.member.getImg()+"\", ");
		sb.append("\"friends\":[");
		
		for(int i=0;i<roommateList.size();i++) {   // 3-1보다 작을때까지  쉼표..
			Roommate roommate =roommateList.get(i);
			
			sb.append("{");
			sb.append("\"member_idx\":"+roommate.getMember().getMember_idx()+",");
			sb.append("\"room_idx\":"+roommate.getRoom().getRoom_idx()+",");
			//sb.append("\"name\":\"zino\"");
			if(i<(roommateList.size()-1)) {
				sb.append("},");
			}else {
				sb.append("}");
			}
		}
		sb.append("],");
		
		String str= t_input.getText().replace("\n", "");
		
		sb.append("\"contents\" :\""+str+"\" ");
		sb.append("}");
	
		System.out.println(sb.toString());
		
		cmt.sendMsg(sb.toString());
		
		t_input.setText("");
	}
	
	/*public static void main(String[] args) {
		new ClientMain();
	}
	*/
}