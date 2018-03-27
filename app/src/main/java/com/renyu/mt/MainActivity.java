package com.renyu.mt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.renyu.mt.service.MTService;

public class MainActivity extends AppCompatActivity {

    Button btn_conn;
    Button btn_login;
    Button btn_getfriends;
    Button btn_getfriend;
    Button btn_getofflinemessage;
    Button btn_getsysmessage;
    Button btn_getsetting;
    Button btn_chattext;
    Button btn_chatpic;
    Button btn_voice;
    Button btn_getgroup;
    Button btn_getsinglegroupinfo;
    Button btn_updategroupnickname;
    Button btn_updategroupremark;
    Button btn_updategroupinfo;
    Button btn_updategroupmessagesetting;
    Button btn_requestgroupinfo;
    Button btn_requestgroupmembers;
    Button btn_requestgroupmembersinfo;
    Button btn_deletegroupuser;
    Button btn_addgroupuser;
    Button btn_creategroup;
    Button btn_groupchatpic;
    Button btn_groupchatvoice;
    Button btn_notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_conn= (Button) findViewById(R.id.btn_conn);
        btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MTService.conn(MainActivity.this);
            }
        });
        btn_login= (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MTService.reqLogin(MainActivity.this, "wuhan", "");
            }
        });
        btn_getfriends= (Button) findViewById(R.id.btn_getfriends);
        btn_getfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "reqFriendGroups");
                startService(intent);
            }
        });
        btn_getfriend= (Button) findViewById(R.id.btn_getfriend);
        btn_getfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "reqFriends");
                startService(intent);
            }
        });
        btn_getofflinemessage= (Button) findViewById(R.id.btn_getofflinemessage);
        btn_getofflinemessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "reqGetOfflineMessage");
                startService(intent);
            }
        });
        btn_getsysmessage= (Button) findViewById(R.id.btn_getsysmessage);
        btn_getsysmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "getsystemmessage");
                startService(intent);
            }
        });
        btn_getsetting= (Button) findViewById(R.id.btn_getsetting);
        btn_getsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "getsetting");
                startService(intent);
            }
        });
        btn_chattext= (Button) findViewById(R.id.btn_chattext);
        btn_chattext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "sendTextMessage");
                startService(intent);
            }
        });
        btn_chatpic= (Button) findViewById(R.id.btn_chatpic);
        btn_chatpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "sendPicMessage");
                startService(intent);
            }
        });
        btn_voice= (Button) findViewById(R.id.btn_voice);
        btn_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "sendVoiceMessage");
                startService(intent);
            }
        });
        btn_getgroup= (Button) findViewById(R.id.btn_getgroup);
        btn_getgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "getGroupList");
                startService(intent);
            }
        });
        btn_getsinglegroupinfo= (Button) findViewById(R.id.btn_getsinglegroupinfo);
        btn_getsinglegroupinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "getGroupSingleUserInfo");
                startService(intent);
            }
        });
        btn_updategroupnickname= (Button) findViewById(R.id.btn_updategroupnickname);
        btn_updategroupnickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "updateGroupNickName");
                startService(intent);
            }
        });
        btn_updategroupremark= (Button) findViewById(R.id.btn_updategroupremark);
        btn_updategroupremark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "updateGroupRemark");
                startService(intent);
            }
        });
        btn_updategroupinfo= (Button) findViewById(R.id.btn_updategroupinfo);
        btn_updategroupinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "updateGroupInfo");
                startService(intent);
            }
        });
        btn_updategroupmessagesetting= (Button) findViewById(R.id.btn_updategroupmessagesetting);
        btn_updategroupmessagesetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "updateGroupMessageSetting");
                startService(intent);
            }
        });
        btn_requestgroupinfo= (Button) findViewById(R.id.btn_requestgroupinfo);
        btn_requestgroupinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "requestGroupInfo");
                startService(intent);
            }
        });
        btn_requestgroupmembers= (Button) findViewById(R.id.btn_requestgroupmembers);
        btn_requestgroupmembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "requestGroupMember");
                startService(intent);
            }
        });
        btn_requestgroupmembersinfo= (Button) findViewById(R.id.btn_requestgroupmembersinfo);
        btn_requestgroupmembersinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "requestGroupMemberInfo");
                startService(intent);
            }
        });
        btn_deletegroupuser= (Button) findViewById(R.id.btn_deletegroupuser);
        btn_deletegroupuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "deleteGroupUser");
                startService(intent);
            }
        });
        btn_addgroupuser= (Button) findViewById(R.id.btn_addgroupuser);
        btn_addgroupuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "inviteUserJoinGroup");
                startService(intent);
            }
        });
        btn_creategroup= (Button) findViewById(R.id.btn_creategroup);
        btn_creategroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "createGroup");
                startService(intent);
            }
        });
        btn_groupchatpic= (Button) findViewById(R.id.btn_groupchatpic);
        btn_groupchatpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "groupchatpic");
                startService(intent);
            }
        });
        btn_groupchatvoice= (Button) findViewById(R.id.btn_groupchatvoice);
        btn_groupchatvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "groupchatvoice");
                startService(intent);
            }
        });
        btn_notify= (Button) findViewById(R.id.btn_notify);
        btn_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MTService.class);
                intent.putExtra("type", "getSysNty");
                startService(intent);
            }
        });
    }
}
