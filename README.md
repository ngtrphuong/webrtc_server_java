
## Override

Corresponding app - 
https://github.com/ddssingsong/webrtc_android
A set of server java version used

Realize basic signaling sending and receiving, and cooperate with Android to realize basic calling, ringing, hanging up, voice calling, and video calling functions


Android access address is ws://ip:port/websocket

## Branch introduction

*master*

Cooperate with the business logic of the Java version on the Android side to realize the basic functions of calling, ringing, voice calling, and video calling

*nodejs_copy*

Change nodejs version https://github.com/ddssingsong/webrtc_server_node Wrote it in java


## Signaling related


1. Log in successfully, return personal information, used to display the user's online status

   ```json
   {
   	"eventName":"__login_success",
   	"data":{
           "userID":"userId",
           "avatar":"...jpg"
       }
   }
   ```

   

2. Invite to join the room

   ```json
   # The server is responsible for forwarding
   {		
     "eventName":"__invite",
     "data":{
           "room":"room",
           "roomSize":"9",
           "mediaType":"1",  // 0 video 1 voice
       	"inviteID":"userId",
           "userList":"userId,usrId,userId"  #Comma separated
       }
   }
   
   1. Create a room
   2. Send invitation
   3.
   ```

   

3. Cancel outgoing

   ```
   Cancel the invitation during the call
   {
       "eventName":"__cancel",
       "data":{
           "inviteID":"userId",
           "userList":"userId,usrId,userId" 
       }
   }
   ```

   

4. The other party has ringed

   ```json
   {
       "eventName":"__ring",
       "data":{
           "inviteID":"userId",
           "fromID":"myId"
       }
   }
   ```