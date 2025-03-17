package RMI;

import Banco.No;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AplicacaoRemoteInterface extends Remote {
    List<String> inserirBackup(No no) throws RemoteException;
}
