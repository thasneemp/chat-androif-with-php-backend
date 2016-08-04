package test.launcher.mummu.chatexamplegooglerecomended.interfaces;


public interface ISocketOperator {

    String sendHttpRequest(String params);

    boolean sendMessage(String message, String ip, int port);

    int startListening(int port);

    void stopListening();

    void exit();

    int getListeningPort();

}
